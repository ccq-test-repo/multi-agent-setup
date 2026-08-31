import { expect, test } from "@playwright/test";

/**
 * E2E-Test für den Vite-Dev-Proxy (Sub-Issue #93).
 *
 * Der Dev-Server leitet `/api/*` an das Backend (http://localhost:8080) weiter,
 * statt es selbst zu bedienen. Ohne Proxy würde Vite die SPA-Fallback-HTML
 * (index.html) für unbekannte Pfade zurückgeben — mit Proxy wird der Request
 * an das Backend durchgereicht (im Runner läuft kein Java-Backend, daher
 * kommt kein 200, aber eben auch nicht mehr die Vite-SPA-HTML).
 *
 * Referenzfall aus dem Ticket: http://127.0.0.1:5173/api/calculate erreicht das
 * Backend, statt einen Dev-Server-404 zu liefern.
 */
test("GET /api/health geht durch den Dev-Proxy und nicht an die SPA-Fallback-HTML", async ({
  request,
  baseURL,
}) => {
  const response = await request.get(`${baseURL}/api/health`);

  // Nicht-negativ-Status: Der Request wurde vom Proxy behandelt (405/5xx vom
  // fehlenden Backend) — entscheidend ist, dass NICHT die Vite-SPA-HTML kommt.
  const contentType = response.headers()["content-type"] ?? "";

  // Ohne Proxy liefert Vite die SPA-Fallback-HTML (text/html) mit 200.
  // Mit Proxy wird an das Backend durchgereicht → nicht text/html.
  expect(contentType).not.toContain("text/html");
});

test("GET /api/calculate wird vom Proxy behandelt (kein SPA-Fallback)", async ({
  request,
  baseURL,
}) => {
  const response = await request.get(`${baseURL}/api/calculate`);
  const contentType = response.headers()["content-type"] ?? "";

  // Auch ein nicht vorhandener API-Pfad darf nicht den SPA-Fallback liefern —
  // er muss an den Proxy/Backend gehen.
  expect(contentType).not.toContain("text/html");
});
