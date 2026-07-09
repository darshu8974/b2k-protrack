// OpenAPI lint: validate the committed openapi.json against the OpenAPI schema (structure + $ref
// resolution). Exits non-zero on an invalid document.
import SwaggerParser from "@apidevtools/swagger-parser";

import { SPEC_PATH } from "./lib.mjs";

try {
  const api = await SwaggerParser.validate(SPEC_PATH);
  const paths = Object.keys(api.paths ?? {}).length;
  const schemas = Object.keys(api.components?.schemas ?? {}).length;
  console.log(`OpenAPI ${api.openapi} valid: "${api.info.title}" ${api.info.version} (${paths} paths, ${schemas} schemas)`);
} catch (error) {
  console.error("OpenAPI validation failed:");
  console.error(`  ${error.message}`);
  process.exit(1);
}
