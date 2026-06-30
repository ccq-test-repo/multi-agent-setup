/**
 * Pure Rechenlogik für den Taschenrechner.
 * Exportiert Funktionen, die sowohl von der UI als auch von Tests genutzt werden können.
 */

export type CalcOp = "+" | "-" | "×" | "÷";

const operators: CalcOp[] = ["+", "-", "×", "÷"];

export function isOperator(token: string): token is CalcOp {
  return operators.includes(token as CalcOp);
}

export function applyOperation(a: number, op: CalcOp, b: number): number {
  switch (op) {
    case "+": return a + b;
    case "-": return a - b;
    case "×": return a * b;
    case "÷": {
      if (b === 0) throw new Error("Division durch Null");
      return a / b;
    }
    default: return b;
  }
}

export function calculate(expression: string): number {
  // Parse the expression into tokens
  const tokens: string[] = [];
  let current = "";

  for (let i = 0; i < expression.length; i++) {
    const ch = expression[i];
    if (isOperator(ch) && current) {
      tokens.push(current);
      tokens.push(ch);
      current = "";
    } else {
      current += ch;
    }
  }
  if (current) tokens.push(current);

  if (tokens.length === 0) return 0;

  // First pass: handle ÷ and ×
  const processed: (string | number)[] = [parseFloat(tokens[0])];
  for (let i = 1; i < tokens.length; i += 2) {
    const op = tokens[i] as CalcOp;
    const num = parseFloat(tokens[i + 1]);
    if (isNaN(num)) throw new Error("Ungültige Eingabe");

    if (op === "÷" || op === "×") {
      const last = processed.pop() as number;
      processed.push(applyOperation(last, op, num));
    } else {
      processed.push(op, num);
    }
  }

  // Second pass: handle + and -
  let result = processed[0] as number;
  for (let i = 1; i < processed.length; i += 2) {
    const op2 = processed[i] as CalcOp;
    const num2 = processed[i + 1] as number;
    result = applyOperation(result, op2, num2);
  }

  return result;
}

export function formatNumber(n: number): string {
  if (Number.isInteger(n)) return n.toString();
  return parseFloat(n.toFixed(10)).toString();
}
