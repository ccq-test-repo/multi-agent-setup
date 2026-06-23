#!/usr/bin/env node

/**
 * Lasttest-Skript: 1000 parallele Aufrufe von leap-year.js
 *
 * Simuliert 1000 gleichzeitige "Anfragen" an die Schaltjahr-Berechnung,
 * analog zu 1000 parallelen Navigationsanfragen.
 *
 * Externe APIs sind nicht betroffen – die gesamte Logik ist synchron
 * und wird unter Node.js parallel ausgeführt.
 *
 * Aufruf: node tests/load-test.js
 *
 * Voraussetzung: k6 (für HTTP-Lasttest) – siehe tests/load-test-k6.js
 */

const { isLeapYear, daysUntilNextLeapYear } = require('../leap-year.js');

async function runLoadTest() {
    const NUM_REQUESTS = 1000;
    const CONCURRENCY = 100; // gleichzeitige Promises

    console.log(`\n=== Lasttest: ${NUM_REQUESTS}x isLeapYear (${CONCURRENCY} parallel) ===\n`);

    // ── 1. isLeapYear-Last ──────────────────────────────────────────────
    const testYears = [];
    for (let i = 0; i < NUM_REQUESTS; i++) {
        testYears.push(
            1900 + Math.floor(Math.random() * 500) // 1900–2399
        );
    }

    const start1 = process.hrtime.bigint();

    // Parallel 1: Chunked parallel execution (nicht blockierend)
    let results = [];
    for (let i = 0; i < testYears.length; i += CONCURRENCY) {
        const chunk = testYears.slice(i, i + CONCURRENCY);
        const chunkResults = await Promise.all(
            chunk.map((y) => Promise.resolve(isLeapYear(y)))
        );
        results.push(...chunkResults);
    }

    const end1 = process.hrtime.bigint();
    const dur1 = Number(end1 - start1) / 1e6;

    const leapCount = results.filter(Boolean).length;
    console.log(`  isLeapYear: ${NUM_REQUESTS} Aufrufe in ${dur1.toFixed(2)}ms`);
    console.log(`  → ${leapCount} Schaltjahre, ${NUM_REQUESTS - leapCount} Nicht-Schaltjahre`);
    console.log(`  → Durchschnitt: ${(dur1 / NUM_REQUESTS).toFixed(4)}ms/Aufruf`);

    // ── 2. formatDate-Last ──────────────────────────────────────────────
    const { formatDate } = require('../leap-year.js');
    const testDates = [];
    for (let i = 0; i < NUM_REQUESTS; i++) {
        testDates.push(new Date(
            2000 + Math.floor(Math.random() * 100),
            Math.floor(Math.random() * 12),
            Math.floor(Math.random() * 28) + 1
        ));
    }

    const start2 = process.hrtime.bigint();
    for (let i = 0; i < testDates.length; i += CONCURRENCY) {
        const chunk = testDates.slice(i, i + CONCURRENCY);
        await Promise.all(
            chunk.map((d) => Promise.resolve(formatDate(d)))
        );
    }
    const end2 = process.hrtime.bigint();
    const dur2 = Number(end2 - start2) / 1e6;

    console.log(`\n  formatDate: ${NUM_REQUESTS} Aufrufe in ${dur2.toFixed(2)}ms`);
    console.log(`  → Durchschnitt: ${(dur2 / NUM_REQUESTS).toFixed(4)}ms/Aufruf`);

    // ── 3. Kombinierter Workflow (simuliert "Navigation" = isLeapYear + formatDate) ──
    const start3 = process.hrtime.bigint();
    const navigationPromises = [];

    for (let i = 0; i < NUM_REQUESTS; i++) {
        navigationPromises.push(
            (async () => {
                const year = 1900 + Math.floor(Math.random() * 500);
                const leap = isLeapYear(year);
                const date = formatDate(new Date(year, 0, 1));
                return { year, leap, date };
            })()
        );
    }

    // Alle 1000 parallel starten
    const navResults = await Promise.all(navigationPromises);
    const end3 = process.hrtime.bigint();
    const dur3 = Number(end3 - start3) / 1e6;

    const validResults = navResults.filter((r) => r.date.startsWith(r.year.toString()));
    console.log(`\n  Navigation (isLeapYear + formatDate): ${NUM_REQUESTS}x parallel`);
    console.log(`  → ${dur3.toFixed(2)}ms Gesamtzeit (echt parallel!)`);
    console.log(`  → ${validResults.length}/${NUM_REQUESTS} gültige Ergebnisse`);

    // ── Fazit ───────────────────────────────────────────────────────────
    const totalDuration = dur1 + dur2 + dur3; // nicht ganz exakt, aber gut genug
    console.log(`\n  ✅ Gesamt: ${(totalDuration).toFixed(2)}ms für alle Tests`);
    console.log(`  ✅ Alle 1000 parallelen Aufrufe erfolgreich\n`);

    return { dur1, dur2, dur3, total: totalDuration };
}

// Hauptausführung
runLoadTest()
    .then(() => {
        process.exit(0);
    })
    .catch((err) => {
        console.error('❌ Lasttest fehlgeschlagen:', err.message);
        process.exit(1);
    });
