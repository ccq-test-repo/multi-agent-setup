import { beforeEach, describe, expect, it, vi } from "vitest";
import { render, screen, within } from "@testing-library/react";
import { waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { Calculator } from "@/components/calculator";
import { calculate } from "@/lib/api";

vi.mock("@/lib/api", async (importOriginal) => {
  const actual = await importOriginal<typeof import("@/lib/api")>();
  return {
    ...actual,
    calculate: vi.fn(),
  };
});

const mockCalculate = vi.mocked(calculate);

function ariaButton(name: string) {
  return screen.getByRole("button", { name });
}

/** Große Anzeige (aktueller Wert/Ergebnis). Wird per css query benutzt, da "42" auch im Verlauf erscheinen kann. */
function resultDisplay(): HTMLElement {
  const el = document.querySelector("p.text-3xl");
  if (!el) throw new Error("Hauptanzeige (p.text-3xl) nicht gefunden");
  return el as HTMLElement;
}

/** Wartet, bis die Hauptanzeige den übergebenen Ergebniswert zeigt. */
async function awaitResult(expected: string): Promise<void> {
  await waitFor(() => expect(resultDisplay()).toHaveTextContent(expected));
}
/** Anzeige-Symbole der Operator-Tasten (entspricht den aria-labels der Buttons). */
const OP_LABEL: Record<string, string> = {
  ADD: "+",
  SUBTRACT: "−",
  MULTIPLY: "×",
  DIVIDE: "÷",
  PERCENT: "%",
};

beforeEach(() => {
  mockCalculate.mockReset();
});

describe("Calculator — Eingabe und Anzeige", () => {
  it("zeigt initial 0 und den leeren Verlauf", () => {
    render(<Calculator />);
    expect(resultDisplay()).toHaveTextContent("0");
    expect(screen.getByText("Noch keine Rechnung durchgeführt")).toBeInTheDocument();
  });

  it("hängt Ziffern an den ersten Operanden an (Selbst ein 7), ohne Rechnung", async () => {
    const user = userEvent.setup();
    render(<Calculator />);
    await user.click(ariaButton("7"));
    await user.click(ariaButton("6"));
    expect(resultDisplay()).toHaveTextContent("76");
    expect(mockCalculate).not.toHaveBeenCalled();
  });
});

describe("Calculator — Operator-Buttons per Klick (Klick-Pfad)", () => {
  it.each(["ADD", "SUBTRACT", "MULTIPLY", "DIVIDE", "PERCENT"])(
    "setzt den Operator über Klick auf %s und zeigt den Ausdruck",
    async (op) => {
      const user = userEvent.setup();
      render(<Calculator />);
      await user.click(ariaButton("7"));
      await user.click(ariaButton(OP_LABEL[op]));
      // Links + Operator gesetzt; der Ausdruck zeigt das Operatorsymbol.
      expect(
        screen.getByText((_, el) => el?.textContent === `7 ${OP_LABEL[op]} …`),
      ).toBeInTheDocument();
      expect(mockCalculate).not.toHaveBeenCalled();
    },
  );

  it("Klick-Pfad: 7, ×, 6, = ruft die API mit MULTIPLY auf und zeigt 42", async () => {
    mockCalculate.mockResolvedValueOnce(42);
    const user = userEvent.setup();
    render(<Calculator />);

    await user.click(ariaButton("7"));
    await user.click(ariaButton("×"));
    await user.click(ariaButton("6"));
    await user.click(ariaButton("Gleich"));

    expect(mockCalculate).toHaveBeenCalledWith({ left: 7, operator: "MULTIPLY", right: 6 });
    await awaitResult("42");
  });
});

describe("Calculator — Ergebnis und Verlauf", () => {
  it("legt nach einer Rechnung einen Verlauf-Eintrag an (7 + 6 = 13)", async () => {
    mockCalculate.mockResolvedValueOnce(13);
    const user = userEvent.setup();
    render(<Calculator />);

    await user.click(ariaButton("7"));
    await user.click(ariaButton("+"));
    await user.click(ariaButton("6"));
    await user.click(ariaButton("Gleich"));

    await awaitResult("13");
    const verlauf = screen.getByRole("region", { name: "Verlauf" });
    expect(within(verlauf).getByText("= 13")).toBeInTheDocument();
    expect(screen.queryByText("Noch keine Rechnung durchgeführt")).not.toBeInTheDocument();
  });

  it("begrenzt den Verlauf auf die letzten 5 Einträge", async () => {
    mockCalculate.mockImplementation(async ({ left, right }) => left + right);
    const user = userEvent.setup();
    render(<Calculator />);

    // 6 aufeinanderfolgende Additionen über den Klick-Pfad.
    for (let i = 1; i <= 6; i++) {
      await user.click(ariaButton(String(i)));
      await user.click(ariaButton(OP_LABEL.ADD));
      await user.click(ariaButton("1"));
      await user.click(ariaButton("Gleich"));
      await awaitResult(String(i + 1));
    }

    const verlauf = screen.getByRole("region", { name: "Verlauf" });
    expect(within(verlauf).getAllByRole("listitem")).toHaveLength(5);
  });
});

describe("Calculator — Fehleranzeige und Wiederholen", () => {
  it("zeigt die Backend-Message bei Division durch null (5 ÷ 0)", async () => {
    mockCalculate.mockRejectedValueOnce(new Error("Division durch null ist nicht definiert."));
    const user = userEvent.setup();
    render(<Calculator />);

    await user.click(ariaButton("5"));
    await user.click(ariaButton("÷"));
    await user.click(ariaButton("0"));
    await user.click(ariaButton("Gleich"));

    expect(await screen.findByText("Division durch null ist nicht definiert.")).toBeInTheDocument();
    // Rolling alert ist vorhanden und Anwendung bleibt bedienbar.
    expect(screen.getByRole("alert")).toBeInTheDocument();
    expect(ariaButton("7")).toBeEnabled();
  });

  it("zeigt bei nicht erreichbarem Backend einen Fehler mit Wiederholen-Button und wiederholt auf Klick", async () => {
    mockCalculate
      .mockRejectedValueOnce(new Error("Server-Fehler (HTTP 404)"))
      .mockResolvedValueOnce(21);
    const user = userEvent.setup();
    render(<Calculator />);

    await user.click(ariaButton("7"));
    await user.click(ariaButton("×"));
    await user.click(ariaButton("3"));
    await user.click(ariaButton("Gleich"));

    expect(await screen.findByRole("alert")).toHaveTextContent("Server-Fehler (HTTP 404)");
    const retry = screen.getByRole("button", { name: /Wiederholen/i });
    await user.click(retry);

    await awaitResult("21");
    expect(mockCalculate).toHaveBeenCalledTimes(2);
  });
});

describe("Calculator — Löschen und Sonderfunktionen", () => {
  it("bricht eine begonnene Eingabe über C ab und setzt auf 0 zurück", async () => {
    const user = userEvent.setup();
    render(<Calculator />);

    await user.click(ariaButton("7"));
    await user.click(ariaButton("6"));
    await user.click(ariaButton("Löschen"));

    expect(resultDisplay()).toHaveTextContent("0");
    expect(mockCalculate).not.toHaveBeenCalled();
  });

  it("setzt einen Operator zurück, wenn C nach einer Operator-Wahl gedrückt wird", async () => {
    const user = userEvent.setup();
    render(<Calculator />);

    await user.click(ariaButton("7"));
    await user.click(ariaButton("+"));
    await user.click(ariaButton("Löschen"));

    await user.click(ariaButton("3"));
    expect(resultDisplay()).toHaveTextContent("3");
  });
});
