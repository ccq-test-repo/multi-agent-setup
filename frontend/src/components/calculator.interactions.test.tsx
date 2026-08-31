import { beforeEach, describe, expect, it, vi } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
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

/** Große Anzeige (aktueller Wert/Ergebnis). */
function resultDisplay(): HTMLElement {
  const el = document.querySelector("p.text-3xl");
  if (!el) throw new Error("Hauptanzeige (p.text-3xl) nicht gefunden");
  return el as HTMLElement;
}

/** Kleine Ausdruckszeile (laufender Ausdruck / Zwischenwert). */
function expressionLine(): HTMLElement {
  const el = document.querySelector<HTMLElement>("p.min-h-5");
  if (!el) throw new Error("Ausdruckszeile (p.min-h-5) nicht gefunden");
  return el as HTMLElement;
}

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

describe("Calculator — Tastatur-Steuerung", () => {
  it("Enter führt die Gleichung aus wie ein Klick auf =", async () => {
    mockCalculate.mockResolvedValueOnce(12);
    const user = userEvent.setup();
    render(<Calculator />);

    await user.click(ariaButton("7"));
    await user.click(ariaButton("+"));
    await user.click(ariaButton("5"));
    await user.keyboard("{Enter}");

    expect(mockCalculate).toHaveBeenCalledWith({ left: 7, operator: "ADD", right: 5 });
    await awaitResult("12");
  });

  it("Escape löscht die Eingabe wie C", async () => {
    const user = userEvent.setup();
    render(<Calculator />);

    await user.click(ariaButton("7"));
    await user.click(ariaButton("6"));
    await user.keyboard("{Escape}");

    expect(resultDisplay()).toHaveTextContent("0");
    expect(mockCalculate).not.toHaveBeenCalled();
  });

  it("Backspace entfernt das letzte Zeichen und kann auf 0 zurückfallen", async () => {
    const user = userEvent.setup();
    render(<Calculator />);

    await user.click(ariaButton("7"));
    await user.click(ariaButton("6"));
    await user.keyboard("{Backspace}");
    expect(resultDisplay()).toHaveTextContent("7");

    await user.keyboard("{Backspace}");
    expect(resultDisplay()).toHaveTextContent("0");
  });

  it("Backspace ohne Eingabe (0) bleibt bei 0", async () => {
    const user = userEvent.setup();
    render(<Calculator />);
    await user.keyboard("{Backspace}");
    expect(resultDisplay()).toHaveTextContent("0");
  });

  it("Ziffern über die Tastatur werden eingegeben", async () => {
    const user = userEvent.setup();
    render(<Calculator />);
    await user.keyboard("42");
    expect(resultDisplay()).toHaveTextContent("42");
    expect(mockCalculate).not.toHaveBeenCalled();
  });

  it("Operatoren über die Tastatur werden gesetzt", async () => {
    const user = userEvent.setup();
    render(<Calculator />);
    await user.keyboard("7+");
    expect(expressionLine()).toHaveTextContent("7 + …");
    expect(mockCalculate).not.toHaveBeenCalled();
  });

  it("Komma über die Tastatur startet einen Dezimalbruch (0.)", async () => {
    const user = userEvent.setup();
    render(<Calculator />);
    await user.keyboard("12,");
    expect(resultDisplay()).toHaveTextContent("12.");
  });

  it("ignoriert die Leertaste", async () => {
    const user = userEvent.setup();
    render(<Calculator />);
    await user.keyboard("7 { }");
    expect(resultDisplay()).toHaveTextContent("7");
    expect(mockCalculate).not.toHaveBeenCalled();
  });

  it("ignoriert Tasten mit gedrückter Modifier-Taste (Ctrl/Alt/Meta)", async () => {
    const user = userEvent.setup();
    render(<Calculator />);
    await user.keyboard("{Control>}a{/Control}");
    await user.keyboard("{Alt>}b{/Alt}");
    await user.keyboard("{Meta>}c{/Meta}");
    expect(resultDisplay()).toHaveTextContent("0");
    expect(mockCalculate).not.toHaveBeenCalled();
  });
});

describe("Calculator — Ziffern- und Dezimal-Eingabe", () => {
  it("ersetzt eine führende 0 durch die erste Ziffer statt anzuhängen", async () => {
    const user = userEvent.setup();
    render(<Calculator />);
    await user.click(ariaButton("0"));
    await user.click(ariaButton("5"));
    expect(resultDisplay()).toHaveTextContent("5");
  });

  it("verhindert doppelte Dezimalpunkte im ersten Operanden", async () => {
    const user = userEvent.setup();
    render(<Calculator />);
    await user.click(ariaButton("1"));
    await user.click(ariaButton("Komma"));
    await user.click(ariaButton("Komma"));
    expect(resultDisplay()).toHaveTextContent("1.");
  });

  it("verhindert mehrere Dezimalpunkte im zweiten Operanden", async () => {
    const user = userEvent.setup();
    render(<Calculator />);
    await user.click(ariaButton("2"));
    await user.click(ariaButton("+"));
    await user.click(ariaButton("3"));
    await user.click(ariaButton("Komma"));
    await user.click(ariaButton("Komma"));
    expect(resultDisplay()).toHaveTextContent("3.");
  });

  it("hängt nach einer Operator-Wahl ohne zweiten Operanden einen Dezimalpunkt an den linken Operanden an", async () => {
    const user = userEvent.setup();
    render(<Calculator />);
    await user.click(ariaButton("8"));
    await user.click(ariaButton("+"));
    await user.click(ariaButton("Komma"));
    // Ohne zweiten Operanden wird der Dezimalpunkt an die linke Zahl „8" gehängt.
    expect(resultDisplay()).toHaveTextContent("8.");
  });

  it("behält eine führende 0 im zweiten Operanden, solange nur weitere Nullen folgen", async () => {
    const user = userEvent.setup();
    render(<Calculator />);
    await user.click(ariaButton("5"));
    await user.click(ariaButton("+"));
    await user.click(ariaButton("0"));
    await user.click(ariaButton("0"));
    expect(resultDisplay()).toHaveTextContent("0");
  });

  it("ersetzt die führende 0 im zweiten Operanden durch eine Ziffer", async () => {
    const user = userEvent.setup();
    render(<Calculator />);
    await user.click(ariaButton("5"));
    await user.click(ariaButton("+"));
    await user.click(ariaButton("0"));
    await user.click(ariaButton("7"));
    expect(resultDisplay()).toHaveTextContent("7");
  });
});

describe("Calculator — Vorzeichenwechsel (±)", () => {
  it("negativiert den aktuellen Wert", async () => {
    const user = userEvent.setup();
    render(<Calculator />);
    await user.click(ariaButton("7"));
    await user.click(ariaButton("Vorzeichenwechsel"));
    expect(resultDisplay()).toHaveTextContent("-7");
  });

  it("entfernt das Vorzeichen, wenn der Wert bereits negativ ist", async () => {
    const user = userEvent.setup();
    render(<Calculator />);
    await user.click(ariaButton("7"));
    await user.click(ariaButton("Vorzeichenwechsel"));
    await user.click(ariaButton("Vorzeichenwechsel"));
    expect(resultDisplay()).toHaveTextContent("7");
  });

  it("tut nichts bei unvollständiger Eingabe (leer/0)", async () => {
    const user = userEvent.setup();
    render(<Calculator />);
    await user.click(ariaButton("Vorzeichenwechsel"));
    expect(resultDisplay()).toHaveTextContent("0");
  });

  it("negativiert ein berechnetes Ergebnis", async () => {
    mockCalculate.mockResolvedValueOnce(21);
    const user = userEvent.setup();
    render(<Calculator />);
    await user.click(ariaButton("7"));
    await user.click(ariaButton("×"));
    await user.click(ariaButton("3"));
    await user.click(ariaButton("Gleich"));
    await awaitResult("21");

    await user.click(ariaButton("Vorzeichenwechsel"));
    expect(resultDisplay()).toHaveTextContent("-21");
  });
});

describe("Calculator — Backspace auf dem zweiten Operanden", () => {
  it("löscht Zeichen aus dem zweiten Operanden", async () => {
    const user = userEvent.setup();
    render(<Calculator />);
    await user.click(ariaButton("7"));
    await user.click(ariaButton("+"));
    await user.click(ariaButton("6"));
    await user.click(ariaButton("5"));
    await user.keyboard("{Backspace}");
    expect(resultDisplay()).toHaveTextContent("6");
  });

  it("ersetzt einen vollständig gelöschten zweiten Operanden durch 0", async () => {
    const user = userEvent.setup();
    render(<Calculator />);
    await user.click(ariaButton("7"));
    await user.click(ariaButton("+"));
    await user.click(ariaButton("5"));
    await user.keyboard("{Backspace}");
    // Nach dem Löschen der einzigen Ziffer steht eine 0 als zweiter Operand.
    expect(expressionLine()).toHaveTextContent("7 + 0");
  });
});

describe("Calculator — Operator-Wechsel und Verkettung", () => {
  it("wechselt den Operator, solange noch kein zweiter Operand eingegeben wurde", async () => {
    const user = userEvent.setup();
    render(<Calculator />);
    await user.click(ariaButton("5"));
    await user.click(ariaButton("+"));
    await user.click(ariaButton("×"));
    expect(expressionLine()).toHaveTextContent("5 × …");
    expect(mockCalculate).not.toHaveBeenCalled();
  });

  it("verwendet das Ergebnis als nächsten linken Operanden für einen Folge-Operator", async () => {
    mockCalculate.mockResolvedValueOnce(10);
    const user = userEvent.setup();
    render(<Calculator />);
    await user.click(ariaButton("7"));
    await user.click(ariaButton("+"));
    await user.click(ariaButton("3"));
    await user.click(ariaButton("Gleich"));
    await awaitResult("10");

    await user.click(ariaButton("×"));
    expect(expressionLine()).toHaveTextContent("10 × …");
  });

  it("verknüpft zwei Rechnungen ohne = (verkettete Operation)", async () => {
    mockCalculate.mockResolvedValueOnce(10);
    const user = userEvent.setup();
    render(<Calculator />);
    await user.click(ariaButton("7"));
    await user.click(ariaButton("+"));
    await user.click(ariaButton("3"));
    await user.click(ariaButton("×"));

    expect(mockCalculate).toHaveBeenCalledWith({ left: 7, operator: "ADD", right: 3 });
    await awaitResult("10");
    expect(expressionLine()).toHaveTextContent("10 × …");
  });

  it("zeigt die Fehlermeldung, wenn die verkettete Rechnung fehlschlägt", async () => {
    mockCalculate.mockRejectedValueOnce(new Error("Server-Fehler (HTTP 500)"));
    const user = userEvent.setup();
    render(<Calculator />);
    await user.click(ariaButton("7"));
    await user.click(ariaButton("+"));
    await user.click(ariaButton("3"));
    await user.click(ariaButton("×"));

    expect(await screen.findByRole("alert")).toHaveTextContent("Server-Fehler (HTTP 500)");
  });
});

describe("Calculator — = Verhalten", () => {
  it("tut nichts, wenn kein Operator gesetzt ist", async () => {
    const user = userEvent.setup();
    render(<Calculator />);
    await user.click(ariaButton("7"));
    await user.click(ariaButton("Gleich"));
    expect(resultDisplay()).toHaveTextContent("7");
    expect(mockCalculate).not.toHaveBeenCalled();
  });

  it("wiederholt den linken Operanden, wenn kein zweiter eingegeben wurde (7 + =)", async () => {
    mockCalculate.mockResolvedValueOnce(14);
    const user = userEvent.setup();
    render(<Calculator />);
    await user.click(ariaButton("7"));
    await user.click(ariaButton("+"));
    await user.click(ariaButton("Gleich"));
    expect(mockCalculate).toHaveBeenCalledWith({ left: 7, operator: "ADD", right: 7 });
    await awaitResult("14");
  });
});

describe("Calculator — Ladezustand", () => {
  it("deaktiviert alle Tasten und zeigt den Spinner während der Berechnung", async () => {
    let resolveFn: (v: number) => void = () => {};
    mockCalculate.mockImplementationOnce(
      () =>
        new Promise<number>((resolve) => {
          resolveFn = resolve;
        }),
    );

    const user = userEvent.setup();
    render(<Calculator />);
    await user.click(ariaButton("7"));
    await user.click(ariaButton("+"));
    await user.click(ariaButton("3"));
    await user.click(ariaButton("Gleich"));

    // Während loading: = zeigt den Spinner, Zifferntasten sind deaktiviert, Eingaben werden ignoriert.
    await waitFor(() =>
      expect(ariaButton("Gleich").querySelector(".animate-spin")).toBeInTheDocument(),
    );
    await user.click(ariaButton("5"));
    expect(mockCalculate).toHaveBeenCalledTimes(1);

    resolveFn(10);
    await awaitResult("10");
  });

  it("ignoriert weitere Rechnungen, während eine läuft (busy-Schutz)", async () => {
    let resolveFn: (v: number) => void = () => {};
    mockCalculate.mockImplementationOnce(
      () =>
        new Promise<number>((resolve) => {
          resolveFn = resolve;
        }),
    );

    const user = userEvent.setup();
    render(<Calculator />);
    await user.click(ariaButton("4"));
    await user.click(ariaButton("+"));
    await user.click(ariaButton("2"));
    await user.click(ariaButton("Gleich"));
    await waitFor(() =>
      expect(ariaButton("Gleich").querySelector(".animate-spin")).toBeInTheDocument(),
    );

    // Während busy gilt der nächste = -Klick nicht als neue Anfrage.
    await user.click(ariaButton("Gleich"));
    expect(mockCalculate).toHaveBeenCalledTimes(1);

    resolveFn(6);
    await awaitResult("6");
  });
});

describe("Calculator — Fehlerzustand zurücksetzen", () => {
  it("setzt den Fehlerzustand zurück, wenn nach einem Fehler eine Ziffer gedrückt wird", async () => {
    mockCalculate.mockRejectedValueOnce(new Error("Division durch null ist nicht definiert."));
    const user = userEvent.setup();
    render(<Calculator />);

    await user.click(ariaButton("5"));
    await user.click(ariaButton("÷"));
    await user.click(ariaButton("0"));
    await user.click(ariaButton("Gleich"));
    expect(await screen.findByRole("alert")).toBeInTheDocument();

    // Eine Zifferneingabe räumt den Fehlerzustand auf (Alert verschwindet).
    await user.click(ariaButton("3"));
    expect(screen.queryByRole("alert")).not.toBeInTheDocument();

    // Eine explizite Eingabe danach zeigt wieder einen gültigen Wert.
    await user.click(ariaButton("Löschen"));
    await user.click(ariaButton("3"));
    expect(resultDisplay()).toHaveTextContent("3");
  });
});

describe("Calculator — Verlauf mit verschiedenen Operatoren", () => {
  it.each([
    ["MULTIPLY", "×"],
    ["DIVIDE", "÷"],
    ["SUBTRACT", "−"],
    ["PERCENT", "%"],
  ])("zeigt den Operator %s korrekt als %s im Verlauf", async (apiOp, symbol) => {
    const operatorKey = OP_LABEL[apiOp as keyof typeof OP_LABEL];
    mockCalculate.mockResolvedValueOnce(1);
    const user = userEvent.setup();
    render(<Calculator />);

    await user.click(ariaButton("8"));
    await user.click(ariaButton(operatorKey));
    await user.click(ariaButton("2"));
    await user.click(ariaButton("Gleich"));
    await awaitResult("1");

    const verlauf = screen.getByRole("region", { name: "Verlauf" });
    expect(verlauf.textContent).toContain(symbol);
  });
});
