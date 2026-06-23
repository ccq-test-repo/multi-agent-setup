/**
 * k6-Lasttest-Skript (Vorlage für HTTP-basierte APIs)
 *
 * Dieses Skript simuliert 1000 gleichzeitige Nutzer, die
 * die geo-basierte Navigation des Pathiful-Backends aufrufen.
 *
 * Ersetze BASE_URL, sobald das Backend deployed ist.
 *
 * Installation k6: https://k6.io/docs/getting-started/installation/
 * Aufruf: k6 run tests/load-test-k6.js
 */

import http from 'k6/http';
import { check, sleep } from 'k6';
import { SharedArray } from 'k6/data';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

// 1000 zufällige Koordinaten-Paare (Norddeutschland)
const routes = new SharedArray('routes', function () {
    const data = [];
    for (let i = 0; i < 1000; i++) {
        data.push({
            startLat: 53.0 + Math.random() * 2.0,   // ~53–55°N
            startLon: 8.0 + Math.random() * 6.0,     // ~8–14°E
            endLat: 53.0 + Math.random() * 2.0,
            endLon: 8.0 + Math.random() * 6.0,
            distance: 20 + Math.floor(Math.random() * 80), // 20–100 km
        });
    }
    return data;
});

export const options = {
    stages: [
        { duration: '10s', target: 1000 }, // Rampe hoch auf 1000 VUs
        { duration: '30s', target: 1000 }, // 1000 parallel für 30s
        { duration: '10s', target: 0 },    // Runterfahren
    ],
    thresholds: {
        http_req_duration: ['p(95)<500'], // 95% der Requests unter 500ms
        http_req_failed: ['rate<0.01'],   // max 1% Fehler
    },
};

export default function () {
    const idx = __VU % routes.length;
    const route = routes[idx];

    // 1. Route berechnen (gemockte externe API)
    const routePayload = JSON.stringify({
        start: { lat: route.startLat, lon: route.startLon },
        end: { lat: route.endLat, lon: route.endLon },
        distance: route.distance,
    });

    const res = http.post(
        `${BASE_URL}/api/routes/calculate`,
        routePayload,
        { headers: { 'Content-Type': 'application/json' } }
    );

    check(res, {
        'route status 200': (r) => r.status === 200,
        'route response time < 2s': (r) => r.timings.duration < 2000,
    });

    // 2. Scoring abrufen
    const scoringRes = http.get(
        `${BASE_URL}/api/routes/scoring?routeId=${res.json().id || 0}`,
        { headers: { 'Content-Type': 'application/json' } }
    );

    check(scoringRes, {
        'scoring status 200': (r) => r.status === 200,
    });

    sleep(1);
}
