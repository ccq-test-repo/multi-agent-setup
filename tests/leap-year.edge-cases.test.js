/**
 * Edge-Case-Tests für leap-year.js
 *
 * Testet extreme und seltene Randbedingungen:
 * - Sehr große/kleine Jahre
 * - Millisekunden-Genauigkeit der Tage-Berechnung
 * - Sommerzeit-Wechsel
 * - isLeapYear Performance unter Last
 *
 * Verwendet den integrierten Node.js Test Runner (node:test).
 * Aufruf: node --test tests/
 */

const assert = require('node:assert');
const { describe, it, mock } = require('node:test');
const { isLeapYear, formatDate, daysUntilNextLeapYear } = require('../leap-year.js');

// ---------------------------------------------------------------------------
// Edge Cases
// ---------------------------------------------------------------------------

describe('isLeapYear – extreme Jahre', () => {
    it('should handle year 0 (astronomisch = 1 v. Chr.)', () => {
        // 0 ist durch 4 und 400, aber auch durch 100 teilbar
        assert.strictEqual(isLeapYear(0), true, 'Jahr 0 ist ein Schaltjahr');
    });

    it('should handle very large years (year 1000000)', () => {
        // 1.000.000 ÷ 400 = 2500 → genau durch 400 teilbar
        assert.strictEqual(isLeapYear(1000000), true, '1000000 ist ein Schaltjahr (÷400)');
        assert.strictEqual(isLeapYear(1000001), false, '1000001 ist kein Schaltjahr');
        assert.strictEqual(isLeapYear(1000004), true, '1000004 ist ein Schaltjahr');
    });

    it('should handle Number.MAX_SAFE_INTEGER edge', () => {
        // Wir prüfen nur, dass es keinen Fehler wirft
        const year = Number.MAX_SAFE_INTEGER;
        const result = isLeapYear(year);
        assert.ok(typeof result === 'boolean', `MAX_SAFE_INTEGER gibt boolean zurück: ${result}`);
    });

    it('should handle very large negative years', () => {
        assert.strictEqual(isLeapYear(-400), true, '-400 ist Schaltjahr');
        assert.strictEqual(isLeapYear(-500), false, '-500 ist kein Schaltjahr (nicht ÷4)');
    });
});

describe('formatDate – extreme Daten', () => {
    it('should handle year 1000', () => {
        assert.strictEqual(formatDate(new Date(1000, 0, 1)), '1000-01-01');
    });

    it('should handle year 9999', () => {
        assert.strictEqual(formatDate(new Date(9999, 11, 31)), '9999-12-31');
    });

    it('should handle Jan 1 of year 1 (JS Date year < 100 is 1900+)', () => {
        // JS Date interpretiert Jahre 0-99 als 1900-1999.
        // formatDate gibt das Jahr korrekt aus JS Date zurück.
        assert.strictEqual(formatDate(new Date(1, 0, 1)), '1901-01-01');
    });
});

describe('daysUntilNextLeapYear – Sommerzeit und Zeitzonen', () => {
    it('should handle the date before a DST transition (spring forward)', () => {
        // In Europa: 29. März 2026 → 28. März auf 29. März wird Uhr vorgestellt
        // 31. Dezember 2027 → 1. Januar 2028 = 1 Tag, unabhängig von DST
        const fixedNow = new Date('2027-10-31T00:00:00Z'); // Ende MEZ/MESZ
        mock.timers.enable({ apis: ['Date'] });
        mock.timers.setTime(fixedNow.getTime());
        mock.method(console, 'log', () => {});

        daysUntilNextLeapYear();

        // Nächstes Schaltjahr ist 2028
        const lastArg = console.log.mock.calls.at(-1).arguments[0];
        // 2027-10-31 → 2028-01-01 = 62 Tage
        assert.ok(lastArg.includes('62'),
            `2027-10-31 → 2028-01-01 sollte 62 sein, war: ${lastArg}`);

        mock.timers.reset();
        mock.reset();
    });
});

// ---------------------------------------------------------------------------
// Performance: 1000+ Aufrufe von isLeapYear
// ---------------------------------------------------------------------------

describe('Performance – 1000x isLeapYear', () => {
    it('should compute 1000 leap year checks in under 100ms', () => {
        const years = [];
        for (let i = 0; i < 1000; i++) {
            years.push(Math.floor(Math.random() * 100000) - 50000);
        }

        const start = Date.now();
        for (const y of years) {
            isLeapYear(y);
        }
        const duration = Date.now() - start;

        assert.ok(duration < 100, `1000 isLeapYear-Aufrufe dauerten ${duration}ms (Limit: 100ms)`);
    });
});
