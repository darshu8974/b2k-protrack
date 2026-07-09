import { fileURLToPath } from "node:url";
import path from "node:path";

import openapiTS, { astToString } from "openapi-typescript";

export const PACKAGE_ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
export const SPEC_PATH = path.join(PACKAGE_ROOT, "openapi.json");
export const TYPES_PATH = path.join(PACKAGE_ROOT, "src", "generated", "api.ts");

/** Default location of the running API's OpenAPI document. Override the base with PROTRACK_API_URL. */
const API_BASE = (process.env.PROTRACK_API_URL ?? "http://localhost:8080").replace(/\/$/, "");
export const DEFAULT_SPEC_URL = `${API_BASE}/v3/api-docs`;

const BANNER = `/**
 * AUTO-GENERATED — DO NOT EDIT BY HAND.
 * TypeScript types for the Protrack HTTP contract, generated from openapi.json.
 * Regenerate with \`npm run generate\`; \`npm run check\` fails CI if this file drifts.
 */
`;

/** Generate the TypeScript type source from the committed OpenAPI document. */
export async function generateTypes() {
  const ast = await openapiTS(new URL(`file://${SPEC_PATH.replace(/\\/g, "/")}`));
  return BANNER + astToString(ast);
}
