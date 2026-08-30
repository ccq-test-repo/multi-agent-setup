import { describe, it, expect } from "vitest";
import {
  DISPLAY_OPERATORS,
  buildExpression,
  formatNumber,
  isDigit,
  mapOperatorKey,
  normalizeDecimalChar,
  operatorSymbol,
  parseInput,
} from "@/lib/calculator";

describe("mapOperatorKey", () => {
  it("ordnet die drei Multiplikations-Eingabeformen MULTIPLY zu", () => {
    expect(mapOperatorKey("*")).toBe("MULTIPLY");
    expect(mapOperatorKey("×")).toBe("MULTIPLY");
  });

  it("ordnet Plus und das Anzeige-Symbol + dem Operator ADD zu", () => {
    expect(mapOperatorKey("+")).toBe("ADD");
  });

  it("ordnet Minus- und Subtraktions-Symbole dem Operator SUBTRACT zu", () => {
    expect(mapOperatorKey("-")).toBe("SUBTRACT");
    expect(mapOperatorKey("−")).toBe("SUBTRACT");
  });

  it("ordnet Schrägstrich und Geteilt-Symbol dem Operator DIVIDE zu", () => {
    expect(mapOperatorKey("/")).toBe("DIVIDE");
    expect(mapOperatorKey("÷")).toBe("DIVIDE");
  });

  it("ordnet das Prozentzeichen dem Operator PERCENT zu", () => {
    expect(mapOperatorKey("%")).toBe("PERCENT");
  });

  it("liefert null für unbekannte Eingaben zurück (Kein gültiger Operator)", () => {
    expect(mapOperatorKey("=")).toBeNull();
    expect(mapOperatorKey("C")).toBeNull();
    expect(mapOperatorKey("ADD")).toBeNull();
    expect(mapOperatorKey("")).toBeNull();
  });
});

describe("operatorSymbol", () => {
  it("liefert für jeden API-Operator das Anzeige-Symbol", () => {
    expect(operatorSymbol("ADD")).toBe("+");
    expect(operatorSymbol("SUBTRACT")).toBe("−");
    expect(operatorSymbol("MULTIPLY")).toBe("×");
    expect(operatorSymbol("DIVIDE")).toBe("÷");
    expect(operatorSymbol("PERCENT")).toBe("%");
  });

  it("deckt alle DISPLAY_OPERATORS ab", () => {
    expect(DISPLAY_OPERATORS).toEqual({
      ADD: "+",
      SUBTRACT: "−",
      MULTIPLY: "×",
      DIVIDE: "÷",
      PERCENT: "%",
    });
  });
});

describe("formatNumber", () => {
  it("formatiert ganze Zahlen ohne Nachkommastellen", () => {
    expect(formatNumber(42)).toBe("42");
    expect(formatNumber(0)).toBe("0");
    expect(formatNumber(-7)).toBe("-7");
  });

  it("kürzt unnötige Nachkommastellen", () => {
    expect(formatNumber(14.5)).toBe("14.5");
    expect(formatNumber(2.1)).toBe("2.1");
  });

  it("begrenzt auf 10 Nachkommastellen mit Rundung", () => {
    expect(formatNumber(1 / 3)).toBe("0.3333333333");
    expect(formatNumber(2 / 3)).toBe("0.6666666667");
  });

  it("behandelt Nicht-Finite- und NaN-Fälle", () => {
    expect(formatNumber(NaN)).toBe("Nicht definiert");
    expect(formatNumber(Infinity)).toBe("∞");
    expect(formatNumber(-Infinity)).toBe("-∞");
  });
});

describe("parseInput", () => {
  it("parst einfache ganze Zahlen und Dezimalzahlen", () => {
    expect(parseInput("42")).toBe(42);
    expect(parseInput("12.5")).toBe(12.5);
    expect(parseInput("0")).toBe(0);
  });

  it("parst negative Zahlen", () => {
    expect(parseInput("-3")).toBe(-3);
  });

  it("liefert null für unvollständige Eingaben", () => {
    expect(parseInput("")).toBeNull();
    expect(parseInput("-")).toBeNull();
    expect(parseInput(".")).toBeNull();
    expect(parseInput("-.")).toBeNull();
  });

  it("liefert null für nicht-numerische Eingaben", () => {
    expect(parseInput("abc")).toBeNull();
    expect(parseInput("1e")).toBeNull();
  });
});

describe("buildExpression", () => {
  it("zeigt bei fehlendem Operator nur den linken Operanden", () => {
    expect(buildExpression("7", null, "")).toBe("7");
  });

  it("baut `7 × 6` aus Symbol-Operator und rechts", () => {
    expect(buildExpression("7", "×", "6")).toBe("7 × 6");
    expect(buildExpression("7", "+", "…")).toBe("7 + …");
  });
});

describe("normalizeDecimalChar", () => {
  it("wandelt das Komma in einen Punkt", () => {
    expect(normalizeDecimalChar(",")).toBe(".");
  });

  it("lässt einen Punkt unverändert", () => {
    expect(normalizeDecimalChar(".")).toBe(".");
  });

  it("lässt andere Zeichen unverändert", () => {
    expect(normalizeDecimalChar("5")).toBe("5");
  });
});

describe("isDigit", () => {
  it("erkennt Ziffern 0–9", () => {
    expect(isDigit("0")).toBe(true);
    expect(isDigit("5")).toBe(true);
    expect(isDigit("9")).toBe(true);
  });

  it("lehnt Nicht-Ziffern ab", () => {
    expect(isDigit("+")).toBe(false);
    expect(isDigit("a")).toBe(false);
    expect(isDigit(".")).toBe(false);
  });
});
