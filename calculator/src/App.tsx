import { Calculator } from "@/components/calculator";
import { HistoryPanel } from "@/components/history-panel";
import { useHistory } from "@/hooks/useHistory";

export default function App() {
  const { entries, addEntry, clearHistory } = useHistory();

  return (
    <div className="min-h-screen flex flex-col items-center justify-center p-4">
      <header className="mb-6 text-center">
        <h1 className="text-2xl font-semibold tracking-tight">
          Taschenrechner
        </h1>
        <p className="text-sm text-muted-foreground mt-1">
          Einfache Rechenoperationen
        </p>
      </header>
      <main className="flex flex-col lg:flex-row items-start gap-6 lg:gap-8">
        <Calculator onCalculate={addEntry} />
        <div className="w-px hidden lg:block bg-border self-stretch" aria-hidden="true" />
        <HistoryPanel entries={entries} onClear={clearHistory} />
      </main>
      <footer className="mt-8 text-xs text-muted-foreground">
        Unterstützt: +, -, ×, ÷
      </footer>
    </div>
  );
}
