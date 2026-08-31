import { afterEach, describe, expect, it, vi } from "vitest";
import { screen, waitFor } from "@testing-library/react";

vi.mock("@/lib/api", async (importOriginal) => {
  const actual = await importOriginal<typeof import("@/lib/api")>();
  return { ...actual, calculate: vi.fn() };
});

/**
 * Kontext-Test für den Anwendungs-Einstiegspunkt (DoD: Startklasse).
 *
 * main.tsx führt beim Import Seiteneffekte aus (createRoot + render auf #root).
 * Deshalb wird vor dem Import ein #root-Element bereitgestellt und das Modul
 * erst anschließend dynamisch geladen. Der Test verifiziert, dass die Anwendung
 * tatsächlich mountet und die Taschenrechner-Oberfläche rendert.
 *
 * Der Timeout ist bewusst großzügig bemessen: Unter Strykers Command-Runner
 * laufen viele Sandbox-Instanzen gleichzeitig, was den Bootstrap verlangsamt.
 */
describe("main.tsx — Anwendungs-Bootstrap", () => {
  afterEach(() => {
    document.getElementById("root")?.remove();
    vi.resetModules();
  });

  it(
    "mountet die App und rendert die Taschenrechner-Oberfläche",
    async () => {
      const root = document.createElement("div");
      root.id = "root";
      document.body.appendChild(root);

      expect(screen.queryByText("Taschenrechner")).not.toBeInTheDocument();

      // Module erst nach dem Anlegen von #root laden (main.tsx rendert beim Import).
      await import("@/main");

      expect(await screen.findByText("Taschenrechner")).toBeInTheDocument();
      // Die große Anzeige startet bei 0.
      await waitFor(() =>
        expect(document.querySelector("p.text-3xl")).toHaveTextContent("0"),
      );
      // Eine Zifferntaste ist sichtbar gerendert.
      expect(screen.getByRole("button", { name: "7" })).toBeInTheDocument();
    },
    20_000,
  );
});
