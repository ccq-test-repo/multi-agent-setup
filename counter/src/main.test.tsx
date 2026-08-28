import { describe, it, expect, beforeAll } from 'vitest'
import { act, screen, cleanup } from '@testing-library/react'

/**
 * Test der App-Einstiegsdatei `main.tsx`.
 *
 * `main.tsx` rendert den Counter ueber `createRoot(document.getElementById('root')!)`
 * in den DOM-Container `#root`. Da kein Unit-Test diese Datei importierte, konnte
 * z. B. der StringLiteral-Mutant `'root'` -> anderes String (getElementById liefert
 * dann null) unerkannt ueberleben und den Mutation Score druecken.
 *
 * Dieser Test importiert `main.tsx` *dynamisch* innerhalb der Test-Hooks, NACHDEM der
 * Container `#root` im jsdom-DOM existiert. So wird der echte Render-Pfad von
 * `main.tsx` ausgefuehrt und geprueft: Ist das String-Literal mutiert (Container wird
 * nicht gefunden), wirft `createRoot(null!)` und der Test schlaegt fehl -> Mutant gekillt.
 *
 * Anmerkung zur Umgebungsannahme: Der Test setzt das Vorhandensein eines
 * `#root`-Elements voraus und mounted es selbst separat — er laeuft isoliert und
 * unabhaenig von anderen Suiten (eigene Datei, keine geteilte DOM-Fixture noetig).
 * Falls in Zukunft mehrere Einstiegspunkte existieren, trennen.
 */

describe('main.tsx (App-Einstieg)', () => {
  beforeAll(() => {
    cleanup()
    // Naechster Schritt mounted #root selbst; sicherstellen, dass kein Alt-Zustand da ist.
  })

  it('rendert die Zaehler-App in den Container #root', async () => {
    // Arrange: Container so erzeugen, wie index.html ihn bereitstellt.
    const rootEl = document.createElement('div')
    rootEl.id = 'root'
    document.body.appendChild(rootEl)

    // Act: Einstiegspunkt importieren -> fuert createRoot(#root).render(<Counter />) aus.
    // React 18 rendert mit createRoot asynchron; in act() wrappen, damit das
    // Rendering vor der Assertion abgeschlossen ist.
    await act(async () => {
      await import('./main')
    })

    // Assert: Der Counter ist im #root gerendert (Ueberschrift sichtbar, Stand 0).
    // getByText ohne { exact: false } -> um sicherzugehen, dass der gerenderte Knoten
    // wirklich vom React-Baum stammt, fragen wir die Ueberschrift ueber die Rolle ab.
    const heading = screen.getByRole('heading', { name: 'Zaehler' })
    expect(heading).toBeInTheDocument()
    expect(screen.getByTestId('count').textContent).toBe('0')

    // Cleanup des getesteten Container-Knotens.
    rootEl.remove()
  })
})
