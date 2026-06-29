# Protrack — Phase 1 MVP · REST API Specification

> Status: Design contract, pre-implementation. No implementation code.
> Contract between: React (Vercel) ⇄ Spring Boot (Render) ⇄ FastAPI AI Service (Render).
> Aligns to approved architecture + database design.

---

## 1. Conventions

### 1.1 Versioning & naming
- **URI versioning:** all public endpoints under `/api/v1`. Internal AI contract under `/internal/v1`.
- **Resources are plural nouns**, kebab-free, lowercase: `/projects`, `/ai-jobs`, `/workflow-stages`.
- **Sub-resources nest** under their parent: `/projects/{projectId}/documents`.
- **Actions that aren't CRUD** use a verb sub-resource via POST: `/projects/{id}/analysis`, `/issues/{id}/decision`, `/projects/{id}/signoff`.
- **JSON only.** `Content-Type: application/json` (except file upload = `multipart/form-data`). Field names **camelCase**. Timestamps **ISO-8601 UTC**. IDs are UUID strings (or int for reference data).

### 1.2 Authentication & authorization
- **Bearer JWT** in `Authorization: Bearer <token>` on every endpoint except `/auth/login`, `/auth/refresh`, `/auth/forgot-password`, `/auth/reset-password`, and the health check.
- Access token short-lived (~15 min); refresh token rotated (~7 days).
- **RBAC** enforced server-side per endpoint (roles: `ADMIN`, `PM`, `DESIGNER`, `QA`). Each endpoint below lists allowed roles. Workflow transitions are additionally guarded by stage + ownership.
- Internal AI endpoints authenticated with `X-Internal-Key` (shared secret/HMAC), never exposed publicly.

### 1.3 Pagination, filtering, sorting, searching
- **Pagination (page-based):** `?page=0&size=20` (0-based; default size 20, max 100).
- **Sorting:** `?sort=createdAt,desc` (repeatable: `?sort=priority,desc&sort=dueDate,asc`).
- **Filtering:** explicit query params per resource (e.g. `?stage=AI_ANALYSIS&imprintId=...&status=ACTIVE&mine=true`).
- **Searching:** `?q=<term>` (full-text-ish over title/ISBN/manuscript name).
- **Standard list envelope:**
```json
{
  "content": [ ... ],
  "page": 0, "size": 20, "totalElements": 137, "totalPages": 7,
  "sort": "createdAt,desc", "hasNext": true
}
```

### 1.4 HTTP status codes
| Code | Use |
|---|---|
| 200 OK | successful GET/PATCH/POST returning data |
| 201 Created | resource created (returns the resource + `Location`) |
| 202 Accepted | async job started (analysis/preflight) — returns the `aiJob` resource |
| 204 No Content | successful DELETE / read-marking |
| 400 Bad Request | malformed request |
| 401 Unauthorized | missing/invalid token |
| 403 Forbidden | authenticated but role/stage not allowed |
| 404 Not Found | resource missing or not visible to caller |
| 409 Conflict | invalid state transition / duplicate (e.g. ISBN, email) |
| 422 Unprocessable Entity | validation failed (field-level errors) |
| 429 Too Many Requests | rate limit (AI endpoints) |
| 500 Internal Server Error | unexpected; AI provider failure surfaces as job FAILED, not 500 |

### 1.5 Standardized error response (RFC 9457-style Problem Details)
```json
{
  "type": "https://protrack.app/errors/validation",
  "title": "Validation failed",
  "status": 422,
  "code": "VALIDATION_ERROR",
  "detail": "One or more fields are invalid.",
  "instance": "/api/v1/projects",
  "traceId": "0HM...",
  "timestamp": "2026-06-29T10:15:30Z",
  "fieldErrors": [
    { "field": "title", "code": "NotBlank", "message": "Publication title is required" },
    { "field": "isbn", "code": "Pattern", "message": "ISBN format is invalid" }
  ]
}
```
Every error carries a stable machine `code`, a `traceId` (correlation id) and, for 422, `fieldErrors[]`.

### 1.6 Cross-cutting
- **Idempotency:** unsafe POSTs that create may accept `Idempotency-Key` header (uploads, project create).
- **Async + realtime:** long jobs return `202` + an `aiJob`; clients subscribe to **SSE** `GET /api/v1/projects/{id}/events` for progress and stage changes.
- **Rate limiting:** AI-triggering endpoints rate-limited per user; `429` with `Retry-After`.
- **CORS:** restricted to the Vercel origin.

---

## 2. OpenAPI skeleton (representative)

```yaml
openapi: 3.1.0
info:
  title: Protrack API
  version: 1.0.0
  description: AI Publishing Operating System — Phase 1 MVP contract.
servers:
  - url: https://api.protrack.app/api/v1
security:
  - bearerAuth: []
components:
  securitySchemes:
    bearerAuth: { type: http, scheme: bearer, bearerFormat: JWT }
    internalKey: { type: apiKey, in: header, name: X-Internal-Key }
  schemas:
    Problem:
      type: object
      properties:
        type: { type: string }
        title: { type: string }
        status: { type: integer }
        code: { type: string }
        detail: { type: string }
        instance: { type: string }
        traceId: { type: string }
        timestamp: { type: string, format: date-time }
        fieldErrors:
          type: array
          items:
            type: object
            properties:
              field: { type: string }
              code: { type: string }
              message: { type: string }
    Page:
      type: object
      properties:
        content: { type: array, items: {} }
        page: { type: integer }
        size: { type: integer }
        totalElements: { type: integer }
        totalPages: { type: integer }
        sort: { type: string }
        hasNext: { type: boolean }
    LoginRequest:
      type: object
      required: [email, password]
      properties:
        email: { type: string, format: email }
        password: { type: string, minLength: 8 }
    TokenResponse:
      type: object
      properties:
        accessToken: { type: string }
        refreshToken: { type: string }
        expiresIn: { type: integer }
        user: { $ref: '#/components/schemas/UserSummary' }
    CreateProjectRequest:
      type: object
      required: [title, imprintId, publicationType]
      properties:
        title: { type: string, minLength: 3, maxLength: 250 }
        isbn: { type: string, pattern: '^(97(8|9))?[0-9-]{9,17}$', nullable: true }
        imprintId: { type: string, format: uuid }
        publicationType: { type: string, enum: [STEM_TEXTBOOK, MONOGRAPH, JOURNAL, REFERENCE] }
        discipline: { type: string }
        brief: { type: string, maxLength: 4000, nullable: true }
        pageExtent: { type: integer, minimum: 1, nullable: true }
        trimSize: { type: string, nullable: true }
        priority: { type: string, enum: [LOW, MEDIUM, HIGH] }
        dueDate: { type: string, format: date, nullable: true }
        memberUserIds:
          type: array
          items: { type: string, format: uuid }
paths:
  /auth/login:
    post:
      security: []
      summary: Authenticate and receive JWTs
      requestBody:
        required: true
        content: { application/json: { schema: { $ref: '#/components/schemas/LoginRequest' } } }
      responses:
        '200': { description: OK, content: { application/json: { schema: { $ref: '#/components/schemas/TokenResponse' } } } }
        '401': { description: Invalid credentials, content: { application/json: { schema: { $ref: '#/components/schemas/Problem' } } } }
  /projects:
    get:
      summary: List projects (role-filtered)
      parameters:
        - { name: page, in: query, schema: { type: integer, default: 0 } }
        - { name: size, in: query, schema: { type: integer, default: 20, maximum: 100 } }
        - { name: sort, in: query, schema: { type: string, default: 'createdAt,desc' } }
        - { name: stage, in: query, schema: { type: string } }
        - { name: imprintId, in: query, schema: { type: string, format: uuid } }
        - { name: status, in: query, schema: { type: string } }
        - { name: mine, in: query, schema: { type: boolean } }
        - { name: q, in: query, schema: { type: string } }
      responses:
        '200': { description: OK, content: { application/json: { schema: { $ref: '#/components/schemas/Page' } } } }
    post:
      summary: Create a project (PM)
      requestBody:
        required: true
        content: { application/json: { schema: { $ref: '#/components/schemas/CreateProjectRequest' } } }
      responses:
        '201': { description: Created }
        '422': { description: Validation failed, content: { application/json: { schema: { $ref: '#/components/schemas/Problem' } } } }
        '409': { description: Duplicate ISBN }
  /projects/{id}/analysis:
    post:
      summary: Start AI manuscript analysis (PM) — async
      responses:
        '202': { description: Analysis job accepted }
        '409': { description: No manuscript uploaded / wrong stage }
```
*(Full component schemas + every path are itemized in the catalog below; this skeleton shows the shape and conventions.)*

---

## 3. Endpoint Catalog by Module

> Legend — Roles: A=Admin, P=PM, D=Designer, Q=QA. ✱ = async (202+job). Auth required unless marked public.

### 3.1 Auth & Session (identity)
| Method | Path | Roles | Purpose | Success |
|---|---|---|---|---|
| POST | `/auth/login` | public | email+password → JWTs | 200 |
| POST | `/auth/refresh` | public | rotate access token | 200 |
| POST | `/auth/logout` | all | revoke refresh token | 204 |
| POST | `/auth/forgot-password` | public | email reset link | 202 |
| POST | `/auth/reset-password` | public | set new password via token | 204 |
| GET | `/me` | all | profile + roles + permissions + nav model | 200 |
| PATCH | `/me` | all | update own profile/password | 200 |

### 3.2 Users, Roles, Permissions (identity — Admin)
| Method | Path | Roles | Purpose | Success |
|---|---|---|---|---|
| GET | `/users` | A | list (filter `role`,`status`; `q`; paginated) | 200 |
| POST | `/users` | A | create user + role | 201 |
| GET | `/users/{id}` | A | detail | 200 |
| PATCH | `/users/{id}` | A | update profile/status | 200 |
| DELETE | `/users/{id}` | A | soft-deactivate | 204 |
| POST | `/users/{id}/roles` | A | assign role | 200 |
| DELETE | `/users/{id}/roles/{roleId}` | A | revoke role | 204 |
| POST | `/users:bulk` | A | **bulk** create/deactivate | 200 |
| GET | `/roles` | A,P | reference list | 200 |
| GET | `/permissions` | A | reference list | 200 |

### 3.3 Imprints & Reference (catalog)
| Method | Path | Roles | Purpose | Success |
|---|---|---|---|---|
| GET | `/imprints` | all | list imprints | 200 |
| POST | `/imprints` | A | create | 201 |
| PATCH | `/imprints/{id}` | A | update | 200 |
| DELETE | `/imprints/{id}` | A | retire (RESTRICT if in use) | 204/409 |
| GET | `/workflow-stages` | all | the 7 canonical stages | 200 |

### 3.4 Dashboard & Search
| Method | Path | Roles | Purpose | Success |
|---|---|---|---|---|
| GET | `/dashboard` | all | role-aware KPIs, active projects, AI insights, pipeline counts | 200 |
| GET | `/search?q=` | all | global ⌘K search (projects, manuscripts) | 200 |

### 3.5 Projects, Members, Workflow
| Method | Path | Roles | Purpose | Success |
|---|---|---|---|---|
| GET | `/projects` | all | list (role-filtered; filter/sort/search/paginate) | 200 |
| POST | `/projects` | P | create (wizard) | 201 |
| GET | `/projects/{id}` | member/A | base project | 200 |
| GET | `/projects/{id}/workspace` | member/A | aggregated workspace (overview+status+recs+uploads+team) | 200 |
| PATCH | `/projects/{id}` | P,A | update meta | 200 |
| DELETE | `/projects/{id}` | A | soft-delete | 204 |
| GET | `/projects/{id}/timeline` | member/A | stage history | 200 |
| POST | `/projects/{id}/transitions` | role-by-stage | guarded stage transition `{toStage,note}` | 200/409 |
| GET | `/projects/{id}/members` | member/A | team list | 200 |
| POST | `/projects/{id}/members` | P,A | assign member(s) (accepts bulk) | 201 |
| DELETE | `/projects/{id}/members/{userId}` | P,A | unassign | 204 |

> Convenience transition aliases (each validates guard + emits audit + transition): `POST /projects/{id}/start-production` (P), `POST /projects/{id}/send-to-qa` (Q,P), `POST /projects/{id}/reject` (Q) → back to IN_PRODUCTION.

### 3.6 Tasks
| Method | Path | Roles | Purpose | Success |
|---|---|---|---|---|
| GET | `/tasks` | all | my tasks / queue (filter `status`,`projectId`; paginated) | 200 |
| GET | `/projects/{id}/tasks` | member | project tasks | 200 |
| POST | `/projects/{id}/tasks` | P,A | create task | 201 |
| PATCH | `/tasks/{id}` | assignee/P/A | update status | 200 |

### 3.7 Documents & Files (versioning)
| Method | Path | Roles | Purpose | Success |
|---|---|---|---|---|
| GET | `/projects/{id}/documents` | member | list (filter `docType`) | 200 |
| POST | `/projects/{id}/documents` | P,D | create logical doc + upload v1 (`multipart`) | 201 |
| POST | `/projects/{id}/manuscript` | P | convenience: upload manuscript (DOCX/PDF) | 201 |
| POST | `/projects/{id}/pdf` | D | upload final production PDF (PDF/X-4/UA) ✱→PDF_REVIEW | 201 |
| GET | `/documents/{id}` | member | document + current version | 200 |
| GET | `/documents/{id}/versions` | member | version history | 200 |
| POST | `/documents/{id}/versions` | P,D | upload new version | 201 |
| POST | `/documents/{id}/versions/{vid}:setCurrent` | P,A | rollback/set current | 200 |
| GET | `/file-versions/{id}/download` | member | stream/redirect download | 200/302 |
| POST | `/projects/{id}/uploads:init` | P,D | (S3-ready) request presigned upload | 200 |

> Validation: manuscript accepts `DOCX,PDF` (Phase 1); PDF endpoint accepts `application/pdf` ≤ 500 MB; size/type rejected with 422.

### 3.8 Production Package
| Method | Path | Roles | Purpose | Success |
|---|---|---|---|---|
| GET | `/projects/{id}/package` | D,P,A | package + items | 200 |
| POST | `/projects/{id}/package` | P | assemble package (no IDML gen) | 201 |
| POST | `/projects/{id}/package/items` | P | add/curate item | 201 |
| DELETE | `/projects/{id}/package/items/{itemId}` | P | remove item | 204 |
| GET | `/projects/{id}/package/download` | D,P,A | download `.zip` (increments count) | 200/302 |

### 3.9 AI Orchestration (client-facing)
| Method | Path | Roles | Purpose | Success |
|---|---|---|---|---|
| POST | `/projects/{id}/analysis` | P | start manuscript analysis ✱ | 202 |
| GET | `/projects/{id}/analysis` | member | latest result (metrics, composition, headings, risks, team, complexity) | 200 |
| POST | `/projects/{id}/preflight` | Q,P | start PDF preflight ✱ | 202 |
| GET | `/projects/{id}/preflight` | member | latest run + checks + issues | 200 |
| GET | `/ai-jobs/{id}` | member | job status/progress | 200 |
| GET | `/projects/{id}/events` | member | **SSE** progress + stage stream | 200 (text/event-stream) |
| GET | `/projects/{id}/recommendations` | member | AI recommendations | 200 |
| POST | `/projects/{id}/recommendations/{rid}:accept` | P,Q | accept → routes to person | 200 |
| POST | `/projects/{id}/recommendations/{rid}:dismiss` | P,Q | dismiss | 200 |

### 3.10 AI Assistant
| Method | Path | Roles | Purpose | Success |
|---|---|---|---|---|
| GET | `/projects/{id}/assistant/thread` | member | thread + messages | 200 |
| POST | `/projects/{id}/assistant/messages` | member | ask scoped question → assistant reply | 200/202 |

### 3.11 Preflight Issues & QA Sign-off
| Method | Path | Roles | Purpose | Success |
|---|---|---|---|---|
| GET | `/projects/{id}/issues` | Q,P,A | issues (filter `severity`,`status`,`category`; paginated) | 200 |
| GET | `/issues/{id}` | Q,P,A | issue detail + decision history | 200 |
| POST | `/issues/{id}/decision` | Q | `{decision: ACCEPT_FIX\|SEND_BACK\|COMMENT, comment}` | 201 |
| POST | `/issues:bulk-decision` | Q | **bulk** triage `{issueIds[], decision}` | 200 |
| POST | `/projects/{id}/signoff` | Q | approve+e-sign `{decision, qualityScore, signature}` → COMPLETED / reject→IN_PRODUCTION | 201 |
| GET | `/projects/{id}/approvals` | member/A | approval history | 200 |
| GET | `/projects/{id}/signoffs` | member/A | sign-off records | 200 |

### 3.12 Comments
| Method | Path | Roles | Purpose | Success |
|---|---|---|---|---|
| GET | `/projects/{id}/comments` | member | list (filter `contextType`,`contextId`; paginated) | 200 |
| POST | `/projects/{id}/comments` | member | add `{body,parentId?,contextType,contextId}` | 201 |
| PATCH | `/comments/{id}` | author | edit | 200 |
| DELETE | `/comments/{id}` | author/A | soft-delete | 204 |

### 3.13 Notifications
| Method | Path | Roles | Purpose | Success |
|---|---|---|---|---|
| GET | `/notifications` | all | feed (filter `unread`; paginated) | 200 |
| GET | `/notifications/unread-count` | all | badge count | 200 |
| POST | `/notifications/{id}:read` | owner | mark read | 204 |
| POST | `/notifications:read-all` | owner | **bulk** mark all read | 204 |
| GET | `/notification-preferences` | all | per-type channels | 200 |
| PATCH | `/notification-preferences` | all | update | 200 |

### 3.14 Audit & Activity
| Method | Path | Roles | Purpose | Success |
|---|---|---|---|---|
| GET | `/audit-events` | A | global log (filter `projectId`,`actorId`,`entityType`,`from`,`to`; paginated) | 200 |
| GET | `/projects/{id}/activity` | member/A | project activity feed (audit projection) | 200 |
| GET | `/audit-events:export?format=csv` | A | CSV export | 200 (text/csv) |

### 3.15 Reports
| Method | Path | Roles | Purpose | Success |
|---|---|---|---|---|
| GET | `/reports/overview?range=6m` | A,P,Q | KPIs (turnaround, on-time %, avg AI confidence, QA pass %) | 200 |
| GET | `/reports/throughput?range=6m` | A,P,Q | titles completed/month | 200 |
| GET | `/reports/workload-by-imprint` | A,P,Q | share of active projects by imprint | 200 |

---

## 4. Key request/response DTOs (validation rules)

- **CreateProjectRequest** — `title` (NotBlank, 3–250), `imprintId` (uuid, exists), `publicationType` (enum), `isbn` (pattern, optional, unique), `brief` (≤4000), `priority` (enum), `dueDate` (date ≥ today), `memberUserIds` (each exists). → 422 with field errors; 409 on duplicate ISBN.
- **AnalysisResponse** — `overallConfidence` (0–100), `summary`, `language`, `complexityScore` (0–100), `complexityLabel`, `estimatedWorkingDays`, `metrics[] {key,value,confidence}`, `composition[] {segment,percentage}`, `headings[] {level,count}`, `risks[] {severity,title,description}`, `suggestedTeam[] {userId,role,matchScore,rationale}`.
- **PreflightResponse** — `overallScore` (0–100), `passed`, `standard`, `checks[] {key,result,detail}`, `issues[] {id,category,severity,title,recommendation,pageRef,source,status}`, `totalIssues`, `highSeverity`.
- **IssueDecisionRequest** — `decision` (enum ACCEPT_FIX|SEND_BACK|COMMENT), `comment` (required when SEND_BACK/COMMENT).
- **SignoffRequest** — `decision` (APPROVED|REJECTED), `qualityScore` (0–100), `signature` (NotBlank — typed name / attestation token); APPROVED requires all high-severity issues triaged → else 409.
- **TransitionRequest** — `toStage` (enum), `note` (optional); invalid transition → 409 with `code: INVALID_TRANSITION`.
- **AssistantMessageRequest** — `content` (NotBlank, ≤4000).
- **UserSummary / NavModel** (from `/me`) — `id,fullName,email,roles[],permissions[],avatarInitials,avatarColor,nav[]` (role-shaped nav items).

---

## 5. AI Service Integration Contract (`/internal/v1`, FastAPI)

Authenticated with `X-Internal-Key`. Spring Boot `ai` module is the only caller. AI service is stateless.

| Method | Path | Purpose | Request → Response |
|---|---|---|---|
| POST | `/internal/v1/analyze/manuscript` | parse + structure manuscript | `{jobId, storageKey, docType, projectContext}` → `AnalysisResult` (metrics, composition, headings, risks, complexity, suggestedTeam, overallConfidence, raw) |
| POST | `/internal/v1/preflight/pdf` | lightweight PDF checks + findings | `{jobId, storageKey, standard}` → `{overallScore, passed, checks[], issues[]}` |
| POST | `/internal/v1/assistant/chat` | scoped Q&A | `{projectContext, history[], message}` → `{reply, tokens}` |
| GET | `/internal/v1/health` | readiness | → `{status, provider, model}` |

**Progress callback (FastAPI → Spring Boot):**
| POST | `/internal/v1/ai-jobs/{jobId}/progress` | AI service posts progress | `{progressPct, status, partialResult?}` → 204 |

Spring Boot relays these to clients over SSE. Flow: client POSTs `/projects/{id}/analysis` → Spring creates `AiJob(QUEUED)` (202) → calls FastAPI `analyze/manuscript` on a worker thread → FastAPI streams progress via the callback → Spring persists final `analysis_*` rows and emits `AnalysisCompleted` → SSE notifies client. Provider/model captured on the job; Claude is pluggable behind FastAPI's `LLMProvider`.

---

## 6. Traceability Matrix — Screen → API → DB Tables → AI Service

| Screen (prototype) | Primary API(s) | DB tables | AI service |
|---|---|---|---|
| SCR-01 Login | `POST /auth/login`, `GET /me` | users, roles, user_roles, permissions | — |
| SCR-02 Dashboard | `GET /dashboard`, `GET /projects`, `GET /search` | projects, project_members, workflow_stages, ai_recommendations, report_snapshots | — |
| SCR-03 Create project (wizard) | `POST /projects`, `GET /imprints`, `GET /users` | projects, imprints, project_members | — |
| SCR-04 Upload manuscript | `POST /projects/{id}/manuscript` | documents, file_versions | — |
| SCR-05 AI analysis | `POST /projects/{id}/analysis` ✱, `GET /projects/{id}/analysis`, `GET /ai-jobs/{id}`, SSE | ai_jobs, analysis_results, analysis_metrics, analysis_composition, analysis_headings, analysis_risks, team_suggestions | `analyze/manuscript` (Claude) |
| SCR-06 Workspace (6 tabs) | `GET /projects/{id}/workspace`, `/timeline`, `/documents`, `/comments`, `/recommendations`, `/activity`, `/assistant/thread` | projects, project_stage_history, documents, file_versions, comments, ai_recommendations, audit_events, assistant_* | assistant chat |
| SCR-07 Production package | `GET /projects/{id}/package`, `POST .../package`, `GET .../package/download` | production_packages, package_items, documents, file_versions | — |
| SCR-08 In production (offline) | `GET /projects/{id}/workspace` (status), `POST .../transitions` | projects, project_stage_history, tasks | — |
| SCR-09 Upload PDF | `POST /projects/{id}/pdf` ✱ | documents, file_versions | — |
| SCR-10 AI PDF review (preflight) | `POST /projects/{id}/preflight` ✱, `GET .../preflight`, SSE | ai_jobs, preflight_runs, preflight_checks, qa_issues | `preflight/pdf` (Claude) |
| SCR-11 QA sign-off | `GET /projects/{id}/issues`, `POST /issues/{id}/decision`, `POST /issues:bulk-decision`, `POST /projects/{id}/signoff` | qa_issues, qa_issue_decisions, qa_signoffs, approvals, project_stage_history | (recommendations) |
| SCR-12 Completed | `GET /projects/{id}/workspace`, `/timeline`, `/signoffs`, `/package/download` | projects, project_stage_history, qa_signoffs, approvals, production_packages | — |
| Reports | `GET /reports/overview`, `/throughput`, `/workload-by-imprint` | report_snapshots, projects, qa_signoffs, ai_jobs | — |
| Admin · Users & roles | `GET/POST/PATCH/DELETE /users`, `/roles` | users, roles, user_roles, permissions | — |
| Admin · Audit log | `GET /audit-events`, `:export` | audit_events | — |
| Notifications panel | `GET /notifications`, `:read-all`, `unread-count` | notifications, notification_preferences | — |
| AI Assistant | `GET /projects/{id}/assistant/thread`, `POST .../assistant/messages` | assistant_threads, assistant_messages, ai_jobs | `assistant/chat` (Claude) |

**Entity coverage check:** every DB table has at least one API (organizations → implicit single-tenant context/`/me`; report_snapshots → `/reports/*`; all others mapped above). ✅

---

## 7. Gaps, Inconsistencies & Decisions to confirm

1. **"Viewing as" role switcher** — in the prototype this swaps personas. In production this is **not** a real API; it's either (a) a dev/demo-only client toggle, or (b) true Admin **impersonation** (`POST /admin/impersonate/{userId}`, audited). *Recommend:* demo-only client toggle in Phase 1; real impersonation deferred. **Confirm.**
2. **Forgot/reset password** depends on email being wired (it is — notifications). Kept in Phase 1; SSO deferred.
3. **Global search (`/search`)** scope — Phase 1 limit to projects + manuscript filenames (Postgres ILIKE/trigram), not full document text. **Confirm acceptable.**
4. **Large PDF upload (≤500 MB)** through the API is a known strain (architecture R2). Phase 1 = streamed multipart; `/uploads:init` presigned path is stubbed now and becomes primary with the S3 swap. **Flagged, not blocking.**
5. **SSE vs WebSocket** — spec uses SSE; verify Render proxy doesn't buffer (architecture R8). Fallback: short-poll `GET /ai-jobs/{id}`.
6. **Reports live vs snapshot** — `/reports/*` reads `report_snapshots` (precomputed). Need a scheduled job to populate them; until then reports can compute live via views. **Note for build plan.**
7. **Bulk member assign** — folded into `POST /projects/{id}/members` accepting array; no separate bulk endpoint needed.
8. **AI job cancellation** — not in prototype; optional `POST /ai-jobs/{id}:cancel` deferred unless desired.
9. **Idempotency-Key** optional now; recommended for `/projects` and upload endpoints to avoid double-submit.
10. **Pagination style** — page-based chosen for MVP simplicity; cursor-based is the future-scale upgrade for `audit-events`/`notifications`.

No blocking inconsistencies. Every screen maps to APIs, every entity is served, and the AI integration is fully specified. Consistent with the approved architecture and database design.
```
