/**
 * Berechnet die Anzahl der Tage bis zum nächsten Schaltjahr.
 *
 * Ein Jahr ist ein Schaltjahr, wenn:
 * - Es durch 4 teilbar ist, UND
 * - NICHT durch 100 teilbar ist, ES SEI DENN durch 400.
 *
 * Das nächste Schaltjahr beginnt am 1. Januar des nächsten
 * Jahres, das diese Bedingungen erfüllt.
 */

function isLeapYear(year) {
    return (year % 4 === 0 && year % 100 !== 0) || (year % 400 === 0);
}

function formatDate(date) {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
}

function daysUntilNextLeapYear() {
    const today = new Date();
    console.log(`Heute: ${formatDate(today)}`);

    let year = today.getFullYear();

    // Falls das heutige Datum bereits nach dem 1. Januar eines
    // Schaltjahres liegt, prüfen wir das nächste Jahr.
    while (true) {
        if (isLeapYear(year)) {
            const target = new Date(year, 0, 1); // 1. Januar
            if (target > today || (target.getTime() === today.getTime())) {
                break;
            }
        }
        year++;
    }

    const target = new Date(year, 0, 1); // 1. Januar des gefundenen Schaltjahres
    const diffMs = target.getTime() - today.getTime();
    const diffDays = Math.ceil(diffMs / (1000 * 60 * 60 * 24));

    console.log(`Nächstes Schaltjahr beginnt am ${formatDate(target)}`);
    console.log(`Noch ${diffDays} Tage bis zum nächsten Schaltjahr`);
}

daysUntilNextLeapYear();
