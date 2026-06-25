/**
 * Pathiful – Service Worker
 *
 * Offline-Light-Modus:
 * - Cachet statische Dateien (HTML, CSS, JS) beim ersten Laden
 * - Cachet die zuletzt geladene Routendaten im Cache
 * - Zeigt eine Offline-Seite bei fehlender Verbindung
 *
 * Cache-Strategie: Cache-First für statische Assets (dann Netzwerk),
 * Network-First für API-Anfragen (mit Cache-Fallback).
 */

const CACHE_STATIC = "pathiful-static-v1";
const CACHE_ROUTES = "pathiful-routes-v1";
const CACHE_API = "pathiful-api-v1";

const STATIC_ASSETS = [
  "/",
  "/index.html",
  "/styles/main.css",
  "/scripts/app.js",
  "/manifest.json",
];

// =============================================================================
// Install: Statische Assets cache
// =============================================================================

self.addEventListener("install", (event) => {
  event.waitUntil(
    (async () => {
      const cache = await caches.open(CACHE_STATIC);
      await cache.addAll(STATIC_ASSETS);
      console.log("[SW] Static assets cached");
      await self.skipWaiting();
    })()
  );
});

// =============================================================================
// Activate: Alte Caches bereinigen
// =============================================================================

self.addEventListener("activate", (event) => {
  event.waitUntil(
    (async () => {
      const keys = await caches.keys();
      const keep = [CACHE_STATIC, CACHE_ROUTES, CACHE_API];
      await Promise.all(
        keys
          .filter((k) => !keep.includes(k))
          .map((k) => caches.delete(k))
      );
      console.log("[SW] Old caches cleaned");
      await self.clients.claim();
    })()
  );
});

// =============================================================================
// Fetch: Netzwerk mit Cache-Fallback
// =============================================================================

self.addEventListener("fetch", (event) => {
  const { request } = event;
  const url = new URL(request.url);

  // Nur innerhalb unserer Origin abfangen (keine externen CDN-Styles/Scripts)
  // Externe Requests (map tiles, CDN) durchlassen
  if (url.origin !== self.location.origin) {
    return;
  }

  // Statische Assets: Cache-First, dann Netzwerk
  if (
    request.destination === "style" ||
    request.destination === "script" ||
    request.destination === "document" ||
    request.destination === "manifest"
  ) {
    event.respondWith(cacheFirst(request));
    return;
  }

  // API-Anfragen: Network-First mit Cache-Fallback
  if (url.pathname.startsWith("/api/")) {
    event.respondWith(networkFirst(request));
    return;
  }

  // Alles andere: Network-First
  event.respondWith(networkFirst(request));
});

// =============================================================================
// Cache-Strategien
// =============================================================================

async function cacheFirst(request) {
  const cached = await caches.match(request);
  if (cached) {
    return cached;
  }
  try {
    const response = await fetch(request);
    if (response.ok) {
      const cache = await caches.open(CACHE_STATIC);
      await cache.put(request, response.clone());
    }
    return response;
  } catch (err) {
    // Fallback: Offline-Seite für Dokumente
    if (request.destination === "document") {
      const fallback = await caches.match("/");
      if (fallback) return fallback;
    }
    throw err;
  }
}

async function networkFirst(request) {
  try {
    const response = await fetch(request);
    if (response.ok) {
      // API-Antworten in separatem Cache speichern
      if (request.url.includes("/api/routes")) {
        const cache = await caches.open(CACHE_ROUTES);
        await cache.put(request, response.clone());
      } else {
        const cache = await caches.open(CACHE_API);
        await cache.put(request, response.clone());
      }
    }
    return response;
  } catch (err) {
    const cached = await caches.match(request);
    if (cached) {
      return cached;
    }
    // Für API-Fehler: Leeres JSON-Array oder Fehlerobjekt zurückgeben
    if (request.url.includes("/api/routes")) {
      return new Response(JSON.stringify([]), {
        status: 200,
        headers: { "Content-Type": "application/json" },
      });
    }
    throw err;
  }
}

