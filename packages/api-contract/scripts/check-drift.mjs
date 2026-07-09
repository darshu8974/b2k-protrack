// Contract drift gate (for CI). Two checks:
//   1. OpenAPI lint  — openapi.json is a structurally valid OpenAPI document.
//   2. Type-gen drift — the committed src/generated/api.ts matches a fresh generation from
//      openapi.json. A mismatch means someone edited openapi.json without running `generate`
//      (or hand-edited the generated types). CI fails so the contract and types stay in lockstep.
//
// To detect API -> spec drift as well, run `npm run export` against a live API first (that step
// belongs in the API's CI job, where the server is up) and commit any change.
import { readFile } from "node:fs/promises";
import path from "node:path";

import SwaggerParser from "@apidevtools/swagger-parser";

import { generateTypes, SPEC_PATH, TYPES_PATH } from "./lib.mjs";

let failed = false;

// 1. OpenAPI lint.
try {
  const api = await SwaggerParser.validate(SPEC_PATH);
  console.log(`✓ OpenAPI ${api.openapi} valid (${Object.keys(api.paths ?? {}).length} paths)`);
} catch (error) {
  console.error(`✗ OpenAPI validation failed: ${error.message}`);
  process.exit(1);
}

// 2. Type-gen drift.
let committed;
try {
  committed = await readFile(TYPES_PATH, "utf8");
} catch {
  console.error(`✗ Generated types missing at ${path.relative(process.cwd(), TYPES_PATH)}. Run: npm run generate`);
  process.exit(1);
}

const fresh = await generateTypes();
if (normalize(committed) === normalize(fresh)) {
  console.log("✓ Generated types are in sync with openapi.json");
} else {
  console.error("✗ Generated types are stale — openapi.json changed but src/generated/api.ts was not regenerated.");
  console.error("  Fix with: npm run generate  (then commit the result)");
  failed = true;
}

process.exit(failed ? 1 : 0);

/** Compare on content, ignoring trailing-whitespace / line-ending differences across platforms. */
function normalize(text) {
  return text.replace(/\r\n/g, "\n").replace(/[ \t]+$/gm, "").trimEnd();
}
