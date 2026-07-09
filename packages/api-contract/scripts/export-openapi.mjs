// Fetch the OpenAPI document from a running API instance and write it to openapi.json (stable,
// pretty-printed so diffs are reviewable). Run this whenever the API contract changes, then
// regenerate types with `npm run generate`. Override the source with PROTRACK_API_URL.
import { writeFile } from "node:fs/promises";

import { DEFAULT_SPEC_URL, SPEC_PATH } from "./lib.mjs";

const url = DEFAULT_SPEC_URL;

try {
  const response = await fetch(url);
  if (!response.ok) {
    throw new Error(`GET ${url} -> ${response.status} ${response.statusText}`);
  }
  const doc = await response.json();
  await writeFile(SPEC_PATH, JSON.stringify(doc, null, 2) + "\n", "utf8");
  console.log(`Exported OpenAPI ${doc.openapi} (${Object.keys(doc.paths ?? {}).length} paths) -> openapi.json`);
} catch (error) {
  console.error(`Failed to export OpenAPI from ${url}`);
  console.error(`  ${error.message}`);
  console.error("Is the API running? Start it with: cd apps/api && ./gradlew bootRun");
  process.exit(1);
}
