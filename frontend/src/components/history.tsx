import { History as HistoryIcon } from "lucide-react";
import { formatNumber } from "@/lib/calculator";
import type { Operator } from "@/lib/api";

export interface HistoryEntry {
  left: number;
  operator: Operator;
  right: number;
  result: number;
}

interface HistoryProps {
  entries: HistoryEntry[];
}

/**
 * Verlauf der letzten 5 Rechnungen.
 * Lebt im Browser (useState) und ist nach einem Reload leer.
 */
export function History({ entries }: HistoryProps) {
  if (entries.length === 0) {
    return (
      <section aria-label="Verlauf" className="mt-6">
        <h2 className="text-sm font-medium text-muted-foreground flex items-center gap-2">
          <HistoryIcon className="h-4 w-4" aria-hidden="true" />
          Verlauf
        </h2>
        <p className="mt-2 text-sm text-muted-foreground">
          Noch keine Rechnung durchgeführt
        </p>
      </section>
    );
  }

  return (
    <section aria-label="Verlauf" className="mt-6">
      <h2 className="text-sm font-medium text-muted-foreground flex items-center gap-2">
        <HistoryIcon className="h-4 w-4" aria-hidden="true" />
        Verlauf
      </h2>
      <ul className="mt-2 space-y-1">
        {entries.map((entry, index) => (
          <li
            key={`${index}-${entry.left}-${entry.operator}-${entry.right}`}
            className="flex items-center justify-between gap-4 rounded-md border border-border bg-background px-3 py-2 text-sm"
          >
            <span className="text-muted-foreground truncate">
              {formatNumber(entry.left)}{" "}
              <span aria-hidden="true">·</span>{" "}
              {entry.operator === "MULTIPLY" || entry.operator === "DIVIDE"
                ? entry.operator === "MULTIPLY"
                  ? "×"
                  : "÷"
                : entry.operator === "ADD"
                  ? "+"
                  : entry.operator === "SUBTRACT"
                    ? "−"
                    : "%"}{" "}
              <span aria-hidden="true">·</span> {formatNumber(entry.right)}
            </span>
            <span className="font-semibold tabular-nums">= {formatNumber(entry.result)}</span>
          </li>
        ))}
      </ul>
    </section>
  );
}
