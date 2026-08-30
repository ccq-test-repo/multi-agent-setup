/**
 * Reine, testbare Hilfslogik für die Taschenrechner-Oberfläche.
 *
 * Das UI arbeitet mit einem Two-Operand-Modell:
 *   left  OP  right  →  POST /api/calculate
 *
 * Diese Datei enthält KEINE Rechenlogik — gerechnet wird ausschließlich im
 * Backend über die API (Sub-Issue #82). Hier leben nur Eingabe-, Darstellungs-
 * und Zuordnungs-Helfer.
 */

import type { Operator } from "@/lib/api";

/** Anzeige-Symbole für die Bedienoberfläche. */
export type DisplayOperator = "+" | "−" | "×" | "÷" | "%";

/** Operatoren, wie sie an der Oberfläche angezeigt werden. */
export const DISPLAY_OPERATORS: Record<Operator, DisplayOperator> = {
  ADD: "+",
  SUBTRACT: "−",
  MULTIPLY: "×",
  DIVIDE: "÷",
  PERCENT: "%",
};

/** Ordnet ein Tasten-Label dem API-Operator zu (oder null). */
export function mapOperatorKey(key: string): Operator | null {
  switch (key) {
    case "+":
      return "ADD";
    case "-":
    case "−":
      return "SUBTRACT";
    case "*":
    case "×":
      return "MULTIPLY";
    case "/":
    case "÷":
      return "DIVIDE";
    case "%":
      return "PERCENT";
    default:
      return null;
  }
}

/** Zeigt einen Operator zur Anzeige (Anzeige-Symbol). */
export function operatorSymbol(operator: Operator): DisplayOperator {
  return DISPLAY_OPERATORS[operator];
}

/**
 * Formatiert eine Zahl zur Anzeige.
 * Entfernt unnötige Nachkommastellen und begrenzt auf 10 Stellen.
 * Übernimmt dabei die Rundungs-Konvention des Backends (10 Nachkommastellen).
 */
export function formatNumber(n: number): string {
  if (!Number.isFinite(n)) {
    return Number.isNaN(n) ? "Nicht definiert" : n === Infinity ? "∞" : "-∞";
  }
  if (Number.isInteger(n) && Math.abs(n) < 1e15) return n.toString();
  return parseFloat(n.toFixed(10)).toString();
}

/**
 * Liest eine vom Nutzer eingegebene Zahl.
 * Ein Punkt/Komma als dezimales Trennzeichen; ausschließlich Ziffern,
 * ein optionales Vorzeichen und ein Dezimalpunkt sind erlaubt.
 */
export function parseInput(value: string): number | null {
  if (value === "" || value === "-" || value === "." || value === "-.") {
    return null;
  }
  const num = Number(value);
  return Number.isFinite(num) ? num : null;
}

/** Baut einen lesbaren Ausdruck für die Neben-Anzeige: `7 × 6`. */
export function buildExpression(
  left: string,
  operator: DisplayOperator | null,
  right: string,
): string {
  if (operator === null) return left;
  return `${left} ${operator} ${right}`;
}

/** Wandelt ein Dezimaltrennzeichen (`,` oder `.`) in einen einheitlichen Punkt. */
export function normalizeDecimalChar(char: string): string {
  return char === "," ? "." : char;
}

/** Prüft, ob ein Zeichen eine Ziffer ist. */
export function isDigit(char: string): boolean {
  return /^[0-9]$/.test(char);
}
