import { useState, useCallback, useRef } from "react";
import { Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { calculateExpression } from "@/lib/api";
import { isOperator } from "@/lib/calculator";

type CalculatorState = "idle" | "loading" | "error" | "result";

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

export function Calculator() {
  const [display, setDisplay] = useState("0");
  const [state, setState] = useState<CalculatorState>("idle");
  const [expression, setExpression] = useState("");
  const [errorMessage, setErrorMessage] = useState("");
  const abortRef = useRef<AbortController | null>(null);

  const reset = useCallback(() => {
    setDisplay("0");
    setExpression("");
    setState("idle");
    setErrorMessage("");
  }, []);

  const handleEqual = useCallback(async () => {
    if (!expression.trim()) return;

    // Cancel any in-flight request
    abortRef.current?.abort();
    const controller = new AbortController();
    abortRef.current = controller;

    setState("loading");
    setErrorMessage("");

    try {
      // Convert display operator symbols to parseable expression
      // The backend expects infix notation with standard operators
      const apiExpression = expression
        .replace(/×/g, "*")
        .replace(/÷/g, "/");

      const result = await calculateExpression(apiExpression, controller.signal);

      const resultStr = Number.isInteger(result)
        ? result.toString()
        : parseFloat(result.toFixed(10)).toString();

      setDisplay(resultStr);
      setExpression(resultStr);
      setState("result");
    } catch (err) {
      if (err instanceof DOMException && err.name === "AbortError") return;

      const msg =
        err instanceof Error ? err.message : "Ein Fehler ist aufgetreten";
      setDisplay("Fehler");
      setErrorMessage(msg);
      setState("error");
    } finally {
      if (abortRef.current === controller) {
        abortRef.current = null;
      }
    }
  }, [expression]);

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
        if (state === "loading") return;
        handleEqual();
        return;
      }

      if (state === "loading") return;

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
          className={`text-right text-2xl font-mono break-all leading-relaxed flex items-center gap-2 ${
            state === "error"
              ? "text-destructive"
              : "text-foreground"
          }`}
          aria-live="polite"
          aria-atomic="true"
          role="status"
        >
          {state === "loading" && (
            <Loader2
              className="h-5 w-5 animate-spin shrink-0"
              aria-hidden="true"
            />
          )}
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

      {/* Loading indicator bar */}
      {state === "loading" && (
        <div className="mb-2 h-1 bg-primary/20 rounded-full overflow-hidden">
          <div
            className="h-full bg-primary rounded-full animate-pulse"
            style={{ width: "60%" }}
          />
        </div>
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
            disabled={state === "loading" && btn.action !== "clear"}
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
