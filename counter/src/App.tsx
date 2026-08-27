import { useState } from "react";

function App() {
  const [count, setCount] = useState(0);

  return (
    <main className="container">
      <h1>Zaehler</h1>
      <p className="count" data-testid="count">
        {count}
      </p>
      <div className="actions">
        <button type="button" onClick={() => setCount((c) => c + 1)}>
          Erhoehen
        </button>
        <button type="button" onClick={() => setCount(0)}>
          Zuruecksetzen
        </button>
      </div>
    </main>
  );
}

export default App;
