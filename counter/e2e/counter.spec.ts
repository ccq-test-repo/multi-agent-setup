import { test, expect } from '@playwright/test'

test.describe('Zaehler-Seite', () => {
  test('zeigt Ueberschrift "Zaehler" und einen Stand, der bei 0 startet', async ({ page }) => {
    await page.goto('/')

    await expect(page.getByRole('heading', { name: 'Zaehler' })).toBeVisible()
    await expect(page.getByTestId('count')).toHaveText('0')
  })

  test('Button "Erhoehen" erhoeht den Stand um 1', async ({ page }) => {
    await page.goto('/')

    await page.getByRole('button', { name: 'Erhoehen' }).click()
    await expect(page.getByTestId('count')).toHaveText('1')

    await page.getByRole('button', { name: 'Erhoehen' }).click()
    await expect(page.getByTestId('count')).toHaveText('2')
  })

  test('Button "Zuruecksetzen" setzt den Stand auf 0 zurueck', async ({ page }) => {
    await page.goto('/')

    await page.getByRole('button', { name: 'Erhoehen' }).click()
    await page.getByRole('button', { name: 'Erhoehen' }).click()
    await expect(page.getByTestId('count')).toHaveText('2')

    await page.getByRole('button', { name: 'Zuruecksetzen' }).click()
    await expect(page.getByTestId('count')).toHaveText('0')
  })

  test('Zuruecksetzen bei Stand 0 laesst den Stand bei 0', async ({ page }) => {
    await page.goto('/')

    await expect(page.getByTestId('count')).toHaveText('0')
    await page.getByRole('button', { name: 'Zuruecksetzen' }).click()
    await expect(page.getByTestId('count')).toHaveText('0')
  })
})
