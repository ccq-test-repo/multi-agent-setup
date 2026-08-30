import { useCallback, useEffect, useRef, useState } from "react";
import { Loader2, Minus, RotateCcw } from "lucide-react";
import { Button } from "@/components/ui/button";
import { History, type HistoryEntry } from "@/components/history";
import { calculate, type Operator } from "@/lib/api";
import {
  buildExpression,
  formatNumber,
  isDigit,
  mapOperatorKey,
  normalizeDecimalChar,
  operatorSymbol,
  parseInput,
} from "@/lib/calculator";

type Status = "idle" | "loading" | "error";

interface ButtonDef {
  label: string;
  key: string;
  variant?: "default" | "secondary" | "destructive" | "outline";
  span?: boolean;
}

const BUTTONS: ButtonDef[] = [
  { label: "C", key: "clear", variant: "destructive" },
  { label: "±", key: "negate", variant: "secondary" },
  { label: "%", key: "PERCENT", variant: "secondary" },
  { label: "÷", key: "DIVIDE", variant: "secondary" },
  { label: "7", key: "7" },
  { label: "8", key: "8" },
  { label: "9", key: "9" },
  { label: "×", key: "MULTIPLY", variant: "secondary" },
  { label: "4", key: "4" },
  { label: "5", key: "5" },
  { label: "6", key: "6" },
  { label: "−", key: "SUBTRACT", variant: "secondary" },
  { label: "1", key: "1" },
  { label: "2", key: "2" },
  { label: "3", key: "3" },
  { label: "+", key: "ADD", variant: "secondary" },
  { label: "0", key: "0" },
  { label: ",", key: "," },
  { label: "=", key: "equals", variant: "default", span: true },
];

/** Letzte (fehlgeschlagene) Anfrage, für die Wiederholen-Schaltfläche. */
interface PendingRequest {
  left: number;
  operator: Operator;
  right: number;
}

export function Calculator() {
  const [left, setLeft] = useState("0");
  const [operator, setOperator] = useState<Operator | null>(null);
  const [right, setRight] = useState("");
  const [result, setResult] = useState<string | null>(null);
  const [history, setHistory] = useState<HistoryEntry[]>([]);
  const [status, setStatus] = useState<Status>("idle");
  const [errorMsg, setErrorMsg] = useState("");

  const busyRef = useRef(false);
  const pendingRef = useRef<PendingRequest | null>(null);

  const reset = useCallback(() => {
    setLeft("0");
    setOperator(null);
    setRight("");
    setResult(null);
    setStatus("idle");
    setErrorMsg("");
    pendingRef.current = null;
  }, []);

  /** Wert der Zeile, die gerade bearbeitet wird (right während Eingabe, sonst left). */
  const entryValue = useCallback(
    (op: Operator | null, rightStr: string): string =>
      op !== null && rightStr !== "" ? rightStr : left,
    [left],
  );

  const setEntry = useCallback(
    (value: string) => {
      if (operator !== null && right !== "") setRight(value);
      else setLeft(value);
    },
    [operator, right],
  );

  const digitPressed = useCallback(
    (digit: string) => {
      if (busyRef.current) return;
      if (status === "error") reset();
      if (result !== null && operator === null) reset();
      if (operator !== null) {
        // Zweiten Operanden eingeben.
        setRight((r) => {
          if (r === "" && digit === ".") return "0.";
          if (r === "" && digit === "0") return "0";
          if (r === "0" && digit !== ".") return digit;
          if (r.includes(".") && digit === ".") return r;
          return r + digit;
        });
      } else {
        // Ersten Operanden eingeben.
        setLeft((l) => {
          if (l === "0" && digit !== ".") return digit;
          if (l.includes(".") && l.indexOf(".") === l.length - 1 && digit === ".") return l;
          if (l.includes(".") && digit === ".") return l;
          if (l === "0" && digit === ".") return "0.";
          return l + digit;
        });
      }
      setResult(null);
    },
    [busyRef, status, result, operator, reset],
  );

  const commaPressed = useCallback(() => {
    if (busyRef.current) return;
    if (status === "error") reset();
    if (result !== null && operator === null) reset();
    const entry = entryValue(operator, right);
    if (entry.includes(".")) return;
    const next = entry === "" ? "0." : entry + ".";
    setEntry(next);
    setResult(null);
  }, [busyRef, status, result, operator, right, reset, entryValue, setEntry]);

  const negatePressed = useCallback(() => {
    if (busyRef.current) return;
    if (status === "error") reset();
    if (result !== null && operator === null) {
      const val = parseInput(result);
      if (val === null) return;
      setLeft(formatNumber(-val));
      setResult(null);
      return;
    }
    const entry = entryValue(operator, right);
    if (entry === "" || entry === "0") return;
    setEntry(entry.startsWith("-") ? entry.slice(1) : "-" + entry);
    setResult(null);
  }, [busyRef, status, result, operator, right, reset, entryValue, setEntry]);

  const backspacePressed = useCallback(() => {
    if (busyRef.current) return;
    if (status === "error") reset();
    if (result !== null && operator === null) return;
    const entry = entryValue(operator, right);
    if (entry === "" || entry === "0") return;
    const next = entry.slice(0, -1);
    setEntry(next === "" || next === "-" ? "0" : next);
  }, [busyRef, status, result, operator, right, reset, entryValue, setEntry]);

  const recordSuccess = useCallback(
    (req: PendingRequest, value: number) => {
      const formatted = formatNumber(value);
      setHistory((h) =>
        [
          { left: req.left, operator: req.operator, right: req.right, result: value },
          ...h,
        ].slice(0, 5),
      );
      setResult(formatted);
      setLeft(formatted);
    },
    [],
  );

  const performCalculate = useCallback(
    async (req: PendingRequest) => {
      if (busyRef.current) return;
      busyRef.current = true;
      setStatus("loading");
      setErrorMsg("");
      pendingRef.current = req;
      try {
        const value = await calculate(req);
        recordSuccess(req, value);
        setOperator(null);
        setRight("");
        setStatus("idle");
      } catch (err) {
        const message =
          err instanceof Error ? err.message : "Unerwarteter Fehler.";
        setErrorMsg(message);
        setStatus("error");
      } finally {
        busyRef.current = false;
      }
    },
    [recordSuccess],
  );

  const performChained = useCallback(
    async (a: number, oldOp: Operator, b: number, newOp: Operator) => {
      if (busyRef.current) return;
      busyRef.current = true;
      setStatus("loading");
      setErrorMsg("");
      try {
        const value = await calculate({ left: a, operator: oldOp, right: b });
        const formatted = formatNumber(value);
        setHistory((h) =>
          [{ left: a, operator: oldOp, right: b, result: value }, ...h].slice(0, 5),
        );
        setResult(formatted);
        setLeft(formatted);
        setOperator(newOp);
        setRight("");
        setStatus("idle");
      } catch (err) {
        const message =
          err instanceof Error ? err.message : "Unerwarteter Fehler.";
        setErrorMsg(message);
        setStatus("error");
      } finally {
        busyRef.current = false;
      }
    },
    [],
  );

  const operatorPressed = useCallback(
    (op: Operator) => {
      if (busyRef.current) return;
      if (status === "error") reset();
      // Ergebnis als nächsten linken Operanden weiterverwenden.
      if (result !== null && operator === null) {
        setOperator(op);
        setRight("");
        setResult(null);
        return;
      }
      // Noch kein hängender Operator → nur setzen.
      if (operator === null) {
        setOperator(op);
        setRight("");
        setResult(null);
        return;
      }
      // Hängender Operator ohne zweiten Operanden → Operator wechseln.
      if (right === "") {
        setOperator(op);
        return;
      }
      // Beide Operanden vorhanden: hängende Rechnung abschließen und verketten.
      const aLeft = parseInput(left);
      const b = parseInput(right);
      if (aLeft === null || b === null) return;
      performChained(aLeft, operator, b, op);
    },
    [busyRef, status, result, operator, left, right, reset, performChained],
  );

  const equalsPressed = useCallback(() => {
    if (busyRef.current) return;
    if (status === "error") reset();
    if (operator === null) return;
    const a = parseInput(left);
    const b = parseInput(right !== "" ? right : left);
    if (a === null || b === null) return;
    void performCalculate({ left: a, operator, right: b });
  }, [busyRef, status, operator, left, right, reset, performCalculate]);

  const retry = useCallback(() => {
    if (pendingRef.current) void performCalculate(pendingRef.current);
  }, [performCalculate]);

  const handleAction = useCallback(
    (key: string) => {
      if (key === "clear") return reset();
      if (key === "negate") return negatePressed();
      if (key === "equals") return equalsPressed();
      if (isDigit(key)) return digitPressed(key);
      if (key === "," || key === ".") return commaPressed();
      const op = mapOperatorKey(key);
      if (op !== null) return operatorPressed(op);
    },
    [
      reset,
      negatePressed,
      equalsPressed,
      digitPressed,
      commaPressed,
      operatorPressed,
    ],
  );

  // Tastatur-Steuerung: Ziffern, + - * /, %, Enter=Gleich, Escape=Löschen,
  // Backspace=Zeichen löschen, ,/.=Dezimaltrennzeichen. Fokus bleibt sichtbar.
  useEffect(() => {
    const onKeyDown = (event: KeyboardEvent) => {
      if (event.ctrlKey || event.metaKey || event.altKey) return;
      const key = normalizeDecimalChar(event.key);
      if (key === "Enter") {
        event.preventDefault();
        handleAction("equals");
        return;
      }
      if (key === "Escape") {
        event.preventDefault();
        handleAction("clear");
        return;
      }
      if (key === "Backspace") {
        event.preventDefault();
        backspacePressed();
        return;
      }
      if (key === " ") return;
      handleAction(key);
    };
    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, [handleAction, backspacePressed]);

  // Anzeige: laufender Ausdruck (klein) und aktueller Wert/Ergebnis (groß).
  const baseLeft = result !== null ? result : left;
  const expressionLine =
    operator !== null
      ? buildExpression(baseLeft, operatorSymbol(operator), right === "" ? "…" : right)
      : baseLeft;
  const mainValue =
    operator !== null && right !== ""
      ? right
      : baseLeft;

  return (
    <div className="w-full max-w-sm">
      {/* Anzeigefeld */}
      <div className="mb-4 rounded-lg border border-border bg-background px-4 py-3 text-right">
        <p
          className="min-h-5 text-sm text-muted-foreground tabular-nums truncate"
          aria-live="polite"
        >
          {expressionLine}
        </p>
        <p className="text-3xl font-semibold tabular-nums truncate break-words">
          {status === "loading" ? "…" : mainValue}
        </p>
      </div>

      {/* Fehleranzeige mit Wiederholen */}
      {status === "error" && (
        <div
          role="alert"
          className="mb-4 rounded-md border border-destructive/40 bg-destructive/10 px-3 py-2 text-sm text-destructive"
        >
          <p>{errorMsg}</p>
          <Button onClick={retry} variant="outline" size="sm" className="mt-2">
            <RotateCcw className="h-4 w-4" aria-hidden="true" />
            Wiederholen
          </Button>
        </div>
      )}

      {/* Tastenfeld */}
      <div className="grid grid-cols-4 gap-2">
        {BUTTONS.map((btn) => (
          <Button
            key={btn.key}
            variant={btn.variant ?? "default"}
            size="calc"
            disabled={status === "loading"}
            onClick={() => handleAction(btn.key)}
            aria-label={ariaLabel(btn.key, btn.label)}
            className={btn.span ? "col-span-2" : undefined}
          >
            {btn.key === "equals" && status === "loading" ? (
              <Loader2 className="h-5 w-5 animate-spin" aria-hidden="true" />
            ) : btn.key === "negate" ? (
              <Minus className="h-5 w-5" aria-hidden="true" />
            ) : (
              btn.label
            )}
          </Button>
        ))}
      </div>

      <History entries={history} />
    </div>
  );
}

/** Zugänglicher Name für eine Taste. */
function ariaLabel(key: string, label: string): string {
  switch (key) {
    case "clear":
      return "Löschen";
    case "negate":
      return "Vorzeichenwechsel";
    case "equals":
      return "Gleich";
    case ",":
      return "Komma";
    case "ADD":
      return "Plus";
    case "SUBTRACT":
      return "Minus";
    case "MULTIPLY":
      return "Mal";
    case "DIVIDE":
      return "Geteilt";
    case "PERCENT":
      return "Prozent";
    default:
      return label;
  }
}
