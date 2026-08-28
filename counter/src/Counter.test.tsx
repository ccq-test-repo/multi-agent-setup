import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import Counter from './Counter'

describe('Counter-Seite', () => {
  it('zeigt die Ueberschrift "Zaehler" und einen Stand, der bei 0 startet', () => {
    render(<Counter />)

    const heading = screen.getByRole('heading', { name: 'Zaehler' })
    expect(heading).toBeInTheDocument()

    const count = screen.getByTestId('count')
    expect(count).toHaveTextContent('0')
  })

  it('der Stand ist als Text mit der Test-ID "count" lesbar', () => {
    render(<Counter />)

    const count = screen.getByTestId('count')
    expect(count).toHaveTextContent('0')
    // Der Stand wird als Text (nicht z. B. als Attribut) ausgegeben
    expect(count.textContent).toBe('0')
  })

  it('der Button "Erhoehen" erhoeht den Stand um 1', async () => {
    const user = userEvent.setup()
    render(<Counter />)

    const increase = screen.getByRole('button', { name: 'Erhoehen' })
    await user.click(increase)

    expect(screen.getByTestId('count')).toHaveTextContent('1')
  })

  it('mehrfaches Erhoehen erhoeht den Stand entsprechend', async () => {
    const user = userEvent.setup()
    render(<Counter />)

    const increase = screen.getByRole('button', { name: 'Erhoehen' })
    await user.click(increase)
    await user.click(increase)
    await user.click(increase)

    expect(screen.getByTestId('count')).toHaveTextContent('3')
  })

  it('der Button "Zuruecksetzen" setzt den Stand auf 0 zurueck', async () => {
    const user = userEvent.setup()
    render(<Counter />)

    await user.click(screen.getByRole('button', { name: 'Erhoehen' }))
    await user.click(screen.getByRole('button', { name: 'Erhoehen' }))
    expect(screen.getByTestId('count')).toHaveTextContent('2')

    await user.click(screen.getByRole('button', { name: 'Zuruecksetzen' }))

    expect(screen.getByTestId('count')).toHaveTextContent('0')
  })

  it('Zuruecksetzen bei Stand 0 laesst den Stand bei 0', async () => {
    const user = userEvent.setup()
    render(<Counter />)

    expect(screen.getByTestId('count')).toHaveTextContent('0')
    await user.click(screen.getByRole('button', { name: 'Zuruecksetzen' }))

    expect(screen.getByTestId('count')).toHaveTextContent('0')
  })
})
