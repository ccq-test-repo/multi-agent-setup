import { useState, useCallback } from "react";
import { Button } from "@/components/ui/button";
// icons

type CalcOp = "+" | "-" | "×" | "÷";
type CalculatorState = "idle" | "error" | "result";

const operators: CalcOp[] = ["+", "-", "×", "÷"];

function isOperator(token: string): token is CalcOp {
  return operators.includes(token as CalcOp);
}

function applyOperation(a: number, op: CalcOp, b: number): number {
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

function calculate(expression: string): number {
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
  let processed: (string | number)[] = [parseFloat(tokens[0])];
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

function formatNumber(n: number): string {
  if (Number.isInteger(n)) return n.toString();
  return parseFloat(n.toFixed(10)).toString();
}

const buttons: { label: string; action: string; variant?: "secondary" | "destructive" | "ghost" | "outline" }[] = [
  { label: "C", action: "clear", variant: "destructive" },
  { label: "⌫", action: "backspace", variant: "secondary" },
  { label: "÷", action: "÷", variant: "secondary" },
  { label: "7", action: "7" },
  { label: "8", action: "8" },
  { label: "9", action: "9" },
  { label: "×", action: "×", variant: "secondary" },
  { label: "4", action: "4" },
  { label: "5", action: "5" },
  { label: "6", action: "6" },
  { label: "-", action: "-", variant: "secondary" },
  { label: "1", action: "1" },
  { label: "2", action: "2" },
  { label: "3", action: "3" },
  { label: "+", action: "+", variant: "secondary" },
  { label: "0", action: "0" },
  { label: ",", action: "." },
  { label: "=", action: "=", variant: "outline" },
];

export function Calculator() {
  const [display, setDisplay] = useState("0");
  const [state, setState] = useState<CalculatorState>("idle");
  const [expression, setExpression] = useState("");

  const handleButton = useCallback((action: string) => {
    if (action === "clear") {
      setDisplay("0");
      setExpression("");
      setState("idle");
      return;
    }

    if (action === "backspace") {
      if (state === "error" || state === "result") {
        setDisplay("0");
        setExpression("");
        setState("idle");
        return;
      }
      if (expression.length <= 1) {
        setDisplay("0");
        setExpression("");
        setState("idle");
        return;
      }
      const newExpr = expression.slice(0, -1);
      setExpression(newExpr);
      setDisplay(newExpr || "0");
      return;
    }

    if (state === "error") {
      if (action === "=") return;
      setDisplay("0");
      setExpression("");
      setState("idle");
    }

    if (action === "=") {
      try {
        const result = calculate(expression);
        setDisplay(formatNumber(result));
        setExpression(formatNumber(result));
        setState("result");
      } catch {
        setDisplay("Fehler");
        setState("error");
      }
      return;
    }

    if (isOperator(action)) {
      // Prevent consecutive operators
      if (expression === "" && action === "-") {
        // Allow negative numbers
        const newExpr = expression + action;
        setExpression(newExpr);
        setDisplay(newExpr);
        return;
      }
      if (expression === "" || isOperator(expression[expression.length - 1])) {
        return;
      }
      const newExpr = expression + " " + action + " ";
      setExpression(newExpr);
      setDisplay(newExpr);
      return;
    }

    if (action === ".") {
      // Find the last number in the expression
      const parts = expression.split(/[\+\-\×\÷]/);
      const lastPart = parts[parts.length - 1].trim();
      if (lastPart.includes(".")) return;
    }

    const newExpr = expression + action;
    setExpression(newExpr);
    setDisplay(newExpr);
    setState("idle");
  }, [expression, state]);

  return (
    <div className="w-full max-w-xs mx-auto">
      {/* Display */}
      <div className="bg-secondary rounded-lg p-4 mb-4 min-h-[5rem] flex flex-col items-end justify-end">
        <div
          className={`text-right text-2xl font-mono break-all leading-relaxed ${
            state === "error" ? "text-destructive" : "text-foreground"
          }`}
          aria-live="polite"
          aria-atomic="true"
          role="status"
        >
          {display}
        </div>
      </div>

      {/* Loading state demo - shown on first render instead of idle */}
      {state === "idle" && display === "0" && false}

      {/* Empty state - shown only when nothing is happening */}
      {state === "error" && (
        <p className="text-sm text-destructive mb-2 text-center" role="alert">
          Ungültige Eingabe. Drücke C zum Zurücksetzen.
        </p>
      )}

      {/* Button grid */}
      <div className="grid grid-cols-4 gap-2" role="group" aria-label="Tastenfeld">
        {buttons.map((btn) => (
          <Button
            key={btn.label + btn.action}
            variant={btn.variant ?? "default"}
            size="lg"
            className={`text-lg font-semibold ${
              btn.action === "=" ? "col-span-2" : ""
            } ${btn.action === "clear" ? "text-destructive-foreground" : ""}`}
            onClick={() => handleButton(btn.action)}
            disabled={false}
            aria-label={
              btn.action === "clear" ? "Löschen" :
              btn.action === "backspace" ? "Zeichen löschen" :
              btn.action === "=" ? "Gleich" :
              btn.action === "÷" ? "Geteilt" :
              btn.action === "×" ? "Mal" :
              btn.action === "-" ? "Minus" :
              btn.action === "+" ? "Plus" :
              btn.action === "." ? "Komma" :
              `Ziffer ${btn.action}`
            }
          >
            {btn.label}
          </Button>
        ))}
      </div>
    </div>
  );
}
