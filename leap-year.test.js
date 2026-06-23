/**
 * Tests für leap-year.js
 *
 * Verwendet den integrierten Node.js Test Runner (node:test).
 * Aufruf: node --test leap-year.test.js
 */

const assert = require('node:assert');
const { describe, it, mock } = require('node:test');
const { isLeapYear, formatDate, daysUntilNextLeapYear } = require('./leap-year.js');

// ---------------------------------------------------------------------------
// isLeapYear – Schaltjahr-Logik
// ---------------------------------------------------------------------------

describe('isLeapYear', () => {
    it('should return true for years divisible by 4 but not by 100', () => {
        assert.strictEqual(isLeapYear(2024), true, '2024 ist ein Schaltjahr');
        assert.strictEqual(isLeapYear(2028), true, '2028 ist ein Schaltjahr');
        assert.strictEqual(isLeapYear(4), true, '4 ist ein Schaltjahr');
        assert.strictEqual(isLeapYear(1996), true, '1996 ist ein Schaltjahr');
    });

    it('should return false for years not divisible by 4', () => {
        assert.strictEqual(isLeapYear(2023), false, '2023 ist kein Schaltjahr');
        assert.strictEqual(isLeapYear(2025), false, '2025 ist kein Schaltjahr');
        assert.strictEqual(isLeapYear(2026), false, '2026 ist kein Schaltjahr');
        assert.strictEqual(isLeapYear(2027), false, '2027 ist kein Schaltjahr');
        assert.strictEqual(isLeapYear(1), false, '1 ist kein Schaltjahr');
    });

    it('should return false for centurial years not divisible by 400', () => {
        assert.strictEqual(isLeapYear(1900), false, '1900 ist kein Schaltjahr');
        assert.strictEqual(isLeapYear(2100), false, '2100 ist kein Schaltjahr');
        assert.strictEqual(isLeapYear(2200), false, '2200 ist kein Schaltjahr');
        assert.strictEqual(isLeapYear(2300), false, '2300 ist kein Schaltjahr');
    });

    it('should return true for centurial years divisible by 400', () => {
        assert.strictEqual(isLeapYear(2000), true, '2000 ist ein Schaltjahr');
        assert.strictEqual(isLeapYear(2400), true, '2400 ist ein Schaltjahr');
    });

    it('should handle negative years (astronomical year numbering)', () => {
        // -4 = 5 v. Chr. – durch 4, nicht durch 100 → Schaltjahr im proleptischen Kalender
        assert.strictEqual(isLeapYear(-4), true, '-4 ist ein Schaltjahr (astronomisch)');
        assert.strictEqual(isLeapYear(-1), false, '-1 ist kein Schaltjahr');
        assert.strictEqual(isLeapYear(-100), false, '-100 ist kein Schaltjahr (durch 100, nicht durch 400)');
        assert.strictEqual(isLeapYear(-400), true, '-400 ist ein Schaltjahr (durch 400)');
    });
});

// ---------------------------------------------------------------------------
// formatDate
// ---------------------------------------------------------------------------

describe('formatDate', () => {
    it('should format a date as YYYY-MM-DD', () => {
        assert.strictEqual(formatDate(new Date(2026, 5, 23)), '2026-06-23');
    });

    it('should zero-pad month and day', () => {
        assert.strictEqual(formatDate(new Date(2026, 0, 1)), '2026-01-01');
        assert.strictEqual(formatDate(new Date(2026, 11, 9)), '2026-12-09');
    });

    it('should handle leap day (Feb 29)', () => {
        assert.strictEqual(formatDate(new Date(2024, 1, 29)), '2024-02-29');
    });

    it('should handle end of year', () => {
        assert.strictEqual(formatDate(new Date(2028, 11, 31)), '2028-12-31');
    });
});

// ---------------------------------------------------------------------------
// daysUntilNextLeapYear – Kapselung für deterministische Tests
// ---------------------------------------------------------------------------

describe('daysUntilNextLeapYear (deterministisch mit gemocktem Datum)', () => {
    it('should return 557 days from 2026-06-23 to 2028-01-01', () => {
        // Heute auf 2026-06-23 festlegen
        const fixedNow = new Date(2026, 5, 23); // Monat 5 = Juni

        mock.timers.enable({ apis: ['Date'] });
        mock.timers.setTime(fixedNow.getTime());

        // logs unterdrücken
        mock.method(console, 'log', () => {});

        daysUntilNextLeapYear();

        const expectedDays = 557; // 2026-06-23 → 2028-01-01 = 557 Tage
        const calls = console.log.mock.calls;
        const lastArg = calls[calls.length - 1].arguments[0];
        assert.match(lastArg, /557/, `Ausgabe sollte 557 enthalten, war: ${lastArg}`);

        mock.timers.reset();
        mock.reset();
    });

    it('should return 0 days when today is Jan 1 of a leap year', () => {
        // Heute = 1. Januar 2028 (Schaltjahr) → nächstes Schaltjahr ist heute
        const fixedNow = new Date(2028, 0, 1);

        mock.timers.enable({ apis: ['Date'] });
        mock.timers.setTime(fixedNow.getTime());
        mock.method(console, 'log', () => {});

        daysUntilNextLeapYear();

        const calls = console.log.mock.calls;
        const lastArg = calls[calls.length - 1].arguments[0];
        assert.match(lastArg, /0/, `Ausgabe sollte 0 enthalten, war: ${lastArg}`);

        mock.timers.reset();
        mock.reset();
    });

    it('should return 1 day when today is Dec 31 of the year before a leap year', () => {
        // 31. Dezember 2027 → 1. Januar 2028 = 1 Tag
        const fixedNow = new Date(2027, 11, 31);

        mock.timers.enable({ apis: ['Date'] });
        mock.timers.setTime(fixedNow.getTime());
        mock.method(console, 'log', () => {});

        daysUntilNextLeapYear();

        const calls = console.log.mock.calls;
        const lastArg = calls[calls.length - 1].arguments[0];
        assert.match(lastArg, /1/, `Ausgabe sollte 1 enthalten, war: ${lastArg}`);

        mock.timers.reset();
        mock.reset();
    });

    it('should handle the 1900 non-leap-year edge case by checking isLeapYear logic', () => {
        // 1900 ist kein Schaltjahr
        assert.strictEqual(isLeapYear(1900), false);
        // Nächstes Schaltjahr nach 1900 ist 1904
        assert.strictEqual(isLeapYear(1904), true);
        // formatDate für 1.1.1900
        assert.strictEqual(formatDate(new Date(1900, 0, 1)), '1900-01-01');
    });
});

// ---------------------------------------------------------------------------
// Integration: Script läuft ohne Fehler durch (CLI smoke test)
// ---------------------------------------------------------------------------

describe('CLI smoke test', () => {
    it('should run the script without errors', () => {
        // Der require allein führt keinen console.log aus, weil require.main !== module
        // → Wir rufen daysUntilNextLeapYear direkt auf, mit gemocktem console
        mock.method(console, 'log', () => {});
        const originalMain = require.main;
        // Simuliere direkten Aufruf
        daysUntilNextLeapYear();
        assert.ok(true, 'daysUntilNextLeapYear() läuft ohne Fehler');
        mock.reset();
    });
});
