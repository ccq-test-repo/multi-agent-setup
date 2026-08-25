import { useState, useCallback } from "react";
import { Button } from "@/components/ui/button";
import {
  isOperator,
  parseBinaryExpression,
  formatNumber,
  calculate,
} from "@/lib/calculator";
import type { HistoryEntry } from "@/hooks/useHistory";

type CalculatorState = "idle" | "error" | "result";

const buttons: {
  label: string;
  action: string;
  variant?: "secondary" | "destructive" | "ghost" | "outline";
}[] = [
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

interface CalculatorProps {
  onCalculate?: (entry: HistoryEntry) => void;
}

export function Calculator({ onCalculate }: CalculatorProps) {
  const [display, setDisplay] = useState("0");
  const [state, setState] = useState<CalculatorState>("idle");
  const [expression, setExpression] = useState("");
  const [errorMessage, setErrorMessage] = useState("");

  const reset = useCallback(() => {
    setDisplay("0");
    setExpression("");
    setState("idle");
    setErrorMessage("");
  }, []);

  const handleEqual = useCallback(() => {
    if (!expression.trim()) return;

    try {
      // Berechnung lokal und synchron über die pure Rechenlogik.
      // Division durch Null wird von applyOperation mit einem Error abgefangen.
      const result = calculate(expression);
      const resultStr = formatNumber(result);

      // Verlaufseintrag aus dem sichtbaren Ausdruck (mit ×, ÷) erzeugen
      const parsed = parseBinaryExpression(expression);
      if (parsed) {
        const entry: HistoryEntry = {
          operandA: parsed.a,
          operator: parsed.op,
          operandB: parsed.b,
          result: resultStr,
        };
        onCalculate?.(entry);
      }

      setDisplay(resultStr);
      setExpression(resultStr);
      setState("result");
    } catch (err) {
      const msg =
        err instanceof Error ? err.message : "Ein Fehler ist aufgetreten";
      setDisplay("Fehler");
      setErrorMessage(msg);
      setState("error");
    }
  }, [expression, onCalculate]);

  const handleButton = useCallback(
    (action: string) => {
      if (action === "clear") {
        reset();
        return;
      }

      if (action === "backspace") {
        if (state === "error" || state === "result") {
          reset();
          return;
        }
        if (expression.length <= 1) {
          reset();
          return;
        }
        const newExpr = expression.slice(0, -1);
        setExpression(newExpr);
        setDisplay(newExpr || "0");
        return;
      }

      if (state === "error") {
        if (action === "=") return;
        reset();
      }

      if (action === "=") {
        handleEqual();
        return;
      }

      if (isOperator(action)) {
        if (expression === "" && action === "-") {
          const newExpr = expression + action;
          setExpression(newExpr);
          setDisplay(newExpr);
          return;
        }
        if (
          expression === "" ||
          isOperator(expression[expression.length - 1])
        ) {
          return;
        }
        const newExpr = expression + " " + action + " ";
        setExpression(newExpr);
        setDisplay(newExpr);
        return;
      }

      if (action === ".") {
        const parts = expression.split(/[\+\-\×\÷]/);
        const lastPart = parts[parts.length - 1].trim();
        if (lastPart.includes(".")) return;
      }

      const newExpr = expression + action;
      setExpression(newExpr);
      setDisplay(newExpr);
      setState("idle");
    },
    [expression, state, reset, handleEqual]
  );

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
          <span>{display}</span>
        </div>
      </div>

      {/* Error message */}
      {state === "error" && errorMessage && (
        <p className="text-sm text-destructive mb-2 text-center" role="alert">
          {errorMessage}
        </p>
      )}

      {/* Empty state */}
      {state === "idle" && expression === "" && (
        <p className="text-sm text-muted-foreground mb-2 text-center">
          Gib eine Rechnung ein und drücke =
        </p>
      )}

      {/* Button grid */}
      <div
        className="grid grid-cols-4 gap-2"
        role="group"
        aria-label="Tastenfeld"
      >
        {buttons.map((btn) => (
          <Button
            key={btn.label + btn.action}
            variant={btn.variant ?? "default"}
            size="lg"
            className={`text-lg font-semibold ${
              btn.action === "=" ? "col-span-2" : ""
            } ${btn.action === "clear" ? "text-destructive-foreground" : ""}`}
            onClick={() => handleButton(btn.action)}
            aria-label={
              btn.action === "clear"
                ? "Löschen"
                : btn.action === "backspace"
                  ? "Zeichen löschen"
                  : btn.action === "="
                    ? "Gleich"
                    : btn.action === "÷"
                      ? "Geteilt"
                      : btn.action === "×"
                        ? "Mal"
                        : btn.action === "-"
                          ? "Minus"
                          : btn.action === "+"
                            ? "Plus"
                            : btn.action === "."
                              ? "Komma"
                              : `Ziffer ${btn.action}`
            }
          >
            {btn.label}
          </Button>
        ))}
      </div>
    </div>
  );
}
