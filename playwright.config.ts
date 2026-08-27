import { defineConfig } from "@playwright/test";

// E2E-Config fuer das counter-Projekt (ticket 71).
// Die Tests liegen unter counter/e2e; der Runner entdeckt diese Config nur,
// wenn sie an der Repo-Wurzel liegt. Der webServer bootet die Vite-App via
// `npm --prefix counter run dev` (die counter/ besitzt das App-package.json).
export default defineConfig({
  testDir: "./counter/e2e",
  use: { baseURL: "http://127.0.0.1:5173" },
  webServer: {
    command: "npm --prefix counter run dev -- --host 0.0.0.0 --port 5173",
    url: "http://127.0.0.1:5173",
    reuseExistingServer: false,
    timeout: 120_000,
  },
});
