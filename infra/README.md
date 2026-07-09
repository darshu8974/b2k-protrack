# Protrack — Infrastructure, CI & Deployment

This directory holds the plumbing that runs Protrack locally and in the cloud: a local
Docker Compose stack, the Render blueprint for the two backend services, and per-app
environment templates. Continuous integration lives in [`.github/workflows/`](../.github/workflows).

> Scope: this covers **how the system is built, run locally, and deployed**. The full
> release runbook and version tagging are a later task (Sprint 7.7).

---

## Topology

| Component | Tech | Local | Cloud |
|---|---|---|---|
| `web` | React 19 + Vite | Vite dev server (`npm run dev`, port 5173) | **Vercel** |
| `api` | Spring Boot 3 (Java 21) | Docker Compose (port 8080) | **Render** (Docker) |
| `ai` | FastAPI (Python 3.12) | Docker Compose (port 8000) | **Render** (Docker) |
| database | PostgreSQL | Compose `postgres:16-alpine` | **Neon** (external managed) |
| file storage | local disk (Phase 1) | named Docker volume | Render persistent disk |

The web app is deployed separately (Vercel) and is intentionally **not** part of the Compose
stack — run it with Vite and point `VITE_API_URL` at `http://localhost:8080`.

---

## Local stack (Docker Compose)

Brings up Postgres + API + AI with local, non-secret defaults. The API runs Flyway
migrations on boot; the AI service uses the deterministic **mock** provider (no Claude key).

```bash
# from the repo root
docker compose -f infra/docker-compose.yml up --build
```

- API:    http://localhost:8080  (health: `/api/v1/health`)
- AI:     http://localhost:8000  (health: `/internal/v1/health`)
- Postgres: `localhost:5432`  (db/user/pass: `protrack` / `protrack` / `protrack`)

Then run the web app against it:

```bash
cd apps/web && echo "VITE_API_URL=http://localhost:8080" > .env.local && npm run dev
```

Tear down (add `-v` to also drop the Postgres + storage volumes):

```bash
docker compose -f infra/docker-compose.yml down
```

To exercise **real Claude** locally, set `AI_PROVIDER=claude` and `ANTHROPIC_API_KEY=…` on
the `ai` service (e.g. via an env file or by editing the compose `environment` block).

---

## Continuous integration

Path-filtered GitHub Actions — each workflow runs only when its app changes:

| Workflow | Trigger paths | Steps |
|---|---|---|
| [`ci-web`](../.github/workflows/ci-web.yml) | `apps/web/**` | `npm ci` → lint → Vitest → test typecheck → build |
| [`ci-api`](../.github/workflows/ci-api.yml) | `apps/api/**` | `./gradlew build` (JUnit + Testcontainers) · plus a job that boots the API on a throwaway Postgres and fails on API→spec contract drift |
| [`ci-ai`](../.github/workflows/ci-ai.yml) | `apps/ai/**` | `pip install .[dev]` → ruff → mypy → pytest (mock provider) |
| [`ci-contract`](../.github/workflows/ci-contract.yml) | `packages/api-contract/**` | OpenAPI lint (`validate`) → spec→types drift (`check`) → typecheck |

The Testcontainers context test is Docker-skipped on machines without Docker but **runs in
CI** (GitHub's ubuntu runners provide Docker). AI tests mock the LLM provider — no key, no cost.

---

## Cloud deployment

### Backend services → Render (Blueprint)

[`render/render.yaml`](render/render.yaml) declares `protrack-api` and `protrack-ai` as Docker
web services (built from `apps/api/Dockerfile` and `apps/ai/Dockerfile`). To deploy: Render
Dashboard → **Blueprints** → **New Blueprint Instance** → point at this repo.

Secrets are never committed. After the first apply, set the `sync: false` values in the Render
dashboard:

- **api**: `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD` (Neon), `PROTRACK_AI_BASE_URL`
  (the `protrack-ai` URL), `PROTRACK_AI_INTERNAL_KEY`. `PROTRACK_JWT_SECRET` is auto-generated.
- **ai**: `AI_INTERNAL_KEY` (must equal the API's `PROTRACK_AI_INTERNAL_KEY`),
  `AI_SPRING_CALLBACK_BASE_URL` (the `protrack-api` URL), and `ANTHROPIC_API_KEY` if using Claude.

### Database → Neon

Create a Neon Postgres project and use its **direct** connection string (host without
`-pooler`, `?sslmode=require`) for the API's `DATABASE_URL` (JDBC form). Flyway migrates on
API boot.

### Web → Vercel

Import `apps/web` as a Vercel project (Vite preset). Set `VITE_API_URL` to the `protrack-api`
URL. Vercel builds `npm run build` and serves the SPA.

---

## Environment reference

Templates (no secrets) live in [`env/`](env):

- [`env/api.env.example`](env/api.env.example) — Spring API
- [`env/ai.env.example`](env/ai.env.example) — FastAPI AI service
- [`env/web.env.example`](env/web.env.example) — React web app

The per-app `apps/*/.env.example` files remain the source for app-local development; these
`infra/env/` templates focus on the containerised / deployed context. The internal key
(`PROTRACK_AI_INTERNAL_KEY` on the API ⇄ `AI_INTERNAL_KEY` on the AI service) must match.
