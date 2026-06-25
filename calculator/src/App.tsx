import { Calculator } from "@/components/calculator";

export default function App() {
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
      <main>
        <Calculator />
      </main>
      <footer className="mt-8 text-xs text-muted-foreground">
        Unterstützt: +, -, ×, ÷
      </footer>
    </div>
  );
}
