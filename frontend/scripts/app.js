/**
 * Pathiful – Frontend Applikation
 *
 * Haupt-JavaScript für MapLibre-Karte, Routing, Tracking,
 * Authentifizierung, SEPA-Mandat und Wetterintegration.
 *
 * Abhängigkeiten:
 *  - MapLibre GL JS (geladen via CDN im HTML)
 *  - fetch API (ES6+)
 */

// =============================================================================
// State
// =============================================================================

const STATE = {
  map: null,
  marker: null,               // aktueller Standortmarker
  trackingWatchId: null,      // Geolocation watch id
  isTracking: false,
  routeLayerIds: [],          // auf der Karte aktive Layer-Ids
  currentRouteId: null,       // aktuell ausgewählte Route
  isRoundtrip: true,
  mapStyleIndex: 0,
  authToken: null,
  authUserId: null,
  authRole: null,
};

const MAP_STYLES = [
  'https://demotiles.maplibre.org/style.json',
  {
    version: 8,
    sources: {
      osm: {
        type: 'raster',
        tiles: ['https://tile.openstreetmap.org/{z}/{x}/{y}.png'],
        tileSize: 256,
        attribution: '© OpenStreetMap contributors',
      },
    },
    layers: [{ id: 'osm', type: 'raster', source: 'osm' }],
  },
];

const API_BASE = '/api';

// DOM-Referenzen (werden in init() gesetzt)
let DOM = {};

// =============================================================================
// Hilfsfunktionen
// =============================================================================

function $(id) { return document.getElementById(id); }

function show(el) {
  if (typeof el === 'string') el = $(el);
  if (el) el.classList.remove('hidden');
}

function hide(el) {
  if (typeof el === 'string') el = $(el);
  if (el) el.classList.add('hidden');
}

function formatDistance(meters) {
  if (!meters) return '-';
  return meters >= 1000 ? (meters / 1000).toFixed(1) + ' km' : meters.toFixed(0) + ' m';
}

function formatDuration(seconds) {
  if (!seconds) return '-';
  const h = Math.floor(seconds / 3600);
  const m = Math.floor((seconds % 3600) / 60);
  if (h > 0) return h + 'h ' + m + 'min';
  return m + ' min';
}

function formatScore(score) {
  if (score == null) return '-';
  return score.toFixed(1);
}

function storageKey(key) {
  return 'pathiful_' + key;
}

// =============================================================================
// API-Client
// =============================================================================

async function api(path, options = {}) {
  const url = API_BASE + path;
  const headers = { ...options.headers };
  if (STATE.authToken) {
    headers['Authorization'] = 'Bearer ' + STATE.authToken;
  }
  if (options.body && typeof options.body === 'object' && !(options.body instanceof FormData)) {
    headers['Content-Type'] = 'application/json';
  }
  const res = await fetch(url, {
    ...options,
    headers,
    body: options.body && typeof options.body === 'object' && !(options.body instanceof FormData)
      ? JSON.stringify(options.body) : options.body,
  });
  if (!res.ok) {
    const errBody = await res.text();
    let msg;
    try { const j = JSON.parse(errBody); msg = j.message || j.error || errBody; }
    catch { msg = errBody; }
    throw new Error(msg || 'HTTP ' + res.status);
  }
  const ct = res.headers.get('content-type') || '';
  if (ct.includes('application/json')) return res.json();
  if (ct.includes('text/')) return res.text();
  return res; // Blob etc.
}

// =============================================================================
// Auth
// =============================================================================

function loadAuth() {
  const token = localStorage.getItem(storageKey('token'));
  const userId = localStorage.getItem(storageKey('userId'));
  const role = localStorage.getItem(storageKey('role'));
  if (token && userId) {
    STATE.authToken = token;
    STATE.authUserId = parseInt(userId, 10);
    STATE.authRole = role || 'USER';
    updateAuthUI();
  }
}

function saveAuth(token, userId, role) {
  STATE.authToken = token;
  STATE.authUserId = userId;
  STATE.authRole = role || 'USER';
  localStorage.setItem(storageKey('token'), token);
  localStorage.setItem(storageKey('userId'), String(userId));
  localStorage.setItem(storageKey('role'), role || 'USER');
  updateAuthUI();
}

function clearAuth() {
  STATE.authToken = null;
  STATE.authUserId = null;
  STATE.authRole = null;
  localStorage.removeItem(storageKey('token'));
  localStorage.removeItem(storageKey('userId'));
  localStorage.removeItem(storageKey('role'));
  updateAuthUI();
}

function updateAuthUI() {
  const emailInput = DOM.profileEmail;
  const loginBtn = DOM.profileLoginBtn;
  const sepaForm = DOM.sepaForm;
  if (STATE.authToken) {
    emailInput.disabled = true;
    loginBtn.textContent = '🚪 Abmelden';
    show(sepaForm);
    loadSepaMandate();
  } else {
    emailInput.disabled = false;
    loginBtn.textContent = '🔐 Anmelden / Registrieren';
    hide(sepaForm);
    hide(DOM.sepaActiveInfo);
  }
}

// =============================================================================
// Profile Tab: Login / Register
// =============================================================================

async function handleLogin() {
  if (STATE.authToken) {
    // Ausloggen
    try {
      await api('/auth/logout', { method: 'POST' });
    } catch {}
    clearAuth();
    updateAuthUI();
    return;
  }
  const email = DOM.profileEmail.value.trim();
  const password = DOM.profilePassword.value;
  if (!email || !password) {
    alert('Bitte E-Mail und Passwort eingeben.');
    return;
  }
  try {
    // Versuche Login
    const data = await api('/auth/login', {
      method: 'POST',
      body: { email, password },
    });
    saveAuth(data.token, data.userId, data.role);
    // Prüfe ob MFA erforderlich
    if (data.requiresMfa) {
      await handleMfaChallenge(data.mfaSessionId);
    }
    DOM.profilePassword.value = '';
    loadRoutes();
  } catch (err) {
    // Login fehlgeschlagen → Registrierung versuchen
    try {
      const data = await api('/auth/register', {
        method: 'POST',
        body: { email, password },
      });
      saveAuth(data.token, data.userId, data.role);
      DOM.profilePassword.value = '';
    } catch (regErr) {
      alert('Login/Registrierung fehlgeschlagen: ' + regErr.message);
    }
  }
}

async function handleMfaChallenge(sessionId) {
  // ADMIN-MFA: Fordere Benutzer zur Eingabe eines TOTP-Codes auf
  const code = prompt('ADMIN-MFA erforderlich. Bitte TOTP-Code eingeben (Simulation: 123456):');
  if (!code) return;
  try {
    const data = await api('/auth/mfa/complete', {
      method: 'POST',
      body: { sessionId, code },
    });
    saveAuth(data.token, data.userId, data.role);
  } catch (err) {
    alert('MFA-Verifikation fehlgeschlagen: ' + err.message);
  }
}

// =============================================================================
// Routes: Laden, Anzeigen, Löschen
// =============================================================================

async function loadRoutes() {
  if (!STATE.authToken) return;
  try {
    const routes = await api('/routes');
    DOM.routeList.innerHTML = '';
    if (!routes || routes.length === 0) {
      DOM.routeEmpty.classList.remove('hidden');
      return;
    }
    DOM.routeEmpty.classList.add('hidden');
    routes.forEach(r => renderRouteCard(r));
  } catch (err) {
    console.warn('loadRoutes failed:', err);
  }
}

function renderRouteCard(route) {
  const card = document.createElement('div');
  card.className = 'route-card';
  card.dataset.routeId = route.id;
  card.innerHTML =
    `<div class="route-card-header">
      <strong>${escapeHtml(route.name) || 'Unbenannte Route'}</strong>
      <span class="badge-${route.type === 'ROUNDTRIP' ? 'roundtrip' : 'destination'}">${route.type === 'ROUNDTRIP' ? '↻' : '→'}</span>
    </div>
    <div class="route-card-body">
      <span>🚶 ${formatDistance(route.totalDistance)}</span>
      <span>⏱ ${formatDuration(route.totalDuration)}</span>
      <span>⭐ ${formatScore(route.sceneryScore)}</span>
    </div>`;
  card.addEventListener('click', () => showRouteDetail(route));
  DOM.routeList.appendChild(card);
}

function escapeHtml(text) {
  if (!text) return text;
  const d = document.createElement('div');
  d.textContent = text;
  return d.innerHTML;
}

async function showRouteDetail(route) {
  STATE.currentRouteId = route.id;
  DOM.detailName.textContent = route.name || 'Unbenannte Route';
  DOM.detailType.textContent = route.type === 'ROUNDTRIP' ? '↻ Rundroute' : '→ Zielroute';
  DOM.detailMode.textContent = route.transportMode || '-';
  DOM.detailDistance.textContent = formatDistance(route.totalDistance);
  DOM.detailDuration.textContent = formatDuration(route.totalDuration);
  DOM.detailScore.textContent = formatScore(route.sceneryScore);
  DOM.detailVisibility.textContent = route.publicRoute ? 'Öffentlich' : 'Privat';
  show(DOM.detailPanel);
  hide(DOM.ratingPanel);
  drawRouteOnMap(route);
}

async function deleteRoute() {
  if (!STATE.currentRouteId) return;
  if (!confirm('Route wirklich löschen?')) return;
  try {
    await api('/routes/' + STATE.currentRouteId, { method: 'DELETE' });
    clearRouteFromMap();
    hide(DOM.detailPanel);
    STATE.currentRouteId = null;
    loadRoutes();
  } catch (err) {
    alert('Löschen fehlgeschlagen: ' + err.message);
  }
}

// =============================================================================
// Map: MapLibre GL
// =============================================================================

function initMap() {
  STATE.map = new maplibregl.Map({
    container: 'map',
    style: MAP_STYLES[0],
    center: [10.5, 51.0], // Deutschland-Mitte
    zoom: 5,
    attributionControl: true,
  });
  STATE.map.addControl(new maplibregl.NavigationControl(), 'top-right');
  STATE.map.on('load', () => {
    console.log('Map loaded');
  });
}

function toggleMapStyle() {
  STATE.mapStyleIndex = (STATE.mapStyleIndex + 1) % MAP_STYLES.length;
  STATE.map.setStyle(MAP_STYLES[STATE.mapStyleIndex]);
}

// =============================================================================
// Map: Route zeichnen
// =============================================================================

function drawRouteOnMap(route) {
  clearRouteFromMap();
  if (!route.points || route.points.length < 2) return;

  const coords = route.points.map(p => [p.longitude, p.latitude]);
  const geojson = {
    type: 'Feature',
    geometry: { type: 'LineString', coordinates: coords },
    properties: {},
  };

  const layerId = 'route_' + route.id;
  STATE.map.addSource(layerId, { type: 'geojson', data: geojson });
  STATE.map.addLayer({
    id: layerId,
    type: 'line',
    source: layerId,
    paint: {
      'line-color': '#4CAF50',
      'line-width': 4,
      'line-opacity': 0.8,
    },
  });

  // Marker an Start und Ende
  const startMarker = new maplibregl.Marker({ color: '#4CAF50' })
    .setLngLat(coords[0])
    .addTo(STATE.map);
  const endMarker = new maplibregl.Marker({ color: '#F44336' })
    .setLngLat(coords[coords.length - 1])
    .addTo(STATE.map);

  STATE.routeLayerIds.push(layerId, startMarker, endMarker);

  // Karte zoomen
  const bounds = coords.reduce((b, c) => b.extend(c), new maplibregl.LngLatBounds(coords[0], coords[0]));
  STATE.map.fitBounds(bounds, { padding: 60, maxZoom: 14 });
}

function clearRouteFromMap() {
  STATE.routeLayerIds.forEach(item => {
    if (typeof item === 'string') {
      try { STATE.map.removeLayer(item); } catch {}
      try { STATE.map.removeSource(item); } catch {}
    } else if (item.remove) {
      item.remove();
    }
  });
  STATE.routeLayerIds = [];
}

// =============================================================================
// Map: Eigenen Standort anzeigen
// =============================================================================

function locateUser() {
  if (!navigator.geolocation) {
    alert('Geolocation wird nicht unterstützt.');
    return;
  }
  navigator.geolocation.getCurrentPosition(
    pos => {
      const { latitude, longitude } = pos.coords;
      updateUserMarker(latitude, longitude);
      STATE.map.flyTo({ center: [longitude, latitude], zoom: 12 });
    },
    () => alert('Standort konnte nicht ermittelt werden.'),
    { enableHighAccuracy: true, timeout: 10000 },
  );
}

function updateUserMarker(lat, lon) {
  if (STATE.marker) {
    STATE.marker.setLngLat([lon, lat]);
  } else {
    const el = document.createElement('div');
    el.className = 'user-marker';
    el.style.cssText = 'width:16px;height:16px;background:#1a73e8;border:3px solid white;border-radius:50%;box-shadow:0 0 4px rgba(0,0,0,0.4);';
    STATE.marker = new maplibregl.Marker({ element: el })
      .setLngLat([lon, lat])
      .addTo(STATE.map);
  }
}

// =============================================================================
// GPS-Tracking
// =============================================================================

function toggleTracking() {
  if (STATE.isTracking) {
    stopTracking();
  } else {
    startTracking();
  }
}

function startTracking() {
  if (!navigator.geolocation) {
    alert('Geolocation nicht verfügbar.');
    return;
  }
  STATE.trackingWatchId = navigator.geolocation.watchPosition(
    pos => {
      const { latitude, longitude } = pos.coords;
      updateUserMarker(latitude, longitude);
    },
    () => console.warn('Tracking position error'),
    { enableHighAccuracy: true, timeout: 5000, maximumAge: 10000 },
  );
  STATE.isTracking = true;
  DOM.trackingDot.className = 'tracking-dot active';
  DOM.trackingIndicator.className = 'tracking-indicator active';
  DOM.trackingLabel.textContent = 'Tracking aktiv';
}

function stopTracking() {
  if (STATE.trackingWatchId != null) {
    navigator.geolocation.clearWatch(STATE.trackingWatchId);
    STATE.trackingWatchId = null;
  }
  STATE.isTracking = false;
  DOM.trackingDot.className = 'tracking-dot inactive';
  DOM.trackingIndicator.className = 'tracking-indicator inactive';
  DOM.trackingLabel.textContent = 'Tracking aus';
}

// =============================================================================
// Route planen
// =============================================================================

function setRouteType(isR) {
  STATE.isRoundtrip = isR;
  if (isR) {
    DOM.typeRoundtripBtn.className = 'btn btn-sm btn-primary';
    DOM.typeDestinationBtn.className = 'btn btn-sm btn-secondary';
    show(DOM.roundtripFields);
    hide(DOM.destinationFields);
  } else {
    DOM.typeRoundtripBtn.className = 'btn btn-sm btn-secondary';
    DOM.typeDestinationBtn.className = 'btn btn-sm btn-primary';
    hide(DOM.roundtripFields);
    show(DOM.destinationFields);
  }
}

async function planRoute() {
  const body = { transportMode: DOM.transportMode.value, name: DOM.routeName.value.trim() };
  if (STATE.isRoundtrip) {
    const lat = parseFloat(DOM.rtStartLat.value);
    const lon = parseFloat(DOM.rtStartLon.value);
    const dist = parseFloat(DOM.rtDistance.value);
    if (isNaN(lat) || isNaN(lon) || isNaN(dist) || dist <= 0) {
      alert('Bitte gültige Startkoordinaten und Distanz eingeben.');
      return;
    }
    body.startLat = lat;
    body.startLon = lon;
    body.distanceKm = dist;
  } else {
    const slat = parseFloat(DOM.destStartLat.value);
    const slon = parseFloat(DOM.destStartLon.value);
    const elat = parseFloat(DOM.destEndLat.value);
    const elon = parseFloat(DOM.destEndLon.value);
    if (isNaN(slat) || isNaN(slon) || isNaN(elat) || isNaN(elon)) {
      alert('Bitte gültige Start- und Zielkoordinaten eingeben.');
      return;
    }
    body.startLat = slat;
    body.startLon = slon;
    body.endLat = elat;
    body.endLon = elon;
  }

  try {
    const endpoint = STATE.isRoundtrip ? '/routes/roundtrip' : '/routes/destination';
    const route = await api(endpoint, { method: 'POST', body });
    showRouteDetail(route);
    // Lade Routenliste neu
    loadRoutes();
  } catch (err) {
    alert('Route konnte nicht berechnet werden: ' + err.message);
  }
}

// =============================================================================
// Export (KML, GPX)
// =============================================================================

async function exportRoute(format) {
  if (!STATE.currentRouteId) return;
  try {
    const res = await api('/routes/' + STATE.currentRouteId + '/export/' + format);
    if (res instanceof Response) {
      const blob = await res.blob();
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'route_' + STATE.currentRouteId + '.' + format;
      a.click();
      URL.revokeObjectURL(url);
    } else {
      // Fallback: text response
      const blob = new Blob([res], { type: 'application/octet-stream' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'route_' + STATE.currentRouteId + '.' + format;
      a.click();
      URL.revokeObjectURL(url);
    }
  } catch (err) {
    alert('Export fehlgeschlagen: ' + err.message);
  }
}

// =============================================================================
// Bewertung
// =============================================================================

let pendingRatingValue = 0;

function showRatingPanel() {
  if (!STATE.currentRouteId) return;
  pendingRatingValue = 0;
  DOM.ratingStars.querySelectorAll('.rating-star').forEach(s => s.classList.remove('selected'));
  DOM.ratingComment.value = '';
  show(DOM.ratingPanel);
}

function hideRatingPanel() {
  hide(DOM.ratingPanel);
}

async function submitRating() {
  if (!STATE.currentRouteId || pendingRatingValue === 0) {
    alert('Bitte eine Bewertung auswählen (1-5 Sterne).');
    return;
  }
  try {
    await api('/routes/' + STATE.currentRouteId + '/ratings', {
      method: 'POST',
      body: { score: pendingRatingValue, comment: DOM.ratingComment.value.trim() },
    });
    alert('Bewertung gespeichert!');
    hideRatingPanel();
  } catch (err) {
    alert('Bewertung fehlgeschlagen: ' + err.message);
  }
}

// =============================================================================
// Teilen
// =============================================================================

function showShareModal() {
  if (!STATE.currentRouteId) return;
  const url = window.location.origin + '/?route=' + STATE.currentRouteId;
  DOM.shareUrlInput.value = url;
  show(DOM.shareModal);
}

function hideShareModal() {
  hide(DOM.shareModal);
}

function copyShareUrl() {
  DOM.shareUrlInput.select();
  document.execCommand('copy');
  alert('Link kopiert!');
}

// =============================================================================
// SEPA-Mandat
// =============================================================================

async function loadSepaMandate() {
  if (!STATE.authToken) return;
  try {
    const mandate = await api('/payment/sepa-mandate');
    if (mandate) {
      DOM.sepaIbanDisplay.textContent = mandate.ibanMasked;
      DOM.sepaRefDisplay.textContent = mandate.mandateReference;
      const statusBadge = DOM.sepaStatusBadge;
      statusBadge.textContent = mandate.status;
      statusBadge.className = 'badge-' + (mandate.status === 'ACTIVE' ? 'roundtrip' : 'destination');
      hide(DOM.sepaForm);
      show(DOM.sepaActiveInfo);
    } else {
      show(DOM.sepaForm);
      hide(DOM.sepaActiveInfo);
    }
  } catch {
    // 404 = kein Mandat
    show(DOM.sepaForm);
    hide(DOM.sepaActiveInfo);
  }
}

async function createSepaMandate() {
  const accountHolder = DOM.sepaAccountHolder.value.trim();
  const iban = DOM.ibanInput.value.trim();
  const bic = DOM.bicInput.value.trim();
  const bankName = DOM.bankNameInput.value.trim();
  const consent = DOM.sepaConsent.checked;

  if (!accountHolder) { alert('Kontoinhaber eingeben.'); return; }
  if (!iban) { alert('IBAN eingeben.'); return; }
  if (!consent) { alert('Bitte den SEPA-Bedingungen zustimmen.'); return; }

  try {
    const mandate = await api('/payment/sepa-mandate', {
      method: 'POST',
      body: { accountHolder, iban, bic: bic || undefined, bankName: bankName || undefined, acceptedTerms: true },
    });
    DOM.sepaIbanDisplay.textContent = mandate.ibanMasked;
    DOM.sepaRefDisplay.textContent = mandate.mandateReference;
    DOM.sepaStatusBadge.textContent = mandate.status;
    DOM.sepaStatusBadge.className = 'badge-roundtrip';
    hide(DOM.sepaForm);
    show(DOM.sepaActiveInfo);
    alert('SEPA-Mandat erfolgreich erteilt!');
  } catch (err) {
    alert('Fehler bei Mandatserstellung: ' + err.message);
  }
}

async function revokeSepaMandate() {
  if (!confirm('SEPA-Mandat wirklich widerrufen?')) return;
  try {
    const mandate = await api('/payment/sepa-mandate/revoke', { method: 'PUT' });
    DOM.sepaIbanDisplay.textContent = mandate.ibanMasked;
    DOM.sepaRefDisplay.textContent = mandate.mandateReference;
    DOM.sepaStatusBadge.textContent = mandate.status;
    DOM.sepaStatusBadge.className = 'badge-destination';
    alert('SEPA-Mandat widerrufen.');
  } catch (err) {
    alert('Widerruf fehlgeschlagen: ' + err.message);
  }
}

// =============================================================================
// Wetter
// =============================================================================

async function loadWeather(lat, lon) {
  show(DOM.weatherLoading);
  hide(DOM.weatherContent);
  try {
    const w = await api('/weather?lat=' + lat + '&lon=' + lon);
    DOM.weatherTemp.textContent = w.temperature != null ? Math.round(w.temperature) + '°C' : '-°C';
    const iconMap = { SUNNY: '☀️', CLOUDY: '☁️', RAINY: '🌧️', SNOWY: '❄️', FOGGY: '🌫️', STORMY: '⛈️' };
    DOM.weatherIcon.textContent = iconMap[w.condition] || '🌤️';
    DOM.weatherPrecip.textContent = w.precipitation != null ? w.precipitation.toFixed(1) : '-';
    DOM.weatherWind.textContent = w.windSpeed != null ? Math.round(w.windSpeed) : '-';
    hide(DOM.weatherLoading);
    show(DOM.weatherContent);
  } catch {
    hide(DOM.weatherLoading);
  }
}

// =============================================================================
// Tab-Switching
// =============================================================================

function switchTab(tabName) {
  DOM.tabBar.querySelectorAll('.tab-btn').forEach(b => b.classList.toggle('active', b.dataset.tab === tabName));
  DOM.tabContent.querySelectorAll('.tab-pane').forEach(p => p.classList.toggle('active', p.id === 'tab' + tabName.charAt(0).toUpperCase() + tabName.slice(1)));
  if (tabName === 'routes') hide(DOM.detailPanel);
}

// =============================================================================
// =============================================================================
// Init
// =============================================================================

function init() {
  // DOM-Referenzen sammeln
  DOM = {
    // Sidebar / Layout
    sidebar: $('sidebar'),
    menuToggleBtn: $('menuToggleBtn'),

    tabBar: $('tabBar'),
    tabContent: $('tabContent'),
    // Routes Tab
    routeList: $('routeList'),
    routeEmpty: $('routeEmpty'),
    refreshRoutesBtn: $('refreshRoutesBtn'),
    detailPanel: $('detailPanel'),
    detailName: $('detailName'),
    detailType: $('detailType'),
    detailMode: $('detailMode'),
    detailDistance: $('detailDistance'),
    detailDuration: $('detailDuration'),
    detailScore: $('detailScore'),
    detailVisibility: $('detailVisibility'),
    closeDetailBtn: $('closeDetailBtn'),
    showOnMapBtn: $('showOnMapBtn'),
    exportKmlBtn: $('exportKmlBtn'),
    exportGpxBtn: $('exportGpxBtn'),
    shareRouteBtn: $('shareRouteBtn'),
    rateRouteBtn: $('rateRouteBtn'),
    deleteRouteBtn: $('deleteRouteBtn'),
    ratingPanel: $('ratingPanel'),
    ratingStars: $('ratingStars'),
    ratingComment: $('ratingComment'),
    submitRatingBtn: $('submitRatingBtn'),
    cancelRatingBtn: $('cancelRatingBtn'),
    // Plan Tab
    typeRoundtripBtn: $('typeRoundtripBtn'),
    typeDestinationBtn: $('typeDestinationBtn'),
    roundtripFields: $('roundtripFields'),
    destinationFields: $('destinationFields'),
    rtStartLat: $('rtStartLat'),
    rtStartLon: $('rtStartLon'),
    rtDistance: $('rtDistance'),
    destStartLat: $('destStartLat'),
    destStartLon: $('destStartLon'),
    destEndLat: $('destEndLat'),
    destEndLon: $('destEndLon'),
    transportMode: $('transportMode'),
    routeName: $('routeName'),
    planRouteBtn: $('planRouteBtn'),
    // Profile Tab
    profileEmail: $('profileEmail'),
    profilePassword: $('profilePassword'),
    profileLoginBtn: $('profileLoginBtn'),
    sepaStatus: $('sepaStatus'),
    sepaForm: $('sepaForm'),
    sepaAccountHolder: $('sepaAccountHolder'),
    ibanInput: $('ibanInput'),
    bicInput: $('bicInput'),
    bankNameInput: $('bankNameInput'),
    sepaConsent: $('sepaConsent'),
    createSepaBtn: $('createSepaBtn'),
    sepaActiveInfo: $('sepaActiveInfo'),
    sepaIbanDisplay: $('sepaIbanDisplay'),
    sepaRefDisplay: $('sepaRefDisplay'),
    sepaStatusBadge: $('sepaStatusBadge'),
    revokeSepaBtn: $('revokeSepaBtn'),
    // Tracking
    trackingIndicator: $('trackingIndicator'),
    trackingDot: $('trackingDot'),
    trackingLabel: $('trackingLabel'),
    locateMeBtn: $('locateMeBtn'),
    // Map Controls
    mapContainer: $('mapContainer'),
    locateMapBtn: $('locateMapBtn'),
    toggleTrackingBtn: $('toggleTrackingBtn'),
    toggleMapTypeBtn: $('toggleMapTypeBtn'),
    // Wetter
    weatherOverlay: $('weatherOverlay'),
    weatherLoading: $('weatherLoading'),
    weatherContent: $('weatherContent'),
    weatherTemp: $('weatherTemp'),
    weatherIcon: $('weatherIcon'),
    weatherPrecip: $('weatherPrecip'),
    weatherWind: $('weatherWind'),
    // Share Modal
    shareModal: $('shareModal'),
    shareUrlInput: $('shareUrlInput'),
    copyShareUrlBtn: $('copyShareUrlBtn'),
    closeShareModalBtn: $('closeShareModalBtn'),
  };

  // Karte initialisieren
  initMap();

  // Auth laden
  loadAuth();


  // -------------------------------------------------------
  // Event-Handler
  // -------------------------------------------------------

  // Tab-Switching
  DOM.tabBar.addEventListener('click', e => {
    const btn = e.target.closest('.tab-btn');
    if (btn) switchTab(btn.dataset.tab);
  });

  // Profil Login
  DOM.profileLoginBtn.addEventListener('click', handleLogin);

  // Routen-Tab
  DOM.refreshRoutesBtn.addEventListener('click', loadRoutes);
  DOM.closeDetailBtn.addEventListener('click', () => { hide(DOM.detailPanel); clearRouteFromMap(); });
  DOM.showOnMapBtn.addEventListener('click', () => {
    if (STATE.currentRouteId) {
      // Route ist bereits auf der Karte – einfach Karte fokussieren
      switchTab('routes');
    }
  });
  DOM.exportKmlBtn.addEventListener('click', () => exportRoute('kml'));
  DOM.exportGpxBtn.addEventListener('click', () => exportRoute('gpx'));
  DOM.shareRouteBtn.addEventListener('click', showShareModal);
  DOM.rateRouteBtn.addEventListener('click', showRatingPanel);
  DOM.deleteRouteBtn.addEventListener('click', deleteRoute);

  // Rating
  DOM.ratingStars.addEventListener('click', e => {
    const star = e.target.closest('.rating-star');
    if (!star) return;
    pendingRatingValue = parseInt(star.dataset.value, 10);
    DOM.ratingStars.querySelectorAll('.rating-star').forEach(s => {
      s.classList.toggle('selected', parseInt(s.dataset.value, 10) <= pendingRatingValue);
    });
  });
  DOM.submitRatingBtn.addEventListener('click', submitRating);
  DOM.cancelRatingBtn.addEventListener('click', hideRatingPanel);

  // Route planen
  DOM.typeRoundtripBtn.addEventListener('click', () => setRouteType(true));
  DOM.typeDestinationBtn.addEventListener('click', () => setRouteType(false));
  DOM.planRouteBtn.addEventListener('click', planRoute);

  // SEPA
  DOM.createSepaBtn.addEventListener('click', createSepaMandate);
  DOM.revokeSepaBtn.addEventListener('click', revokeSepaMandate);

  // IBAN-Formatierung
  DOM.ibanInput.addEventListener('input', () => {
    let val = DOM.ibanInput.value.replace(/[^A-Za-z0-9]/g, '').toUpperCase();
    if (val.length > 22) val = val.substring(0, 22);
    // Füge alle 4 Zeichen Leerzeichen ein
    const parts = [];
    for (let i = 0; i < val.length; i += 4) {
      parts.push(val.substring(i, i + 4));
    }
    DOM.ibanInput.value = parts.join(' ');
  });

  // Kartensteuerung
  DOM.locateMapBtn.addEventListener('click', locateUser);
  DOM.toggleTrackingBtn.addEventListener('click', toggleTracking);
  DOM.toggleMapTypeBtn.addEventListener('click', toggleMapStyle);
  DOM.locateMeBtn.addEventListener('click', locateUser);

  // Share Modal
  DOM.copyShareUrlBtn.addEventListener('click', copyShareUrl);
  DOM.closeShareModalBtn.addEventListener('click', hideShareModal);
  window.addEventListener('click', e => {
    if (e.target === DOM.shareModal) hideShareModal();
  });

  // Karten-Idle: Wetter laden
  let weatherTimeout;
  STATE.map.on('moveend', () => {
    clearTimeout(weatherTimeout);
    weatherTimeout = setTimeout(() => {
      const center = STATE.map.getCenter();
      loadWeather(center.lat, center.lng);
    }, 800);
  });

  // Sidebar-Toggle (mobil)
  DOM.menuToggleBtn.addEventListener('click', () => {
    DOM.sidebar.classList.toggle('sidebar-collapsed');
  });

  console.log('Pathiful App initialized.');
}

// Start after DOM ready
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', init);
} else {
  init();
}
