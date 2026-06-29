# Protrack — AI Publishing Operating System

An enterprise platform that orchestrates the STEM publishing workflow **around** Adobe InDesign —
intake, AI manuscript analysis, production hand-off, PDF preflight, QA sign-off, and audit — while
page layout stays human and offline in InDesign. AI proposes; people decide.

> **Status:** Phase 1 MVP · Implementation phase · Sprint 0 (foundations).

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

> Detailed setup is added per app during Sprint 0. The local stack runs via
> `infra/docker-compose.yml` (PostgreSQL + api + ai).

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for branching, commit conventions, and testing.
