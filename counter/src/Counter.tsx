import { useState } from 'react'

export default function Counter() {
  const [count, setCount] = useState(0)

  return (
    <main>
      <h1>Zaehler</h1>
      <p data-testid="count">{count}</p>
      <button type="button" onClick={() => setCount((c) => c + 1)}>
        Erhoehen
      </button>
      <button type="button" onClick={() => setCount(0)}>
        Zuruecksetzen
      </button>
    </main>
  )
}
