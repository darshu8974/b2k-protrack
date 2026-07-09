// Regenerate src/generated/api.ts from the committed openapi.json.
import { mkdir, writeFile } from "node:fs/promises";
import path from "node:path";

import { generateTypes, TYPES_PATH } from "./lib.mjs";

const source = await generateTypes();
await mkdir(path.dirname(TYPES_PATH), { recursive: true });
await writeFile(TYPES_PATH, source, "utf8");
console.log(`Generated ${path.relative(process.cwd(), TYPES_PATH)}`);
