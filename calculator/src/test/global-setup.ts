// Patch React.act before react-dom/test-utils loads.
// React 19.2 in CJS mode exports `act` as undefined from the main module.
// The react-dom/test-utils module calls `React.act(callback)` which fails
// when act is undefined. This patch restores act before any test runs.

import { fileURLToPath } from "node:url";
import { createRequire } from "node:module";
import path from "node:path";

export function setup() {
  // Create a require that resolves from the project root
  const req = createRequire(
    import.meta.url ? path.dirname(fileURLToPath(import.meta.url)) : __dirname,
  );

  // Patch react module's exports to include act
  // This runs BEFORE any test files or their imports
  const reactModulePath = req.resolve("react", {
    paths: [process.cwd()],
  });

  // Use dynamic import to get the ESM act export
  // and assign it to the CJS exports
  import("react").then((reactMod) => {
    if (typeof reactMod.act === "function") {
      // act IS available via ESM, so patch it into CJS
      const cjsReact = req("react");
      cjsReact.act = reactMod.act;
    }
  });
}

export function teardown() {}
