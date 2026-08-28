import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import Counter from './Counter'

/**
 * Test-Suite fuer die Zaehler-Seite.
 *
 * WICHTIG zur Mutation-Score-Staerke (DoD: >= 70 % Mutation Score):
 * `toHaveTextContent('1')` matcht als *Teilstring* — der Wert `-1` wuerde
 * damit faelschlich als '1' durchgehen (falsch-gruen). Nur exakte Vergleiche
 * (`textContent` strickt gleich) unterscheiden z. B. den uberlebenden Mutanten
 * `c + 1` -> `c - 1`. Deshalb verwenden wir durchgaengig exakte String-/Wert-
 * Vergleiche statt substring-basierter Matcher.
 */

/** Wert des "count"-Elements exakt auslesen. */
function countText(): string {
  return screen.getByTestId('count').textContent ?? ''
}

/** Am "Erhoehen"-Button n-mal klicken. */
async function clickIncrease(user: ReturnType<typeof userEvent.setup>, n: number) {
  const increase = screen.getByRole('button', { name: 'Erhoehen' })
  for (let i = 0; i < n; i++) {
    await user.click(increase)
  }
}

function clickReset(user: ReturnType<typeof userEvent.setup>) {
  return user.click(screen.getByRole('button', { name: 'Zuruecksetzen' }))
}

describe('Counter-Seite', () => {
  it('zeigt die Ueberschrift "Zaehler" und einen exakten Stand 0 beim Start', () => {
    render(<Counter />)

    expect(screen.getByRole('heading', { name: 'Zaehler' })).toBeInTheDocument()
    // Exakter Wert: `-0` oder `0 ` (Leerraum) duerfen nicht durchrutschen.
    expect(countText()).toBe('0')
  })

  it('gibt den Stand als reinen Text aus (kein Attribut, kein Zusatzzeichen)', () => {
    render(<Counter />)

    // Exakt "0" — kein Leerraum, kein Substring-Problem.
    expect(countText()).toBe('0')
  })

  it('der Button "Erhoehen" erhoeht den Stand von 0 auf exakt 1', async () => {
    const user = userEvent.setup()
    render(<Counter />)

    await clickIncrease(user, 1)

    // Exakt 1 — erkennt Mutanten `+ 1` -> `- 1` (waere -1) und `+1` -> `*1`/`/1`.
    expect(countText()).toBe('1')
  })

  it('mehrfaches Erhoehen erhoeht den Stand exakt 1 -> 2 -> 3', async () => {
    const user = userEvent.setup()
    render(<Counter />)

    await clickIncrease(user, 1)
    expect(countText()).toBe('1')

    await clickIncrease(user, 1)
    expect(countText()).toBe('2')

    await clickIncrease(user, 1)
    expect(countText()).toBe('3')
  })

  it('der Button "Zuruecksetzen" setzt einen erhoehten Stand exakt auf 0 zurueck', async () => {
    const user = userEvent.setup()
    render(<Counter />)

    await clickIncrease(user, 2)
    expect(countText()).toBe('2')

    await clickReset(user)

    expect(countText()).toBe('0')
  })

  it('Zuruecksetzen bei Stand 0 laesst den Stand exakt bei 0', async () => {
    const user = userEvent.setup()
    render(<Counter />)

    expect(countText()).toBe('0')
    await clickReset(user)

    expect(countText()).toBe('0')
  })
})
