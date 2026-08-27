import { useState } from "react";
import { Button } from "./components/ui/button";

function App() {
  const [count, setCount] = useState(0);

  return (
    <main className="mx-auto flex min-h-screen w-full max-w-screen-lg flex-col items-center justify-center gap-8 px-4 py-8 text-center">
      <h1 className="text-3xl font-semibold leading-tight">Zaehler</h1>
      <p
        className="text-4xl font-bold tabular-nums"
        data-testid="count"
        aria-live="polite"
      >
        {count}
      </p>
      <div className="flex flex-wrap items-center justify-center gap-4">
        <Button type="button" onClick={() => setCount((c) => c + 1)}>
          Erhoehen
        </Button>
        <Button
          type="button"
          variant="outline"
          onClick={() => setCount(0)}
        >
          Zuruecksetzen
        </Button>
      </div>
    </main>
  );
}

export default App;
