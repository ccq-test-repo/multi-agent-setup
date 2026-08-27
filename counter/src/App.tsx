import { useState } from "react";

export default function App() {
  const [count, setCount] = useState(0);

  return (
    <div style={{ textAlign: "center", margin: "2rem auto", maxWidth: "20rem" }}>
      <h1>Zaehler</h1>
      <p data-testid="count">{count}</p>
      <button type="button" onClick={() => setCount((c) => c + 1)}>
        Erhoehen
      </button>
      <button type="button" onClick={() => setCount(0)}>
        Zuruecksetzen
      </button>
    </div>
  );
}
