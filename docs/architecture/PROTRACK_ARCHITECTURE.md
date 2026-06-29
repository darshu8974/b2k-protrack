# Protrack — AI Publishing Operating System
## Phase 1 MVP — Software Architecture Blueprint

> Status: Approved architecture, pre-implementation. No code.
> Author role: Lead Enterprise Solution Architect.
> Scope: Phase 1 deployable MVP demonstrating the complete AI-assisted publishing workflow.

---

## 0. Approved Phase 1 Decisions (context)

| Area | Decision |
|---|---|
| Goal | Deployable MVP showing the full AI-assisted publishing workflow |
| Style | Modular Monolith (Spring Boot) + detached FastAPI AI Service |
| Frontend | React + TypeScript + Material UI → **Vercel** |
| Backend core | Spring Boot (Java) → **Render** |
| AI service | FastAPI (Python), Claude API → **Render** |
| Database | PostgreSQL → **Neon** |
| Storage | Local disk now → AWS S3 later (behind a storage abstraction) |
| Roles | Administrator, Project Manager, Designer, QA Engineer (RBAC). Customer Portal = Phase 2 |
| Docs | DOCX + PDF (pluggable). XML/LaTeX/EPUB/IDML = future |
| InDesign | Not integrated. Offline. System prepares assets + ingests PDF/IDML |
| Preflight | Lightweight validation + AI-assisted QA. Advanced = future |
| Auth | Spring Security + JWT + RBAC. SSO = future |
| Tenancy | Single-tenant, multi-tenant-ready |
| Governance | Immutable audit, version history, approval history |
| Notifications | In-app + email. Slack/Teams/webhooks = future |

---

## 1. Solution Architecture (the big picture)

Three deployable units plus managed data services:

```
                         ┌──────────────────────────────┐
                         │   React + MUI SPA  (Vercel)   │
                         │  role-shaped UI, JWT in memory│
                         └──────────────┬───────────────┘
                                        │ HTTPS (REST) + SSE/WebSocket (progress)
                                        ▼
                ┌────────────────────────────────────────────────┐
                │     Spring Boot Modular Monolith (Render)       │
                │  Auth · Projects · Workflow · Files · AI Orch.  │
                │  Preflight/QA · Packages · Notifications · Audit│
                │  · Reporting · Assistant gateway                │
                └───────┬─────────────────────────┬──────────────┘
                        │ JDBC                     │ internal REST (signed key)
                        ▼                          ▼
            ┌────────────────────┐     ┌──────────────────────────────┐
            │ PostgreSQL (Neon)  │     │  FastAPI AI Service (Render)  │
            │ state, audit, jobs │     │  parse DOCX/PDF · Claude calls│
            └────────────────────┘     │  preflight checks · assistant │
                        ▲              └───────────────┬──────────────┘
                        │                              │ HTTPS
            ┌────────────────────┐                     ▼
            │ File Storage        │            ┌────────────────┐
            │ local disk → S3     │            │  Claude API     │
            └────────────────────┘            └────────────────┘
```

**Key architectural principles**
- **Modular monolith** for all business logic: one deployable Spring Boot app, internally partitioned into modules with explicit interfaces (package-by-module, no cross-module entity access — modules talk via service interfaces and domain events).
- **AI is a separate service** (FastAPI) because the AI/document toolchain is Python-native (python-docx, pdf libraries, Claude SDK). It is stateless and replaceable; the monolith treats it as a provider behind an interface.
- **Asynchrony for long work.** Manuscript analysis and PDF preflight are minutes-long. They run as tracked **jobs**; the UI subscribes to **progress events** (SSE/WebSocket) rather than blocking.
- **Storage and AI provider are abstractions**, so "local disk → S3" and "Claude → +other LLMs" are configuration swaps, not rewrites.
- **The workflow state machine is the backbone** — every screen is a view of a stage, every action a guarded transition that emits an audit event.

---

## 2. Business Architecture

**Business capability map** (what the platform must do):

```
Intake & Onboarding   →  Project setup, manuscript ingestion
AI Document Intelligence → structure, metrics, complexity, risk, team suggestion
Production Hand-off   →  assemble production assets, designer download
Offline Production    →  (external) InDesign layout by humans
Print-Readiness QA    →  PDF ingestion, lightweight preflight, AI-assisted QA
Approval & Governance →  human gates, e-sign attestation, immutable audit
Delivery & Archive    →  final deliverables, sealed audit trail
Insight & Reporting   →  pipeline status, throughput, quality, AI performance
```

**Value chain & owners**

| Stage | Business outcome | Primary role | AI contribution |
|---|---|---|---|
| Intake | Title registered, manuscript stored | PM | — |
| AI Analysis | Structure + readiness understood | PM (approves) | Manuscript analysis |
| Design Prep | Production assets ready to hand off | PM | Recommendations |
| In Production | Pages laid out (offline) | Designer | — (boundary) |
| PDF Review | Print-readiness assessed | QA/PM | Preflight + QA reasoning |
| QA Sign-off | Title approved, e-signed | QA | Issue triage suggestions |
| Completed | Deliverables sealed + archived | All | Report generation |

**Business rules (invariant):** InDesign never integrated; designers offline; AI advises only; human approval mandatory at each gate; every action audited.

---

## 3. Application Architecture (modular monolith internals)

Spring Boot, package-by-module. Each module = `api` (controllers/DTOs) + `domain` (entities/services) + `spi` (outbound ports) + events. Cross-module calls go through published service interfaces or domain events on an in-process event bus (Spring `ApplicationEventPublisher`).

```
com.protrack
├── identity        // users, roles, JWT, RBAC, login
├── project         // project + imprint + membership/assignment
├── workflow        // state machine, stages, transitions, guards
├── files           // upload, storage abstraction, versioning
├── ai              // AI orchestration: jobs, progress, AI-service client
├── analysis        // manuscript analysis results (read model)
├── preflight       // pdf submissions, checks, issues
├── qa              // issue decisions, sign-off, e-sign attestation
├── packaging       // production-asset bundle assembly + download
├── notification    // in-app + email
├── audit           // append-only immutable event log
├── reporting       // aggregates / analytics read models
├── assistant       // scoped chat gateway to AI service
└── shared          // security, error handling, events, config
```

**Module responsibilities (selected)**
- **identity** — registration/seed, login, JWT issue/validate, role mapping, method-security annotations.
- **workflow** — owns the canonical `ProjectStage` enum and the transition table (who/when/guard). Other modules *request* transitions; workflow validates and emits `StageChanged`.
- **ai** — accepts an analysis/preflight request, creates an `AiJob`, calls the FastAPI service, relays progress, persists the structured result, emits completion events.
- **qa** — records per-issue decisions (accept AI fix / send back / comment), computes quality score, captures the e-signature attestation, triggers the `QA_SIGNOFF → COMPLETED` transition.
- **audit** — subscribes to all domain events; writes immutable rows. No update/delete API.

**API surface (representative REST, all under `/api`)**

```
POST   /auth/login                          → JWT
GET    /me                                  → profile + role + nav model
GET    /projects                            → list (role-filtered)
POST   /projects                            → create (PM)            [INTAKE]
GET    /projects/{id}                        → workspace aggregate
POST   /projects/{id}/manuscript             → presign/upload (PM)
POST   /projects/{id}/analyze                → start AI analysis (PM) [→AI_ANALYSIS]
GET    /projects/{id}/analysis               → analysis result
POST   /projects/{id}/start-production        → (PM)                  [→DESIGN_PREP]
GET    /projects/{id}/package                 → production assets (Designer)
POST   /projects/{id}/pdf                      → upload final PDF (Designer)[→PDF_REVIEW]
POST   /projects/{id}/preflight               → run preflight
GET    /projects/{id}/preflight               → checklist + issues
POST   /projects/{id}/issues/{iid}/decision    → accept/reject/comment (QA)
POST   /projects/{id}/signoff                  → approve + e-sign (QA) [→COMPLETED]
GET    /projects/{id}/audit                    → audit trail
POST   /projects/{id}/assistant                → scoped chat
GET    /notifications                          → in-app feed
GET    /reports                                → analytics
SSE    /projects/{id}/events                    → live job/stage progress
```

---

## 4. AI Architecture

**Topology:** Spring Boot `ai` module ⇄ FastAPI AI Service ⇄ Claude API. The AI service is **stateless**; all results persist in Postgres via the monolith.

```
ai module (Java)                 FastAPI AI Service (Python)
 ─ AiJob (DB, status/progress)    ─ /analyze/manuscript  (DOCX|PDF → structure)
 ─ AiServiceClient (HTTP)   ───►  ─ /preflight/pdf        (PDF → checks + findings)
 ─ progress relay (SSE)     ◄───  ─ /assistant/chat       (project ctx → answer)
 ─ provider-agnostic DTOs         ─ ProviderRouter → Claude (pluggable)
                                  ─ Parsers: python-docx, pdfplumber/pypdf
```

**AI Service abstraction (pluggable):** a `LLMProvider` interface inside FastAPI (`ClaudeProvider` first; OpenAI/Gemini later). Prompts/templates versioned. The monolith never calls Claude directly — only the AI service does — so provider, key rotation, and prompt changes are isolated.

**Phase 1 AI capabilities**
1. **Manuscript analysis** (DOCX/PDF): deterministic parsing for counts (pages, headings H1/H2/H3, figures, tables, equations, references) + Claude for semantic structuring, notation/terminology consistency, reading-complexity scoring, production-effort estimate, suggested team, and risk flags. Every field carries a **confidence score**.
2. **Lightweight PDF preflight**: deterministic checks (page count/geometry sanity, embedded fonts presence, image DPI sampling, text-extraction/overflow heuristics, basic tag/accessibility presence) + Claude to phrase findings, severity, and recommended fixes mapped to the source manuscript.
3. **Scoped assistant**: retrieval over the project's own stored artifacts (analysis JSON, issues, status) — "RAG-lite" using Postgres-stored context in Phase 1 (vector DB = future). Answers status/issue/compliance questions with suggested prompts.

**Guardrails:** outputs are **advisory only**, always paired with confidence + a human decision; no AI output mutates workflow state without a human-triggered transition; AI service calls are time-bounded, retried, and degrade gracefully (job marked `FAILED`, UI offers retry).

**Job lifecycle:** `QUEUED → RUNNING(progress%) → SUCCEEDED|FAILED`. Progress events stream to the SPA. Synchronous Claude latency is hidden behind the async job + SSE.

---

## 5. Human-in-the-Loop Workflow

Every AI step is sandwiched by a human gate. The state machine:

```
            (PM)            (PM approves)       (PM)              (Designer)
 INTAKE ─────────► AI_ANALYSIS ─────────► DESIGN_PREP ─────────► IN_PRODUCTION
   ▲  upload mss     AI analyzes,            assets ready,         offline InDesign
   │                 human reviews           human hands off       (external boundary)
   │                                                                    │ upload PDF
   │                                                                    ▼
 COMPLETED ◄──────── QA_SIGNOFF ◄──────────── PDF_REVIEW ◄──────────────┘
   (sealed,   QA approves+e-signs   QA triages issues,    AI preflight runs,
    archived)                       accept-fix/send-back  human reviews findings
```

**Gate definitions**

| Transition | Trigger role | Human decision | AI input | Audit event |
|---|---|---|---|---|
| INTAKE→AI_ANALYSIS | PM | "Analyze with AI" | starts analysis | `AnalysisStarted` |
| AI_ANALYSIS→DESIGN_PREP | PM | review + "Start Production" | analysis result | `AnalysisApproved` |
| DESIGN_PREP→IN_PRODUCTION | PM | hand off to designer | recommendations | `HandedOff` |
| IN_PRODUCTION→PDF_REVIEW | Designer | upload final PDF | — | `PdfUploaded` |
| PDF_REVIEW→QA_SIGNOFF | QA/PM | "Send to QA" after preflight | preflight findings | `PreflightCompleted` |
| QA_SIGNOFF→COMPLETED | QA | per-issue triage + **e-sign** | issue recommendations | `SignedOff` |
| (reject) PDF_REVIEW/QA→IN_PRODUCTION | QA | "Reject & send back" | — | `SentBack` |

**Human override is always available**: any AI recommendation can be accepted, modified, or dismissed; "send back" returns control to the designer. Nothing is auto-approved.

---

## 6. Module Breakdown (build units)

| # | Module | Owns (entities) | Depends on | Phase-1 deliverable |
|---|---|---|---|---|
| 1 | identity | User, Role, RefreshToken | shared | login, JWT, RBAC, seeded users for 4 roles |
| 2 | project | Project, Imprint, ProjectMember | identity | CRUD, 3-step create wizard backing, role-filtered lists |
| 3 | workflow | (stage state on Project), Transition log | project, audit | state machine + guards + events |
| 4 | files | FileAsset, FileVersion | project, storage SPI | upload/download, versioning, local-disk store |
| 5 | ai | AiJob | files, analysis, preflight | job orchestration + AI-service client + SSE relay |
| 6 | analysis | AnalysisResult, MetricSet, RiskFlag, TeamSuggestion | ai | persisted analysis read model + API |
| 7 | preflight | PdfSubmission, PreflightRun, Check, Issue | ai, files | checklist + issues |
| 8 | qa | IssueDecision, SignOff (e-sign) | preflight, workflow | triage, quality score, sign-off |
| 9 | packaging | ProductionPackage, PackageItem | files, project | assemble + download bundle (no IDML gen) |
| 10 | notification | Notification | (events) | in-app feed + email (SMTP) |
| 11 | audit | AuditEvent (append-only) | (all events) | immutable log + CSV export |
| 12 | reporting | (read models / views) | project, qa, ai | dashboard + reports aggregates |
| 13 | assistant | AssistantMessage | ai, project | scoped chat gateway |
| 14 | shared | security, events, errors, config | — | cross-cutting infra |

---

## 7. Data Flow

**A) Manuscript analysis (async, streamed)**
```
PM uploads DOCX ──► files: store + version ──► PM clicks Analyze
  └► ai: create AiJob(QUEUED) ──► call FastAPI /analyze/manuscript
        FastAPI: parse counts (python-docx/pdf) + Claude semantic pass
        FastAPI streams progress ──► ai relays via SSE ──► SPA animates
        FastAPI returns structured result ──► analysis: persist + confidence
        workflow stays AI_ANALYSIS until PM approves ──► audit: AnalysisCompleted
```

**B) PDF preflight + QA**
```
Designer uploads PDF ──► files: store+version ──► workflow → PDF_REVIEW
  └► ai: AiJob ──► FastAPI /preflight/pdf (deterministic checks + Claude findings)
        results ──► preflight: PreflightRun + Issues(severity,page,recommendation)
        SPA shows checklist + issues table
        QA decides per issue (accept/send-back/comment) ──► qa: IssueDecision
        QA e-signs ──► qa: SignOff ──► workflow → COMPLETED ──► audit: SignedOff
```

**C) Cross-cutting:** every state change and decision publishes a domain event → `audit` persists immutable row + `notification` fans out in-app/email. Reporting reads aggregates.

---

## 8. Component Diagram

```
┌───────────────────────────── Frontend (Vercel) ─────────────────────────────┐
│ React+MUI · Router · TanStack Query · Zustand(role/wizard) · Recharts · SSE  │
│ Screens: Login·Dashboard·Wizard·Upload·Analysis·Workspace·Package·Production │
│          ·UploadPDF·Preflight·QA·Completed·Reports·Admin·Assistant           │
└───────────────────────────────┬──────────────────────────────────────────────┘
                                 │ REST + SSE (JWT bearer)
┌───────────────────────────── Spring Boot (Render) ───────────────────────────┐
│ [Security filter: JWT + RBAC + CORS]                                         │
│ identity │ project │ workflow │ files │ ai │ analysis │ preflight │ qa │      │
│ packaging │ notification │ audit │ reporting │ assistant │ shared(eventbus)   │
│ Ports:  StoragePort ─► LocalDiskAdapter (→ S3Adapter later)                  │
│         AiServicePort ─► FastAPI HTTP client (signed internal key)           │
│         MailPort ─► SMTP                                                      │
└───────┬───────────────────────────────────┬──────────────────────────────────┘
        │ JDBC/HikariCP                       │ HTTPS internal
        ▼                                     ▼
 PostgreSQL (Neon)                  ┌──── FastAPI AI Service (Render) ────┐
 + Flyway migrations                │ routers: analyze · preflight · chat │
 File store (disk→S3)               │ parsers · LLMProvider→Claude        │
                                    └──────────────────────────────────────┘
```

---

## 9. Technology Architecture

| Layer | Technology | Notes |
|---|---|---|
| Frontend | React 18 + TypeScript, Vite, Material UI v5, React Router v6, TanStack Query, Zustand, Recharts | role-shaped nav from `/me` claim |
| Realtime | SSE (Phase 1) over WebSocket simplicity | job/stage progress |
| Backend | Java 21, Spring Boot 3.x (Web, Security, Data JPA, Validation, Mail), Flyway, HikariCP | modular monolith |
| AI service | Python 3.12, FastAPI, Uvicorn, Anthropic SDK, python-docx, pdfplumber/pypdf | stateless |
| LLM | Claude API (Anthropic) behind `LLMProvider` | pluggable |
| DB | PostgreSQL (Neon), Flyway-managed schema | single-tenant, tenant-ready columns |
| Storage | Local disk now → AWS S3 (behind `StoragePort`) | presigned-style download |
| Auth | Spring Security, JWT (access + refresh), BCrypt | RBAC, method security |
| Email | SMTP (e.g. provider relay) | in-app + email notifications |
| Build/CI | Gradle/Maven (BE), npm+Vite (FE), Docker for AI service | |
| Hosting | Vercel (FE), Render (BE + AI), Neon (DB) | |

**Cross-cutting:** centralized error model, request/correlation IDs, structured logging, config via environment variables, OpenAPI (springdoc) for the REST contract.

---

## 10. Security Architecture

- **AuthN:** username/password → JWT access (short-lived) + refresh (rotated). BCrypt password hashing. SSO/OIDC deferred (Phase 1 keeps an auth abstraction so Keycloak/Azure AD slot in later).
- **AuthZ:** RBAC with 4 roles enforced at the API (Spring Security method security) *and* reflected in the role-shaped nav. Workflow transitions are role-guarded server-side (never trust the client).
- **Tenancy:** single tenant now; entities carry a `tenant_id`/`org_id` column reserved for future isolation; queries already scoped through a single accessor to ease the future switch.
- **Data protection:** TLS in transit (all hosts HTTPS); secrets only in env/secret stores (DB URL, JWT secret, Claude key, internal AI key) — never in code. File access authorized per-project membership.
- **Service-to-service:** Spring Boot ↔ FastAPI authenticated with a **shared internal API key/HMAC**; the AI service is not publicly callable for business endpoints.
- **Audit & non-repudiation:** append-only `audit_event` (no update/delete path); QA **e-signature** captured as an attestation record (signer identity, timestamp, hash of the signed artifact) — compliance-shaped, full SOC 2 / 21 CFR Part 11 deferred.
- **Input/abuse:** payload validation, file type/size limits (DOCX ~tens of MB, PDF up to 500 MB), AI prompt-injection mitigations (treat manuscript text as untrusted; system prompts isolate instructions), rate limiting on AI endpoints.

---

## 11. Deployment Architecture

```
 Developer ──► Git ──► CI
                      ├─ FE build ──► Vercel (CDN, preview + prod)
                      ├─ BE image/jar ──► Render (Spring Boot web service)
                      └─ AI image ──► Render (FastAPI web service, Docker)
 Managed:  Neon (PostgreSQL, branchable)        File store: Render disk → S3
 Config:   env vars/secrets per service          Migrations: Flyway on BE startup
```

- **Frontend (Vercel):** static SPA + CDN; environment points to BE base URL; preview deploys per PR.
- **Spring Boot (Render):** web service; Flyway runs migrations on boot; health check endpoint; horizontal-scale-ready (stateless except local disk — which moves to S3 to enable scaling).
- **FastAPI (Render):** Dockerized web service; holds the Claude key; autoscaled independently of the monolith.
- **PostgreSQL (Neon):** managed, connection-pooled; separate dev/prod branches.
- **Storage:** Phase 1 local disk on the BE instance (acceptable for MVP/demo); migration to S3 is a `StoragePort` adapter swap — and is the prerequisite for multi-instance scaling.
- **Environments:** dev → prod; secrets per environment; CORS locked to the Vercel origin.
- **Observability:** platform logs + health checks Phase 1; metrics/tracing as a fast-follow.

---

## 12. Risks, Assumptions, Recommendations

**Assumptions**
- A1. Demo-grade scale (handful of concurrent users, modest project counts).
- A2. Sample/seed data mirrors the prototype (one continuous demo title acceptable).
- A3. Claude API access/key available; network egress allowed from Render.
- A4. "Production package" in Phase 1 = curated/uploaded assets bundled for download — **no IDML/INDD generation**.
- A5. Files up to ~500 MB tolerated on local disk for the MVP demo.

**Risks & mitigations**
- R1 **Local disk is ephemeral on Render** (instance restarts/redeploys can lose files) and blocks scaling. → *Mitigate:* persistent disk for demo; prioritize S3 adapter early; never store the only copy of critical artifacts on ephemeral disk.
- R2 **Large-file upload through the API** strains the monolith. → *Mitigate:* stream uploads, enforce size caps; move to presigned direct-to-S3 with the storage swap.
- R3 **AI latency/variability & cost** (minutes-long, token cost, rate limits). → *Mitigate:* async jobs + SSE, timeouts, retries, caching of analysis results, graceful FAILED state.
- R4 **LLM accuracy / hallucination** in analysis & QA findings. → *Mitigate:* confidence scores, deterministic parsing for hard counts, mandatory human gate, store provenance.
- R5 **Prompt injection** via manuscript content. → *Mitigate:* treat document text as untrusted data, isolate system instructions, output schema validation.
- R6 **Cross-service auth gap** (FastAPI exposed). → *Mitigate:* internal API key/HMAC, network restrictions, no public business routes.
- R7 **Single-tenant shortcuts leaking into the model** make multi-tenancy painful later. → *Mitigate:* reserve `tenant_id` now, centralize data-access scoping.
- R8 **SSE/proxy buffering** on managed hosts can break live progress. → *Mitigate:* verify Render SSE behavior early; fall back to short-poll if needed.
- R9 **Scope creep from enterprise docs** (10 agents, vector DB, portals). → *Mitigate:* hold the approved Phase-1 line; everything else is explicitly Phase 2+.

**Recommendations (sequenced build order)**
1. Foundations: repo structure, shared module, security, DB + Flyway, seeded roles/users.
2. identity + project + workflow state machine (the backbone) + audit.
3. files (local store behind StoragePort) + versioning.
4. ai module + FastAPI service skeleton + Claude provider + analysis (DOCX/PDF) with SSE progress.
5. packaging (download) + preflight + qa sign-off (e-sign attestation).
6. notification (in-app + email) + reporting/dashboards + assistant.
7. Harden: S3 adapter, observability, rate limits; then open Phase 2 (Customer Portal, SSO, multi-tenancy, advanced preflight, vector RAG).

> Phase 2 candidates (documented, not built): Customer Portal + quote + client approval; SSO/OIDC; multi-tenancy; XML/LaTeX/EPUB/IDML + real package generation; advanced preflight (veraPDF/PDF-UA); vector DB RAG + Learning Repository; Slack/Teams/webhooks; SOC 2 / 21 CFR Part 11.
```
