import { Calculator } from "@/components/calculator";

export default function App() {
  return (
    <div className="min-h-screen w-full flex flex-col items-center justify-start p-4">
      <header className="mt-4 mb-6 text-center">
        <h1 className="text-2xl font-semibold tracking-tight">Taschenrechner</h1>
        <p className="mt-1 text-sm text-muted-foreground">
          Jede Rechnung läuft über die API — das Frontend rechnet nicht selbst.
        </p>
      </header>
      <main className="w-full flex justify-center">
        <Calculator />
      </main>
      <footer className="mt-8 text-xs text-muted-foreground text-center">
        Tastatur: Ziffern, + − × ÷, %, Enter = Gleich, Escape = Löschen
      </footer>
    </div>
  );
}
