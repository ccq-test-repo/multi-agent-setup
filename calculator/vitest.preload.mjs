// Preload script for vitest: patches React.act into the CJS module cache
// before react-dom/test-utils loads.
// Used via vitest's `server.deps.entryFilter` or `--preload`
import { createRequire } from "module";

const req = createRequire(import.meta.url);

// Resolve and cache react module to patch it
const reactPath = req.resolve("react");
// Force-load react module
const reactModule = req("react");
if (typeof reactModule.act !== "function") {
  // react CJS doesn't have act. Import ESM version and patch
  const { act } = await import("react");
  if (typeof act === "function") {
    reactModule.act = act;
  }
}
