import { test, expect } from "@playwright/test";

// E2E-Tests abgeleitet aus den Akzeptanzkriterien des Ticket #71
// (Zaehler-Komponente, Vite + React + TypeScript, Ordner counter/).

test.describe("Zaehler-Komponente", () => {
  test("zeigt Ueberschrift 'Zaehler' und startet bei 0", async ({ page }) => {
    await page.goto("/");

    const heading = page.getByRole("heading", { name: "Zaehler" });
    await expect(heading).toBeVisible();

    const count = page.getByTestId("count");
    await expect(count).toHaveText("0");
  });

  test("erhoeht den Stand um 1 pro Klick auf 'Erhoehen'", async ({ page }) => {
    await page.goto("/");

    const count = page.getByTestId("count");
    const increment = page.getByRole("button", { name: "Erhoehen" });

    await increment.click();
    await expect(count).toHaveText("1");

    await increment.click();
    await expect(count).toHaveText("2");

    await increment.click();
    await expect(count).toHaveText("3");
  });

  test("setzt den Stand mit 'Zuruecksetzen' auf 0 zurueck", async ({ page }) => {
    await page.goto("/");

    const count = page.getByTestId("count");
    const increment = page.getByRole("button", { name: "Erhoehen" });
    const reset = page.getByRole("button", { name: "Zuruecksetzen" });

    // Erst erhoehen, dann zuruecksetzen.
    await increment.click();
    await increment.click();
    await increment.click();
    await expect(count).toHaveText("3");

    await reset.click();
    await expect(count).toHaveText("0");

    // Zuruecksetzen auf 0 bleibt stabil, weitere Klicks aendern nichts.
    await reset.click();
    await expect(count).toHaveText("0");
  });

  test("Buttons sind per zugaenglichem Namen bedienbar (sichtbare Beschriftung)", async ({
    page,
  }) => {
    await page.goto("/");

    // Zugreifbarer Name = sichtbare Beschriftung (kein ueberschreibendes aria-label).
    await expect(
      page.getByRole("button", { name: "Erhoehen" })
    ).toBeVisible();
    await expect(
      page.getByRole("button", { name: "Zuruecksetzen" })
    ).toBeVisible();

    // Beide Buttons sind vom Typ "button" und ausloesbar (nicht disabled).
    const increment = page.getByRole("button", { name: "Erhoehen" });
    const reset = page.getByRole("button", { name: "Zuruecksetzen" });
    await expect(increment).toBeEnabled();
    await expect(reset).toBeEnabled();
  });
});
