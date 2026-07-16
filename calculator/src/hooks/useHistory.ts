import { useState, useCallback } from "react";

export interface HistoryEntry {
  /** First operand as a formatted number string */
  operandA: string;
  /** Operator symbol: +, −, ×, ÷ */
  operator: string;
  /** Second operand as a formatted number string */
  operandB: string;
  /** Result as a formatted number string */
  result: string;
}

const STORAGE_KEY = "calculator-history";
const MAX_ENTRIES = 10;

function loadHistory(): HistoryEntry[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return [];
    const parsed = JSON.parse(raw);
    if (!Array.isArray(parsed)) return [];
    return parsed.slice(0, MAX_ENTRIES);
  } catch {
    return [];
  }
}

function saveHistory(entries: HistoryEntry[]): void {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(entries));
  } catch {
    // localStorage may be full or unavailable — silently ignore
  }
}

export function useHistory() {
  const [entries, setEntries] = useState<HistoryEntry[]>(loadHistory);

  const addEntry = useCallback((entry: HistoryEntry) => {
    setEntries((prev) => {
      const next = [entry, ...prev].slice(0, MAX_ENTRIES);
      saveHistory(next);
      return next;
    });
  }, []);

  const clearHistory = useCallback(() => {
    setEntries([]);
    saveHistory([]);
  }, []);

  return { entries, addEntry, clearHistory };
}
