// Register hook: patch React.act into CJS cache before anything loads.
// This is loaded via vitest's `server.deps.fallbackCJS` or `server.deps.registerNodeLoader`.
const Module = require('module');
const origResolve = Module._resolveFilename;

Module._resolveFilename = function(request, parent) {
  const resolved = origResolve.call(this, request, parent);
  // When react is resolved, patch its exports to include act
  if (resolved.endsWith('react/index.js') || resolved.endsWith('react.js') || resolved.endsWith('react\\index.js')) {
    // Will be patched after require
  }
  return resolved;
};
