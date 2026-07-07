# Protrack — AI Publishing Operating System

An enterprise platform that orchestrates the STEM publishing workflow **around** Adobe InDesign —
intake, AI manuscript analysis, production hand-off, PDF preflight, QA sign-off, and audit — while
page layout stays human and offline in InDesign. AI proposes; people decide.

> **Status:** Phase 1 MVP · Implementation phase · Sprint 6 complete (collaboration + intelligence —
> in-app/email notifications, project comments, a scoped AI assistant, a reports dashboard with real
> aggregates + a scheduled snapshot job, and admin user management with audit-log CSV export) on top
> of Sprint 5 (PDF preflight & QA sign-off), Sprint 4 (AI manuscript analysis), Sprint 3 (files/
> versioning/package), Sprint 2 (projects, workflow, dashboard, audit), and Sprint 1 (auth + RBAC).
>
> The AI service defaults to a deterministic **mock** provider, so the whole pipeline runs with no
> API key. Set `AI_PROVIDER=claude` + `ANTHROPIC_API_KEY` to use the real Claude API.

## Monorepo layout

```
protrack/
├── apps/
│   ├── web/                 # React 19 + TypeScript + Vite + Material UI   → Vercel
│   ├── api/                 # Spring Boot 3 (Java 21, modular monolith)    → Render
│   └── ai/                  # FastAPI (Python 3.12), Claude integration    → Render
├── packages/
│   └── api-contract/        # OpenAPI spec + generated TS types (shared web ↔ api)
├── infra/                   # docker-compose (local), Render blueprints, env templates
├── docs/
│   ├── architecture/        # the 6 approved architecture docs (source of truth)
│   └── IMPLEMENTATION_ROADMAP.md
└── .github/workflows/       # path-filtered CI per app
```

## Architecture (source of truth)

All implementation follows the approved documents in [`docs/architecture/`](docs/architecture/):

1. [Solution Architecture](docs/architecture/PROTRACK_ARCHITECTURE.md)
2. [Database Design](docs/architecture/PROTRACK_DATABASE_DESIGN.md)
3. [REST API Specification](docs/architecture/PROTRACK_API_SPECIFICATION.md)
4. [Backend Architecture](docs/architecture/PROTRACK_BACKEND_ARCHITECTURE.md)
5. [Frontend Architecture](docs/architecture/PROTRACK_FRONTEND_ARCHITECTURE.md)
6. [AI Service Architecture](docs/architecture/PROTRACK_AI_SERVICE_ARCHITECTURE.md)

Delivery is sequenced by the [Implementation Roadmap](docs/IMPLEMENTATION_ROADMAP.md) (Sprints 0–7).

## Tech stack

| Layer | Technology | Hosting |
|---|---|---|
| Frontend | React 19, TypeScript, Vite, Material UI, TanStack Query, Axios | Vercel |
| Backend | Java 21, Spring Boot 3, Spring Security (JWT), JPA, Flyway | Render |
| AI service | Python 3.12, FastAPI, Anthropic Claude, python-docx, pdfplumber | Render |
| Database | PostgreSQL | Neon |
| Storage | Local disk (Phase 1) → AWS S3 (future) | — |

## Local development

**Database:** the API connects to PostgreSQL (Neon) via environment variables — copy
`apps/api/.env.example` to `apps/api/.env` and fill in `DATABASE_URL` / `DATABASE_USERNAME` /
`DATABASE_PASSWORD` (never commit the `.env`).

**API** (Spring Boot, port 8080):

```bash
cd apps/api
set -a && source .env && set +a   # load DB env vars (Git Bash)
./gradlew bootRun                 # Flyway applies V1–V12 on startup
```

**AI service** (FastAPI, port 8000 — optional; needed for AI analysis and PDF preflight):

```bash
cd apps/ai
python -m venv .venv && ./.venv/Scripts/python -m pip install -e ".[dev]"   # first run
./.venv/Scripts/python -m uvicorn app.main:app --port 8000                  # AI_PROVIDER=mock by default
```

**Web** (React + Vite, port 5173 — the CORS-allowed origin):

```bash
cd apps/web
npm install
npm run dev
```

**Demo users** (all share the password `password`): `priya.anand@protrack.io` (PM),
`marcus.reed@protrack.io` (Designer), `lena.ortiz@protrack.io` (QA), `david.cho@protrack.io` (Admin).

**Tests:** `cd apps/api && ./gradlew test` (pure JUnit/Mockito unit tests — security, workflow,
ISBN, storage/upload, AI/analysis/preflight mappers, and the Sprint-6 services: notification fan-out,
comment threading, report aggregation + snapshot job, the assistant gateway, admin user CRUD, and
audit CSV export; the Testcontainers context test needs Docker and is skipped without it);
`cd apps/web && npm run build` (type-check); `cd apps/ai && ./.venv/Scripts/python -m pytest` (AI
service unit tests — parsers, PDF facts, providers, normalizer, analyze/preflight/assistant
pipelines). The AI service also runs `ruff check .` and `mypy app` clean.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for branching, commit conventions, and testing.
