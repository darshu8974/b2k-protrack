/**
 * Public entry for the Protrack API contract package. Re-exports the generated OpenAPI types
 * (`paths`, `components`, `operations`) so consumers can import from `@protrack/api-contract`.
 */
export type { paths, components, operations, webhooks } from "./generated/api";
