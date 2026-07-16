import { useState, useCallback, useRef } from "react";
import { Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { calculateOperation } from "@/lib/api";
import { isOperator, opToApiOperation, formatNumber, type CalcOp } from "@/lib/calculator";

type CalculatorState = "idle" | "loading" | "error" | "result";

interface ButtonDef {
  label: string;
  action: string;
  variant?: "secondary" | "destructive" | "ghost" | "outline";
  wide?: boolean;
}

const buttons: ButtonDef[] = [
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
  { label: "=", action: "=", variant: "outline", wide: true },
];

export function Calculator() {
  const [display, setDisplay] = useState("0");
  const [state, setState] = useState<CalculatorState>("idle");
  const [errorMessage, setErrorMessage] = useState("");

  // Two-operand model
  const [currentInput, setCurrentInput] = useState("0");
  const [operandA, setOperandA] = useState<number | null>(null);
  const [pendingOp, setPendingOp] = useState<CalcOp | null>(null);
  // true wenn der Nutzer als Nächstes den zweiten Operanden eingibt
  const [waitingForB, setWaitingForB] = useState(false);

  const abortRef = useRef<AbortController | null>(null);

  const reset = useCallback(() => {
    setDisplay("0");
    setCurrentInput("0");
    setOperandA(null);
    setPendingOp(null);
    setWaitingForB(false);
    setState("idle");
    setErrorMessage("");
  }, []);

  const digitPressed = useCallback((digit: string) => {
    if (state === "error" || state === "result") {
      reset();
    }

    if (waitingForB || operandA === null) {
      // Start new input
      setCurrentInput(digit);
      setDisplay(digit);
      if (waitingForB) setWaitingForB(false);
    } else {
      // Append to current input
      const next = currentInput === "0" && digit !== "." ? digit : currentInput + digit;
      setCurrentInput(next);
      setDisplay(next);
    }
    setState("idle");
    setErrorMessage("");
  }, [state, operandA, currentInput, waitingForB, reset]);

  const decimalPressed = useCallback(() => {
    if (state === "error" || state === "result") {
      reset();
    }

    const input = waitingForB || operandA === null ? "0" : currentInput;
    if (input.includes(".")) return;

    const next = input + ".";
    if (waitingForB || operandA === null) {
      setCurrentInput(next);
      setWaitingForB(false);
    } else {
      setCurrentInput(next);
    }
    setDisplay(next);
    setState("idle");
  }, [state, operandA, currentInput, waitingForB, reset]);

  const operatorPressed = useCallback((op: CalcOp) => {
    if (state === "loading") return;
    if (state === "error") {
      reset();
    }

    const currentVal = parseFloat(currentInput);
    if (isNaN(currentVal)) return;

    if (operandA !== null && pendingOp !== null && !waitingForB) {
      // Chain operation: a op b → use b as a for next operation
      // For true chaining we'd need the API — but /api/calc/* endpoints
      // are two-operand only. Store the value for now.
      setOperandA(currentVal);
    } else {
      setOperandA(currentVal);
      setCurrentInput("0");
    }

    setPendingOp(op);
    setWaitingForB(true);
    setDisplay(`${formatNumber(currentVal)} ${op} `);
    setState("idle");
    setErrorMessage("");
  }, [state, currentInput, operandA, pendingOp, waitingForB, reset]);

  const equalsPressed = useCallback(async () => {
    if (operandA === null || pendingOp === null) return;
    if (state === "loading") return;

    const b = parseFloat(currentInput);
    if (isNaN(b)) return;

    // Cancel any in-flight request
    abortRef.current?.abort();
    const controller = new AbortController();
    abortRef.current = controller;

    setState("loading");
    setErrorMessage("");

    try {
      const apiOp = opToApiOperation(pendingOp);
      const result = await calculateOperation(apiOp, operandA, b, controller.signal);

      const resultStr = formatNumber(result);
      setDisplay(resultStr);
      setCurrentInput(resultStr);
      setOperandA(result);
      setPendingOp(null);
      setWaitingForB(false);
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
  }, [operandA, pendingOp, currentInput, state]);

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
        const input = waitingForB ? currentInput : currentInput;
        if (input.length <= 1 || (input.length === 2 && input.startsWith("-"))) {
          setCurrentInput("0");
          setDisplay(waitingForB ? "0" : "0");
          return;
        }
        const next = input.slice(0, -1);
        setCurrentInput(next);
        setDisplay(next);
        return;
      }

      if (action === "=") {
        equalsPressed();
        return;
      }

      if (isOperator(action)) {
        operatorPressed(action);
        return;
      }

      if (action === ".") {
        decimalPressed();
        return;
      }

      // Digit (0-9)
      digitPressed(action);
    },
    [state, currentInput, waitingForB, reset, equalsPressed, operatorPressed, decimalPressed, digitPressed],
  );

  return (
    <div className="w-full max-w-xs mx-auto">
      {/* Display */}
      <div className="bg-secondary rounded-lg p-4 mb-4 min-h-[5rem] flex flex-col items-end justify-end">
        <div
          className={`text-right text-2xl font-mono break-all leading-relaxed flex items-center gap-2 ${
            state === "error" ? "text-destructive" : "text-foreground"
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

      {/* Empty/initial state */}
      {state === "idle" && operandA === null && currentInput === "0" && (
        <p className="text-sm text-muted-foreground mb-2 text-center">
          Gib eine Zahl ein, wähle eine Operation und drücke =
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
              btn.wide ? "col-span-2" : ""
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
