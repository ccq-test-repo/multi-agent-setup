import { Trash2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import type { HistoryEntry } from "@/hooks/useHistory";

interface HistoryPanelProps {
  entries: HistoryEntry[];
  onClear: () => void;
}

export function HistoryPanel({ entries, onClear }: HistoryPanelProps) {
  return (
    <section
      className="w-full max-w-xs mx-auto"
      aria-label="Verlauf"
    >
      <div className="flex items-center justify-between mb-3">
        <h2 className="text-sm font-semibold text-foreground">Verlauf</h2>
        {entries.length > 0 && (
          <Button
            variant="ghost"
            size="sm"
            onClick={onClear}
            className="h-7 px-2 text-xs text-muted-foreground hover:text-foreground"
            aria-label="Verlauf löschen"
          >
            <Trash2 className="h-3.5 w-3.5 mr-1" aria-hidden="true" />
            Löschen
          </Button>
        )}
      </div>

      {entries.length === 0 ? (
        <p className="text-sm text-muted-foreground text-center py-6">
          Noch keine Berechnungen
        </p>
      ) : (
        <ul className="space-y-1" role="list">
          {entries.map((entry, index) => (
            <li
              key={`${entry.operandA}-${entry.operator}-${entry.operandB}-${entry.result}-${index}`}
              className="flex items-center gap-1.5 rounded-md bg-secondary/50 px-3 py-2 text-sm font-mono text-foreground"
            >
              <span>{entry.operandA}</span>
              <span className="text-muted-foreground">{entry.operator}</span>
              <span>{entry.operandB}</span>
              <span className="text-muted-foreground">=</span>
              <span className="font-semibold">{entry.result}</span>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
