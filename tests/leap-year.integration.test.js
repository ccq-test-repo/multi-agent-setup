/**
 * Integrationstests für leap-year.js
 *
 * Testet das Zusammenspiel der Funktionen isLeapYear, formatDate
 * und daysUntilNextLeapYear in realistischen Szenarien.
 *
 * Verwendet den integrierten Node.js Test Runner (node:test).
 * Aufruf: node --test tests/
 */

const assert = require('node:assert');
const { describe, it, mock, before } = require('node:test');
const { isLeapYear, formatDate, daysUntilNextLeapYear } = require('../leap-year.js');

// ---------------------------------------------------------------------------
// Integrationstests: Funktionen im Zusammenspiel
// ---------------------------------------------------------------------------

describe('Integration: isLeapYear + formatDate', () => {
    it('should correctly identify and format known leap day dates', () => {
        const testDates = [
            { year: 2000, expected: true, date: '2000-02-29' },
            { year: 2024, expected: true, date: '2024-02-29' },
            { year: 1900, expected: false, date: '1900-03-01' }, // 1900 hat keinen 29.02.
            { year: 2026, expected: false, date: '2026-03-01' },
        ];

        for (const { year, expected, date } of testDates) {
            // isLeapYear muss mit formatDate übereinstimmen
            assert.strictEqual(isLeapYear(year), expected, `isLeapYear(${year})`);

            // formatDate für den 1.3. des Jahres
            if (!expected) {
                assert.strictEqual(formatDate(new Date(year, 2, 1)), date, `formatDate 1.3.${year}`);
            }
        }
    });

    it('should verify Feb 29 only exists in leap years', () => {
        for (let year = 2000; year <= 2100; year++) {
            const feb29 = new Date(year, 1, 29);
            const hasFeb29 = feb29.getMonth() === 1; // Februar
            assert.strictEqual(hasFeb29, isLeapYear(year),
                `Jahr ${year}: Feb 29 existiert=${hasFeb29}, isLeapYear=${isLeapYear(year)}`);
        }
    });
});

describe('Integration: daysUntilNextLeapYear with different starting dates', () => {
    it('should count days from non-leap-year to next leap year correctly', () => {
        // 1.1.2023 → 1.1.2024 = 365 Tage (2023 ist kein Schaltjahr, 2024 ist eins)
        const fixedNow = new Date(2023, 0, 1);
        mock.timers.enable({ apis: ['Date'] });
        mock.timers.setTime(fixedNow.getTime());
        mock.method(console, 'log', () => {});

        daysUntilNextLeapYear();

        const lastArg = console.log.mock.calls.at(-1).arguments[0];
        assert.match(lastArg, /365/,
            `2023-01-01 → 2024-01-01 sollte 365 Tage sein, war: ${lastArg}`);

        mock.timers.reset();
        mock.reset();
    });

    it('should handle the century non-leap-year transition (2096 → 2104)', () => {
        // 2096 ist Schaltjahr, 2100 nicht, nächstes Schaltjahr ist 2104
        // Nach dem 1.1.2096 → nächstes ist 2104
        // Das sind 8 Jahre, 2 davon Schalttage (2096+2104 sind Schaltjahre, aber 2100 nicht)
        // 2096-01-02 → 2104-01-01 = (8*365 + 2 Schalttage - 1) Tag
        // Einfacher: ab 2096-01-02 bis 2104-01-01
        // 2096 (Schaltjahr): 366 - 1 = 365 (ab 2.1.)
        // 2097: 365, 2098: 365, 2099: 365, 2100: 365, 2101: 365, 2102: 365, 2103: 365
        // Gesamt: 365 + 7*365 = 2920
        const fixedNow = new Date(2096, 0, 2);
        mock.timers.enable({ apis: ['Date'] });
        mock.timers.setTime(fixedNow.getTime());
        mock.method(console, 'log', () => {});

        daysUntilNextLeapYear();

        const lastArg = console.log.mock.calls.at(-1).arguments[0];
        assert.ok(lastArg.includes('2920'),
            `2096-01-02 → nächstes Schaltjahr sollte 2920 sein, war: ${lastArg}`);

        mock.timers.reset();
        mock.reset();
    });
});

describe('Integration: End-to-End leap year display', () => {
    it('should produce three lines of console output', () => {
        const fixedNow = new Date(2026, 5, 23);
        mock.timers.enable({ apis: ['Date'] });
        mock.timers.setTime(fixedNow.getTime());
        const lines = [];
        mock.method(console, 'log', (...args) => lines.push(args.join(' ')));

        daysUntilNextLeapYear();

        assert.strictEqual(lines.length, 3, 'sollte drei Console-Ausgaben produzieren');
        assert.match(lines[0], /Heute: 2026-06-23/);
        assert.match(lines[1], /Schaltjahr beginnt am 2028-01-01/);
        assert.match(lines[2], /Noch \d+ Tage/);

        mock.timers.reset();
        mock.reset();
    });
});
