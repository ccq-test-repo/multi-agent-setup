/**
 * Reine Rechenlogik für den Taschenrechner.
 *
 * Das UI arbeitet mit einem Two-Operand-Modell:
 * - Der Nutzer gibt eine Zahl a ein
 * - Wählt einen Operator (+ − × ÷)
 * - Gibt eine Zahl b ein
 * - Drückt = → API-Aufruf an /api/calc/{add,subtract,multiply,divide}
 *
 * Diese Datei enthält reine (testbare) Funktionen für die clientseitige
 * Eingabevalidierung und Hilfslogik.
 */

export type CalcOp = "+" | "-" | "×" | "÷";

const OPERATORS: CalcOp[] = ["+", "-", "×", "÷"];

export function isOperator(token: string): token is CalcOp {
  return OPERATORS.includes(token as CalcOp);
}

/**
 * Ordnet einem Anzeige-Operator den API-Operationsnamen zu.
 * × → multiply, ÷ → divide, + → add, - → subtract
 */
export function opToApiOperation(op: CalcOp): "add" | "subtract" | "multiply" | "divide" {
  switch (op) {
    case "+": return "add";
    case "-": return "subtract";
    case "×": return "multiply";
    case "÷": return "divide";
  }
}

/**
 * Formatiert eine Zahl zur Anzeige.
 * Entfernt unnötige Nachkommastellen und begrenzt auf 10 Stellen.
 */
export function formatNumber(n: number): string {
  if (Number.isInteger(n) && Math.abs(n) < 1e15) return n.toString();
  return parseFloat(n.toFixed(10)).toString();
}
