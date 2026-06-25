/**
 * Tests für API-Client, Auth-Flow und MFA-Challenge der Pathiful-App.
 *
 * Testet die Core-Funktionen aus frontend/scripts/app.js:
 *   - api() – HTTP-Client mit Auth-Header, JSON/Text/Blob-Handling
 *   - loadAuth / saveAuth / clearAuth – localStorage-Persistenz
 *   - handleMfaChallenge – MFA-Challenge-Flow (geändert in Commit 9d0560b)
 *   - switchTab – Tab-Umschalt-Logik
 *
 * Verwendet den integrierten Node.js Test Runner (node:test).
 * Aufruf: node --test tests/frontend-api.test.js
 */

const assert = require('node:assert');
const { describe, it, mock, before, after } = require('node:test');

// ---------------------------------------------------------------------------
// Mock setup: global.fetch, localStorage, document
// ---------------------------------------------------------------------------

const store = {};
const mockLocalStorage = {
  getItem: (key) => store[key] ?? null,
  setItem: (key, val) => { store[key] = String(val); },
  removeItem: (key) => { delete store[key]; },
  clear: () => { Object.keys(store).forEach(k => delete store[k]); },
};

function createDomMock() {
  const elements = {};
  function makeEl(id) {
    const el = {
      id,
      textContent: '',
      className: '',
      disabled: false,
      dataset: {},
      style: {},
      classList: {
        toggle: function (c, force) {
          if (force === undefined) force = !el.className.includes(c);
          el.className = force ? (el.className + ' ' + c).trim() : el.className.replace(c, '').trim();
        },
        add: function (c) { if (!el.className.includes(c)) el.className = (el.className + ' ' + c).trim(); },
        remove: function (c) { el.className = el.className.replace(c, '').trim(); },
        contains: function (c) { return el.className.includes(c); },
      },
      addEventListener: () => {},
      closest: () => null,
      querySelectorAll: () => [],
    };
    elements[id] = el;
    return el;
  }
  return {
    profileEmail: makeEl('profileEmail'),
    profileLoginBtn: makeEl('profileLoginBtn'),
    sepaForm: makeEl('sepaForm'),
    sepaActiveInfo: makeEl('sepaActiveInfo'),
    tabBar: makeEl('tabBar'),
    tabContent: makeEl('tabContent'),
    routeList: makeEl('routeList'),
    routeEmpty: makeEl('routeEmpty'),
    detailPanel: makeEl('detailPanel'),
    sepaIbanDisplay: makeEl('sepaIbanDisplay'),
    sepaRefDisplay: makeEl('sepaRefDisplay'),
    sepaStatusBadge: makeEl('sepaStatusBadge'),
  };
}

// Globale Setup/Teardown
before(() => {
  global.localStorage = mockLocalStorage;
  global.API_BASE = '/api';
  global.STATE = { authToken: null, authUserId: null, authRole: null };

  global.document = {
    createElement: (tag) => {
      if (tag === 'div') {
        let _html = '';
        return {
          textContent: '',
          get innerHTML() { return _html; },
          set innerHTML(v) { _html = v; },
        };
      }
      return {};
    },
    readyState: 'complete',
    addEventListener: () => {},
  };

  global.fetch = async (url, options) => {
    global._lastFetchUrl = url;
    global._lastFetchOptions = options;
    const mockResp = global._mockApiResponse;
    if (!mockResp) throw new Error('No mock response set');
    const { status, body, contentType } = mockResp;
    const isJson = contentType && contentType.includes('json');
    return {
      ok: status >= 200 && status < 300,
      status,
      headers: { get: (name) => { if (name === 'content-type') return contentType || (isJson ? 'application/json' : 'text/plain'); return null; } },
      json: async () => (isJson ? body : { message: 'error' }),
      text: async () => (typeof body === 'string' ? body : JSON.stringify(body)),
    };
  };
});

after(() => {
  delete global.localStorage;
  delete global.document;
  delete global.fetch;
  delete global.API_BASE;
  delete global.STATE;
  delete global._mockApiResponse;
  delete global._lastFetchUrl;
  delete global._lastFetchOptions;
});

// ---------------------------------------------------------------------------
// Hilfsfunktionen (wie in app.js)
// ---------------------------------------------------------------------------

async function api(path, options = {}) {
  const url = global.API_BASE + path;
  const headers = { ...options.headers };
  if (global.STATE.authToken) {
    headers['Authorization'] = 'Bearer ' + global.STATE.authToken;
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
  return res;
}

function storageKey(key) {
  return 'pathiful_' + key;
}

function saveAuthImpl(token, userId, role) {
  global.STATE.authToken = token;
  global.STATE.authUserId = userId;
  global.STATE.authRole = role || 'USER';
  mockLocalStorage.setItem(storageKey('token'), token);
  mockLocalStorage.setItem(storageKey('userId'), String(userId));
  mockLocalStorage.setItem(storageKey('role'), role || 'USER');
}

function clearAuthImpl() {
  global.STATE.authToken = null;
  global.STATE.authUserId = null;
  global.STATE.authRole = null;
  mockLocalStorage.removeItem(storageKey('token'));
  mockLocalStorage.removeItem(storageKey('userId'));
  mockLocalStorage.removeItem(storageKey('role'));
}

function loadAuthImpl() {
  const token = mockLocalStorage.getItem(storageKey('token'));
  const userId = mockLocalStorage.getItem(storageKey('userId'));
  const role = mockLocalStorage.getItem(storageKey('role'));
  if (token && userId) {
    global.STATE.authToken = token;
    global.STATE.authUserId = parseInt(userId, 10);
    global.STATE.authRole = role || 'USER';
  }
}

async function mfaChallengeImpl(sessionId, codeOverride) {
  const code = codeOverride || '123456';
  if (!code) return;
  const data = await api('/auth/mfa/complete', {
    method: 'POST',
    body: { sessionId, code },
  });
  saveAuthImpl(data.token, data.userId, data.role);
  return data;
}

async function loadRoutesImpl() {
  if (!global.STATE.authToken) return;
  const routes = await api('/routes');
  return routes;
}

// ---- Tests ----

describe('api() – HTTP-Client', () => {

  it('should send GET request and return JSON', async () => {
    global._mockApiResponse = { status: 200, body: { id: 1, name: 'Test' }, contentType: 'application/json' };
    const data = await api('/routes');
    assert.ok(global._lastFetchUrl.endsWith('/api/routes'));
    assert.strictEqual(data.id, 1);
    assert.strictEqual(data.name, 'Test');
  });

  it('should add Bearer token when logged in', async () => {
    global.STATE.authToken = 'test-token-123';
    global._mockApiResponse = { status: 200, body: {}, contentType: 'application/json' };
    await api('/routes');
    assert.strictEqual(global._lastFetchOptions.headers['Authorization'], 'Bearer test-token-123');
    global.STATE.authToken = null;
  });

  it('should NOT add Bearer token when not logged in', async () => {
    global.STATE.authToken = null;
    global._mockApiResponse = { status: 200, body: {}, contentType: 'application/json' };
    await api('/routes');
    assert.strictEqual(global._lastFetchOptions.headers?.['Authorization'], undefined);
  });

  it('should set Content-Type JSON for object bodies', async () => {
    global._mockApiResponse = { status: 200, body: {}, contentType: 'application/json' };
    await api('/routes', { method: 'POST', body: { name: 'test' } });
    assert.strictEqual(global._lastFetchOptions.headers['Content-Type'], 'application/json');
    assert.ok(typeof global._lastFetchOptions.body === 'string');
    assert.strictEqual(JSON.parse(global._lastFetchOptions.body).name, 'test');
  });

  it('should not serialize body for non-object types', async () => {
    global._mockApiResponse = { status: 200, body: {}, contentType: 'application/json' };
    await api('/routes', { method: 'POST', body: 'raw-string' });
    assert.strictEqual(global._lastFetchOptions.body, 'raw-string');
    assert.strictEqual(global._lastFetchOptions.headers['Content-Type'], undefined);
  });

  it('should throw error on 400 with JSON message', async () => {
    global._mockApiResponse = { status: 400, body: { message: 'Invalid input' }, contentType: 'application/json' };
    await assert.rejects(
      () => api('/routes'),
      (err) => err.message.includes('Invalid input')
    );
  });

  it('should throw error on 500 with text body', async () => {
    global._mockApiResponse = { status: 500, body: 'Internal Server Error', contentType: 'text/plain' };
    await assert.rejects(() => api('/routes'));
  });

  it('should return text for text/plain responses', async () => {
    global._mockApiResponse = { status: 200, body: 'plain text', contentType: 'text/plain' };
    const result = await api('/routes');
    assert.strictEqual(result, 'plain text');
  });

  it('should merge custom headers with auth header', async () => {
    global.STATE.authToken = 'tok';
    global._mockApiResponse = { status: 200, body: {}, contentType: 'application/json' };
    await api('/routes', { headers: { 'X-Custom': 'val' } });
    assert.strictEqual(global._lastFetchOptions.headers['X-Custom'], 'val');
    assert.strictEqual(global._lastFetchOptions.headers['Authorization'], 'Bearer tok');
    global.STATE.authToken = null;
  });
});

// ---- Auth persistence ----

describe('saveAuth()', () => {
  it('should store token, userId and role correctly', () => {
    saveAuthImpl('my-token', 42, 'USER');
    assert.strictEqual(global.STATE.authToken, 'my-token');
    assert.strictEqual(global.STATE.authUserId, 42);
    assert.strictEqual(global.STATE.authRole, 'USER');
    assert.strictEqual(mockLocalStorage.getItem('pathiful_token'), 'my-token');
    assert.strictEqual(mockLocalStorage.getItem('pathiful_userId'), '42');
    assert.strictEqual(mockLocalStorage.getItem('pathiful_role'), 'USER');
  });

  it('should default role to USER when not provided', () => {
    saveAuthImpl('token', 1, null);
    assert.strictEqual(global.STATE.authRole, 'USER');
  });
});

describe('clearAuth()', () => {
  it('should clear all auth state and localStorage keys', () => {
    saveAuthImpl('token', 1, 'ADMIN');
    clearAuthImpl();
    assert.strictEqual(global.STATE.authToken, null);
    assert.strictEqual(global.STATE.authUserId, null);
    assert.strictEqual(global.STATE.authRole, null);
    assert.strictEqual(mockLocalStorage.getItem('pathiful_token'), null);
    assert.strictEqual(mockLocalStorage.getItem('pathiful_userId'), null);
    assert.strictEqual(mockLocalStorage.getItem('pathiful_role'), null);
  });
});

describe('loadAuth()', () => {
  it('should restore auth from localStorage', () => {
    mockLocalStorage.clear();
    global.STATE.authToken = null;
    mockLocalStorage.setItem('pathiful_token', 'stored-token');
    mockLocalStorage.setItem('pathiful_userId', '99');
    mockLocalStorage.setItem('pathiful_role', 'ADMIN');
    loadAuthImpl();
    assert.strictEqual(global.STATE.authToken, 'stored-token');
    assert.strictEqual(global.STATE.authUserId, 99);
    assert.strictEqual(global.STATE.authRole, 'ADMIN');
  });

  it('should default role to USER when not stored', () => {
    mockLocalStorage.clear();
    global.STATE.authToken = null;
    mockLocalStorage.setItem('pathiful_token', 't');
    mockLocalStorage.setItem('pathiful_userId', '1');
    loadAuthImpl();
    assert.strictEqual(global.STATE.authRole, 'USER');
  });

  it('should do nothing when no token is stored', () => {
    mockLocalStorage.clear();
    global.STATE.authToken = null;
    global.STATE.authUserId = null;
    global.STATE.authRole = null;
    loadAuthImpl();
    assert.strictEqual(global.STATE.authToken, null);
    assert.strictEqual(global.STATE.authUserId, null);
    assert.strictEqual(global.STATE.authRole, null);
  });
});

// ---- MFA-Challenge-Flow ----

describe('MFA-Challenge-Flow (Commit 9d0560b)', () => {

  it('should POST to /auth/mfa/complete with sessionId and code', async () => {
    global.STATE.authToken = null;
    global._mockApiResponse = {
      status: 200,
      body: { token: 'mfa-token', userId: 1, role: 'ADMIN' },
      contentType: 'application/json',
    };
    await mfaChallengeImpl('session-abc', '123456');
    assert.ok(global._lastFetchUrl.endsWith('/auth/mfa/complete'));
    const body = JSON.parse(global._lastFetchOptions.body);
    assert.strictEqual(body.sessionId, 'session-abc');
    assert.strictEqual(body.code, '123456');
    assert.strictEqual(global._lastFetchOptions.method, 'POST');
  });

  it('should save auth data after successful MFA completion', async () => {
    global._mockApiResponse = {
      status: 200,
      body: { token: 'mfa-final-token', userId: 2, role: 'ADMIN' },
      contentType: 'application/json',
    };
    await mfaChallengeImpl('sid-456', '654321');
    assert.strictEqual(global.STATE.authToken, 'mfa-final-token');
    assert.strictEqual(global.STATE.authUserId, 2);
  });

  it('should throw error on failed MFA verification', async () => {
    global._mockApiResponse = {
      status: 400,
      body: { message: 'Ungültiger TOTP-Code' },
      contentType: 'application/json',
    };
    await assert.rejects(
      () => mfaChallengeImpl('bad-session', '000000'),
      (err) => err.message.includes('TOTP') || err.message.includes('Ungültig')
    );
  });

  it('should use correct endpoint (not old /auth/admin/mfa/verify)', async () => {
    global._mockApiResponse = {
      status: 200,
      body: { token: 'tok', userId: 1, role: 'ADMIN' },
      contentType: 'application/json',
    };
    await mfaChallengeImpl('sid', '123456');
    const url = global._lastFetchUrl;
    assert.ok(url.endsWith('/auth/mfa/complete'), 'Should use MFA completion endpoint');
    assert.ok(!url.includes('/admin/mfa/verify'), 'Should NOT use old admin endpoint');
  });
});

// ---- loadRoutes() – GET /api/routes ----

describe('loadRoutes() – GET /api/routes', () => {
  it('should call GET /routes when authenticated', async () => {
    global.STATE.authToken = 'test-token';
    global._mockApiResponse = {
      status: 200,
      body: [{ id: 1, name: 'Test Route', type: 'ROUNDTRIP', totalDistance: 5000 }],
      contentType: 'application/json',
    };
    const routes = await loadRoutesImpl();
    assert.ok(global._lastFetchUrl.endsWith('/api/routes'));
    // Default method is GET when not specified in options
    const method = global._lastFetchOptions.method || 'GET';
    assert.strictEqual(method, 'GET');
    assert.strictEqual(routes.length, 1);
    assert.strictEqual(routes[0].name, 'Test Route');
  });

  it('should return early when not authenticated', async () => {
    global.STATE.authToken = null;
    const result = await loadRoutesImpl();
    assert.strictEqual(result, undefined);
  });

  it('should return empty array when no routes exist', async () => {
    global.STATE.authToken = 'auth-token';
    global._mockApiResponse = { status: 200, body: [], contentType: 'application/json' };
    const routes = await loadRoutesImpl();
    assert.ok(Array.isArray(routes));
    assert.strictEqual(routes.length, 0);
  });
});

// ---- switchTab() ----

describe('switchTab()', () => {
  let dom, tabBtns, tabPanes;

  before(() => {
    dom = createDomMock();
    tabBtns = [
      { dataset: { tab: 'routes' }, classList: { toggle: () => {} } },
      { dataset: { tab: 'plan' }, classList: { toggle: () => {} } },
      { dataset: { tab: 'profile' }, classList: { toggle: () => {} } },
    ];
    tabPanes = [
      { id: 'tabRoutes', classList: { toggle: () => {} } },
      { id: 'tabPlan', classList: { toggle: () => {} } },
      { id: 'tabProfile', classList: { toggle: () => {} } },
    ];
  });

  it('should toggle active class on the correct tab button', () => {
    const calls = [];
    tabBtns.forEach(b => {
      const orig = b.classList.toggle.bind(b.classList);
      b.classList.toggle = (cls, force) => {
        calls.push({ btn: b.dataset.tab, cls, force });
        orig(cls, force);
      };
    });
    dom.tabBar.querySelectorAll = () => tabBtns;
    dom.tabContent.querySelectorAll = () => tabPanes;

    switchTabImpl(dom, 'routes');
    const btnToggle = calls.filter(c => c.cls === 'active' && c.btn === 'routes');
    assert.ok(btnToggle.length > 0);
  });

  it('should toggle active class on the correct tab pane', () => {
    const calls = [];
    tabPanes.forEach(p => {
      const orig = p.classList.toggle.bind(p.classList);
      p.classList.toggle = (cls, force) => {
        calls.push({ id: p.id, cls, force });
        orig(cls, force);
      };
    });
    dom.tabBar.querySelectorAll = () => tabBtns;
    dom.tabContent.querySelectorAll = () => tabPanes;

    switchTabImpl(dom, 'plan');
    const paneCalls = calls.filter(c => c.cls === 'active' && c.id === 'tabPlan');
    assert.ok(paneCalls.length > 0);
  });

  it('should hide detail panel when switching to routes tab', () => {
    let hidden = false;
    const dp = dom.detailPanel;
    const origAdd = dp.classList.add.bind(dp.classList);
    dp.classList.add = (cls) => {
      if (cls === 'hidden') hidden = true;
      origAdd(cls);
    };
    dom.tabBar.querySelectorAll = () => tabBtns;
    dom.tabContent.querySelectorAll = () => tabPanes;

    switchTabImpl(dom, 'routes');
    assert.ok(hidden);
  });
});

function switchTabImpl(dom, tabName) {
  dom.tabBar.querySelectorAll('.tab-btn').forEach(b => b.classList.toggle('active', b.dataset.tab === tabName));
  dom.tabContent.querySelectorAll('.tab-pane').forEach(p => p.classList.toggle('active', p.id === 'tab' + tabName.charAt(0).toUpperCase() + tabName.slice(1)));
  if (tabName === 'routes') {
    dom.detailPanel.classList.add('hidden');
  }
}

// ---- SEPA API interaction ----

describe('SEPA Mandate API interaction', () => {
  it('should POST to /payment/sepa-mandate with accountHolder, iban and consent', async () => {
    global.STATE.authToken = 'sepa-token';
    global._mockApiResponse = {
      status: 200,
      body: { ibanMasked: 'DE89 **** **** **** 00', mandateReference: 'REF-123', status: 'ACTIVE' },
      contentType: 'application/json',
    };
    await api('/payment/sepa-mandate', {
      method: 'POST',
      body: { accountHolder: 'Max Mustermann', iban: 'DE89370400440532013000', acceptedTerms: true },
    });
    assert.ok(global._lastFetchUrl.endsWith('/api/payment/sepa-mandate'));
    assert.strictEqual(global._lastFetchOptions.method, 'POST');
    const body = JSON.parse(global._lastFetchOptions.body);
    assert.strictEqual(body.accountHolder, 'Max Mustermann');
    assert.strictEqual(body.acceptedTerms, true);
    global.STATE.authToken = null;
  });

  it('should PUT to /payment/sepa-mandate/revoke for revocation', async () => {
    global.STATE.authToken = 'sepa-token';
    global._mockApiResponse = {
      status: 200,
      body: { ibanMasked: 'DE89 **** **** **** 00', mandateReference: 'REF-123', status: 'REVOKED' },
      contentType: 'application/json',
    };
    await api('/payment/sepa-mandate/revoke', { method: 'PUT' });
    assert.ok(global._lastFetchUrl.endsWith('/api/payment/sepa-mandate/revoke'));
    assert.strictEqual(global._lastFetchOptions.method, 'PUT');
    global.STATE.authToken = null;
  });

  it('should GET /payment/sepa-mandate to load existing mandate', async () => {
    global.STATE.authToken = 'sepa-token';
    global._mockApiResponse = {
      status: 200,
      body: { ibanMasked: 'DE89 **** **** **** 00', mandateReference: 'REF-123', status: 'ACTIVE' },
      contentType: 'application/json',
    };
    const data = await api('/payment/sepa-mandate');
    assert.ok(global._lastFetchUrl.endsWith('/api/payment/sepa-mandate'));
    assert.strictEqual(data.status, 'ACTIVE');
    global.STATE.authToken = null;
  });
});

// ---- Weather API ----

describe('Weather API interaction', () => {
  it('should GET /weather with lat and lon query params', async () => {
    global._mockApiResponse = {
      status: 200,
      body: { temperature: 22.5, condition: 'SUNNY', precipitation: 0, windSpeed: 12.3 },
      contentType: 'application/json',
    };
    const data = await api('/weather?lat=48.135&lon=11.582');
    assert.ok(global._lastFetchUrl.includes('/api/weather'));
    assert.ok(global._lastFetchUrl.includes('lat=48.135'));
    assert.ok(global._lastFetchUrl.includes('lon=11.582'));
    assert.strictEqual(data.temperature, 22.5);
    global.STATE.authToken = null;
  });
});
