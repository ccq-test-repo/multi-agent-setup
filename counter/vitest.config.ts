import { defineConfig } from 'vitest/config'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: ['./src/test/setup.ts'],
    include: ['src/**/*.test.{ts,tsx}'],
    // Hard coverage gate: fail the job below 80% line coverage (DoD/ticket AC #2).
    // Uses vitest's native v8 provider — the only one that instruments Vite
    // transformed ESM/TS modules correctly (c8 sees 0% through Vite).
    coverage: {
      provider: 'v8',
      reporter: ['text', 'lcov'],
      thresholds: {
        lines: 80,
      },
    },
  },
})
