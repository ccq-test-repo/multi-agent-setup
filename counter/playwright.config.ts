import { defineConfig } from "@playwright/test";

// E2E-Config fuer die Zaehler-Komponente (Ticket #71).
// Liegt unter counter/, damit die Datei-Scope des Tickets (nur counter/)
// eingehalten bleibt. Der e2e-runner wird mit `projectDir: "counter"` auf
// diesen Ordner gerichtet. Der webServer bootet die Vite-App direkt aus
// counter/ (kein `--prefix` noetig, da die Config im Projektordner liegt).
export default defineConfig({
  testDir: "./e2e",
  use: { baseURL: "http://127.0.0.1:5173" },
  webServer: {
    command: "npm run dev -- --host 0.0.0.0 --port 5173",
    url: "http://127.0.0.1:5173",
    reuseExistingServer: false,
    timeout: 120_000,
  },
});
