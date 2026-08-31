// @vitest-environment node
import { describe, expect, it } from "vitest";
import { loadConfigFromFile, type ConfigEnv } from "vite";

/**
 * Test für den Vite-Dev-Proxy (Sub-Issue #93).
 *
 * Die Akzeptanzkriterien verlangen, dass frontend/vite.config.ts einen
 * server.proxy-Eintrag enthält, der `/api` an das Backend
 * (http://localhost:8080) weiterleitet. Dadurch laufen alle Backend-Pfade
 * same-origin über den Dev-Server und CORS greift gar nicht erst.
 *
 * Der Test lädt die echte Konfigurationsdatei über Vite's loadConfigFromFile
 * (statt die Logik nachzubauen) und prüft genau das geforderte Proxy-Mapping.
 */
async function loadViteConfig() {
  const root = process.cwd();
  const configEnv: ConfigEnv = { command: "serve", mode: "development" };
  const loaded = await loadConfigFromFile(configEnv, undefined, root);
  if (!loaded) {
    throw new Error("vite.config.ts konnte nicht geladen werden");
  }
  return loaded.config;
}

describe("vite.config.ts — Dev-Proxy", () => {
  it("definiert einen server.proxy-Eintrag für /api", async () => {
    const config = await loadViteConfig();
    const proxy = config.server?.proxy;
    expect(proxy).toBeDefined();
    expect(proxy).toHaveProperty("/api");
  });

  it("leitet /api an das Backend http://localhost:8080 weiter", async () => {
    const config = await loadViteConfig();
    const proxy = config.server?.proxy;

    const apiEntry = proxy?.["/api"];
    // Der Vertrag aus Sub-Issue #82: Das Backend läuft unter localhost:8080.
    // Der Eintrag kann entweder ein nackter String oder eine ProxyOptions
    // (mit .target) sein — beide Formen werden unterstützt.
    const apiTarget =
      typeof apiEntry === "string"
        ? apiEntry
        : (apiEntry as { target?: string } | undefined)?.target;

    expect(apiTarget).toBe("http://localhost:8080");
  });
});
