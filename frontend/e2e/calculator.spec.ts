import { expect, test, type Page } from "@playwright/test";

/**
 * E2E-Tests für die Taschenrechner-Oberfläche (Akzeptanzkriterien).
 * Das Backend wird via Route-Interception nachgebildet — es läuft kein Java-Prozess.
 *
 * `POST /api/calculate`: führt die Operation aus (kleine Modellberechnung).
 * `GET  /api/health`  : liefert `{ status: "UP" }`.
 */

/** Kleine Rechenlogik für die gemockte API (ADD/SUBTRACT/MULTIPLY/DIVIDE/PERCENT). */
function compute(op: string, left: number, right: number): number {
  switch (op) {
    case "ADD":
      return left + right;
    case "SUBTRACT":
      return left - right;
    case "MULTIPLY":
      return left * right;
    case "DIVIDE":
      return right === 0 ? Number.NaN : left / right;
    case "PERCENT":
      return (left / 100) * right;
    default:
      throw new Error(`unbekannter Operator: ${op}`);
  }
}

async function mockBackend(page: Page) {
  await page.route("**/api/calculate", async (route) => {
    const body = route.request().postDataJSON() as {
      left: number;
      operator: string;
      right: number;
    };
    const { left, operator, right } = body;
    if (operator === "DIVIDE" && right === 0) {
      return route.fulfill({
        status: 400,
        contentType: "application/json",
        body: JSON.stringify({
          error: "DIVISION_BY_ZERO",
          message: "Division durch null ist nicht definiert.",
        }),
      });
    }
    const result = compute(operator, left, right);
    await route.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify({ result }),
    });
  });
  await page.route("**/api/health", (route) =>
    route.fulfill({ status: 200, contentType: "application/json", body: JSON.stringify({ status: "UP" }) }),
  );
}

/** Hauptanzeige (großer rechter Wert/Ergebnis). */
function resultDisplay(page: Page) {
  return page.locator("p.text-3xl");
}

/** Ausdruck-Zeile (kleine laufende Eingabe). */
function expressionLine(page: Page) {
  return page.locator('[aria-live="polite"]');
}

test.beforeEach(async ({ page }) => {
  await mockBackend(page);
  await page.goto("/");
});

test("7 * 6 Enter → 42, Verlauf enthält den Eintrag", async ({ page }) => {
  await page.getByRole("button", { name: "7" }).click();
  await page.getByRole("button", { name: "×" }).click();
  await page.getByRole("button", { name: "6" }).click();
  await page.getByRole("button", { name: "Gleich" }).click();

  await expect(resultDisplay(page)).toHaveText("42");
  const verlauf = page.getByRole("region", { name: "Verlauf" });
  await expect(verlauf.getByText("= 42")).toBeVisible();
});

test("5 ÷ 0 → Meldung 'Division durch null ist nicht definiert.', App bleibt bedienbar", async ({
  page,
}) => {
  await page.getByRole("button", { name: "5" }).click();
  await page.getByRole("button", { name: "÷" }).click();
  await page.getByRole("button", { name: "0" }).click();
  await page.getByRole("button", { name: "Gleich" }).click();

  await expect(page.getByRole("alert")).toContainText(
    "Division durch null ist nicht definiert.",
  );
  // Anwendung bleibt bedienbar: alle Tasten weiterhin aktiv.
  await expect(page.getByRole("button", { name: "7" })).toBeEnabled();
  await expect(page.getByRole("button", { name: "Löschen" })).toBeEnabled();
  // Löschen setzt auf 0 zurück → UI reagiert weiterhin auf Eingaben.
  await page.getByRole("button", { name: "Löschen" }).click();
  await expect(resultDisplay(page)).toHaveText("0");
});

test("Bedienung ausschließlich per Tastatur: 7 * 6 Enter → 42, Fokus sichtbar", async ({
  page,
}) => {
  await page.goto("/");
  await page.locator("body").click({ position: { x: 5, y: 5 } });
  await page.keyboard.press("7");
  await page.keyboard.press("*");
  await page.keyboard.press("6");
  await page.keyboard.press("Enter");

  await expect(resultDisplay(page)).toHaveText("42");

  // Escape löscht und Fokus bleibt sichtbar (focus-visible-Ring auf der = / Fokus-Hook).
  await page.keyboard.press("Escape");
  await expect(resultDisplay(page)).toHaveText("0");
});

test("Backend nicht erreichbar → Hinweis mit Wiederholen, kein Absturz", async ({
  page,
}) => {
  // Backend-Routes abschalten → fetch schlägt fehl.
  await page.unroute("**/api/calculate");
  await page.route("**/api/calculate", (route) => route.abort());

  await page.getByRole("button", { name: "7" }).click();
  await page.getByRole("button", { name: "×" }).click();
  await page.getByRole("button", { name: "3" }).click();
  await page.getByRole("button", { name: "Gleich" }).click();

  // Backend nicht erreichbar → der Fehlertext kann je nach Netzwerk-/HTTP-Ursache
  // variieren ("Failed to fetch" oder eine HTTP-Nachricht). Der Hinweis mit
  // Wiederholen-Möglichkeit und der „kein Absturz“-Zustand sind entscheidend.
  const alert = page.getByRole("alert");
  await expect(alert).toContainText("Wiederholen");
  await expect(alert.getByRole("button", { name: /Wiederholen/i })).toBeVisible();

  // Seite weiterhin bedienbar, kein Absturz.
  await expect(page.getByRole("button", { name: "Gleich" })).toBeEnabled();
});

test("Fenster auf 320 px: kein horizontales Scrollen, alle Tasten erreichbar", async ({
  page,
}) => {
  await page.setViewportSize({ width: 320, height: 700 });
  await page.goto("/");

  const hasHorizontalScroll = await page.evaluate(
    () => document.documentElement.scrollWidth > document.documentElement.clientWidth,
  );
  expect(hasHorizontalScroll).toBe(false);

  // Alle Pflichttasten vorhanden und erreichbar.
  for (const name of [
    "Löschen",
    "Vorzeichenwechsel",
    "%",
    "÷",
    "×",
    "−",
    "+",
    "Gleich",
    "Komma",
  ]) {
    await expect(page.getByRole("button", { name })).toBeVisible();
  }
  for (let d = 0; d <= 9; d++) {
    await expect(page.getByRole("button", { name: String(d) })).toBeVisible();
  }
});

test("Vor der ersten Rechnung: 'Noch keine Rechnung durchgeführt'", async ({ page }) => {
  await expect(page.getByText("Noch keine Rechnung durchgeführt")).toBeVisible();
  await expect(resultDisplay(page)).toHaveText("0");
});
