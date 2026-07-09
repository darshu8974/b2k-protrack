# @protrack/api-contract

Single source of truth for the Protrack HTTP contract shared between the Spring API (`apps/api`)
and the React web app (`apps/web`).

- **`openapi.json`** — the OpenAPI 3.0 document, exported verbatim from the running Spring API
  (springdoc, served at `/v3/api-docs`).
- **`src/generated/api.ts`** — TypeScript types generated from `openapi.json` by
  [`openapi-typescript`](https://github.com/openapi-ts/openapi-typescript). **Never hand-edit.**

## Scripts

| Script | What it does |
|---|---|
| `npm run export` | Fetch `/v3/api-docs` from a running API and overwrite `openapi.json`. Requires the API up (`cd apps/api && ./gradlew bootRun`). Override the source with `PROTRACK_API_URL`. |
| `npm run generate` | Regenerate `src/generated/api.ts` from `openapi.json`. |
| `npm run validate` | OpenAPI lint — validate `openapi.json` against the OpenAPI schema. |
| `npm run check` | CI gate: validate the spec **and** assert the committed types match a fresh generation (fails on drift). |

## Contract workflow

1. Change an API DTO/endpoint in `apps/api`.
2. Boot the API, then `npm run export` to refresh `openapi.json`.
3. `npm run generate` to refresh the types.
4. Commit both. CI (`ci-contract`) runs `npm run check` and blocks merges when the spec and the
   generated types have drifted apart.

The `check` gate catches "spec changed but types not regenerated". Detecting **API → spec** drift
additionally requires `export` against a live API, which runs in the API's CI job where the server
is available.
