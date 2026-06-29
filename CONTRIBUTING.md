# Contributing to Protrack

This repository follows the workflow defined in the approved
[Implementation Roadmap](docs/IMPLEMENTATION_ROADMAP.md). The six architecture documents in
[`docs/architecture/`](docs/architecture/) are the **single source of truth** — implementation must
not deviate from them without an explicit, approved change.

## Branching — trunk-based

- `main` is always deployable and protected: changes land via Pull Request with green CI and review.
- Short-lived feature branches off `main`:
  - `feat/<sprint>-<slug>` — new functionality (e.g. `feat/s1-auth-jwt`)
  - `fix/<slug>` — bug fixes
  - `chore/<slug>` — tooling/infra/maintenance
  - `docs/<slug>` — documentation
- Rebase or squash-merge to keep history linear.

## Commits — Conventional Commits

```
<type>(<scope>): <subject>
```

- **type:** `feat`, `fix`, `chore`, `docs`, `test`, `refactor`, `build`, `ci`, `perf`
- **scope (optional):** app/module, e.g. `api/identity`, `web/projects`, `ai/parsers`
- Examples:
  - `feat(api/identity): add JWT authentication filter`
  - `feat(web/auth): wire login form to /auth/login`
  - `test(ai/parsers): add DOCX fixture tests`

## Pull Requests

- Keep PRs small and scoped to a single roadmap task.
- Describe **what** and link the **sprint + task**; include the Definition of Done checklist.
- CI must pass; UI PRs attach a Vercel preview.
- At least one review before merge to `main`.

## Testing

| App | Layers | Tools |
|---|---|---|
| `apps/web` | unit/component, E2E | Vitest + React Testing Library, Playwright |
| `apps/api` | unit, integration | JUnit 5 + Mockito, Testcontainers (Postgres) |
| `apps/ai` | unit, schema/golden | pytest + httpx, parser fixtures |
| contract | drift check | OpenAPI lint + generated TS types |

- AI tests mock the LLM provider in CI; real-Claude smoke tests run in a separate opt-in job.
- No secrets in the repo. Configuration via environment variables only (`.env.example` templates).

## Definition of Done (baseline, every task)

Code reviewed and merged to `main`; tests passing in CI; lint/typecheck/format clean; no secrets
committed; docs/README updated where relevant; the slice runs locally via docker-compose and (from
Sprint 1) is deployed; traceability to the architecture docs noted in the PR.
