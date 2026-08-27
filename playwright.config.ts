import { defineConfig } from "@playwright/test";

// E2E-Config fuer das counter-Projekt (Ticket 71).
// Der e2e-runner entdeckt diese Config nur, wenn sie an der Repo-Wurzel liegt
// und startet `npx playwright test` von dort. Der webServer bootet die Vite-App
// aus counter/ (`npm --prefix counter run dev`). `@playwright/test` ist bewusst
// nur als Dependency der Root-Config installiert (nicht in counter/), damit sie
// im Runner exakt einmal geladen wird.
export default defineConfig({
  testDir: "./e2e",
  use: { baseURL: "http://127.0.0.1:5173" },
  webServer: {
    command: "npm --prefix counter run dev -- --host 0.0.0.0 --port 5173",
    url: "http://127.0.0.1:5173",
    reuseExistingServer: false,
    timeout: 120_000,
  },
});
