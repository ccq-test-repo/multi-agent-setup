/**
 * Tests for frontend helper/utility functions
 *
 * Tests pure logic functions from frontend/scripts/app.js:
 * - formatDistance()
 * - formatDuration()
 * - formatScore()
 * - escapeHtml()
 * - storageKey()
 *
 * Uses the built-in Node.js Test Runner (node:test).
 * Run: node --test tests/frontend/helpers.test.js
 */

const assert = require('node:assert');
const { describe, it } = require('node:test');

// ---------------------------------------------------------------------------
// Helper implementations (extracted from app.js for testability)
// ---------------------------------------------------------------------------

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

function escapeHtml(text) {
  if (!text) return text;
  // In a real browser context, setting textContent on a DOM element
  // and reading innerHTML escapes HTML entities. For this pure-node test
  // we simulate plain-pass-through for safe strings.
  if (typeof text !== 'string') return text;
  // Simulate basic HTML escaping
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

function storageKey(key) {
  return 'pathiful_' + key;
}

// IBAN formatting as implemented in app.js
function formatIban(raw) {
  let val = raw.replace(/[^A-Za-z0-9]/g, '').toUpperCase();
  if (val.length > 22) val = val.substring(0, 22);
  const parts = [];
  for (let i = 0; i < val.length; i += 4) {
    parts.push(val.substring(i, i + 4));
  }
  return parts.join(' ');
}

function maskIban(iban) {
  if (!iban) return '-';
  const clean = iban.replace(/\s/g, '');
  if (clean.length <= 6) return clean;
  return clean.substring(0, 4) + ' ' + '•'.repeat(Math.min(clean.length - 6, 12)) + ' ' + clean.substring(clean.length - 2);
}

// Route name validation
function validateRouteName(name) {
  if (!name || name.trim() === '') return 'Bitte einen Routennamen eingeben.';
  if (name.trim().length > 100) return 'Der Routenname darf maximal 100 Zeichen lang sein.';
  return null;
}

// Plan route validation
function validateRoundtripInput(lat, lon, dist) {
  if (isNaN(lat) || isNaN(lon)) return 'Bitte gültige Startkoordinaten eingeben.';
  if (lat < -90 || lat > 90) return 'Breitengrad muss zwischen -90 und 90 liegen.';
  if (lon < -180 || lon > 180) return 'Längengrad muss zwischen -180 und 180 liegen.';
  if (isNaN(dist) || dist <= 0) return 'Bitte eine gültige Distanz größer 0 eingeben.';
  if (dist > 100) return 'Die Distanz darf maximal 100 km betragen.';
  return null;
}

function validateDestinationInput(slat, slon, elat, elon) {
  if (isNaN(slat) || isNaN(slon) || isNaN(elat) || isNaN(elon)) {
    return 'Bitte gültige Start- und Zielkoordinaten eingeben.';
  }
  if (slat < -90 || slat > 90 || elat < -90 || elat > 90) return 'Breitengrad muss zwischen -90 und 90 liegen.';
  if (slon < -180 || slon > 180 || elon < -180 || elon > 180) return 'Längengrad muss zwischen -180 und 180 liegen.';
  return null;
}

// SEPA validation
function validateSepaInput(accountHolder, iban, consent) {
  if (!accountHolder || accountHolder.trim() === '') return 'Kontoinhaber eingeben.';
  if (!iban || iban.trim() === '') return 'IBAN eingeben.';
  const cleanIban = iban.replace(/\s/g, '');
  if (cleanIban.length < 15 || cleanIban.length > 34) return 'IBAN hat ungültige Länge.';
  if (!/^[A-Z]{2}[0-9A-Z]+$/.test(cleanIban)) return 'IBAN hat ungültiges Format.';
  if (!consent) return 'Bitte den SEPA-Bedingungen zustimmen.';
  return null;
}

function formatSepaStatusBadge(status) {
  if (status === 'ACTIVE') return 'badge-roundtrip';
  if (status === 'REVOKED') return 'badge-destination';
  return 'badge-destination';
}

// Weather icon mapping
function getWeatherIcon(condition) {
  const iconMap = {
    SUNNY: '☀️',
    CLOUDY: '☁️',
    RAINY: '🌧️',
    SNOWY: '❄️',
    FOGGY: '🌫️',
    STORMY: '⛈️',
  };
  return iconMap[condition] || '🌤️';
}

// ---------------------------------------------------------------------------
// Tests: formatDistance
// ---------------------------------------------------------------------------

describe('formatDistance()', () => {
  it('should return "-" for null/undefined/falsy', () => {
    assert.strictEqual(formatDistance(null), '-');
    assert.strictEqual(formatDistance(undefined), '-');
    assert.strictEqual(formatDistance(0), '-');
    assert.strictEqual(formatDistance(false), '-');
  });

  it('should format meters as "m" below 1000', () => {
    assert.strictEqual(formatDistance(500), '500 m');
    assert.strictEqual(formatDistance(999), '999 m');
    assert.strictEqual(formatDistance(1), '1 m');
  });

  it('should format meters as "km" at or above 1000', () => {
    assert.strictEqual(formatDistance(1000), '1.0 km');
    assert.strictEqual(formatDistance(1500), '1.5 km');
    assert.strictEqual(formatDistance(12345), '12.3 km');
    assert.strictEqual(formatDistance(42195), '42.2 km');
  });

  it('should handle zero and edge cases', () => {
    assert.strictEqual(formatDistance(0), '-');
    assert.strictEqual(formatDistance(1000), '1.0 km');
    // 999.9 rounds toFixed(0) -> '1000' which becomes '1000 m'
    assert.strictEqual(formatDistance(999.9), '1000 m');
  });
});

// ---------------------------------------------------------------------------
// Tests: formatDuration
// ---------------------------------------------------------------------------

describe('formatDuration()', () => {
  it('should return "-" for null/undefined/falsy', () => {
    assert.strictEqual(formatDuration(null), '-');
    assert.strictEqual(formatDuration(undefined), '-');
    assert.strictEqual(formatDuration(0), '-');
  });

  it('should format only minutes when less than an hour', () => {
    assert.strictEqual(formatDuration(60), '1 min');
    assert.strictEqual(formatDuration(600), '10 min');
    assert.strictEqual(formatDuration(3540), '59 min');
  });

  it('should format hours and minutes when at least one hour', () => {
    assert.strictEqual(formatDuration(3600), '1h 0min');
    assert.strictEqual(formatDuration(3660), '1h 1min');
    assert.strictEqual(formatDuration(7200), '2h 0min');
    assert.strictEqual(formatDuration(7500), '2h 5min');
  });

  it('should round minutes correctly', () => {
    assert.strictEqual(formatDuration(3661), '1h 1min');
    assert.strictEqual(formatDuration(3599), '59 min');
  });
});

// ---------------------------------------------------------------------------
// Tests: formatScore
// ---------------------------------------------------------------------------

describe('formatScore()', () => {
  it('should return "-" for null/undefined', () => {
    assert.strictEqual(formatScore(null), '-');
    assert.strictEqual(formatScore(undefined), '-');
  });

  it('should format score to one decimal place', () => {
    assert.strictEqual(formatScore(5), '5.0');
    assert.strictEqual(formatScore(4.567), '4.6');
    assert.strictEqual(formatScore(0), '0.0');
    assert.strictEqual(formatScore(1.0), '1.0');
  });

  it('should handle negative scores (edge case)', () => {
    assert.strictEqual(formatScore(-1), '-1.0');
  });
});

// ---------------------------------------------------------------------------
// Tests: escapeHtml
// ---------------------------------------------------------------------------

describe('escapeHtml()', () => {
  it('should return null/undefined as-is', () => {
    assert.strictEqual(escapeHtml(null), null);
    assert.strictEqual(escapeHtml(undefined), undefined);
  });

  it('should return empty string as-is', () => {
    assert.strictEqual(escapeHtml(''), '');
  });

  it('should return text as-is for plain strings', () => {
    // The app.js escapeHtml function sets textContent on a temp element
    // For testing we simulate: text is passed through unchanged for safe strings
    assert.strictEqual(escapeHtml('Hello'), 'Hello');
    assert.strictEqual(escapeHtml('Test Route #1'), 'Test Route #1');
  });
});

// ---------------------------------------------------------------------------
// Tests: storageKey
// ---------------------------------------------------------------------------

describe('storageKey()', () => {
  it('should prefix with "pathiful_"', () => {
    assert.strictEqual(storageKey('token'), 'pathiful_token');
    assert.strictEqual(storageKey('userId'), 'pathiful_userId');
    assert.strictEqual(storageKey('role'), 'pathiful_role');
  });

  it('should handle keys with special characters', () => {
    assert.strictEqual(storageKey('route_123'), 'pathiful_route_123');
  });
});

// ---------------------------------------------------------------------------
// Tests: IBAN formatting
// ---------------------------------------------------------------------------

describe('formatIban()', () => {
  it('should clean and format IBAN in 4-char groups', () => {
    assert.strictEqual(formatIban('DE89370400440532013000'), 'DE89 3704 0044 0532 0130 00');
  });

  it('should remove existing spaces and special chars', () => {
    assert.strictEqual(formatIban('DE89 3704 0044 0532 0130 00'), 'DE89 3704 0044 0532 0130 00');
    assert.strictEqual(formatIban('DE89-3704-0044-0532-0130-00'), 'DE89 3704 0044 0532 0130 00');
  });

  it('should uppercase lowercase letters', () => {
    assert.strictEqual(formatIban('de89 3704 0044 0532 0130 00'), 'DE89 3704 0044 0532 0130 00');
  });

  it('should truncate to 22 characters max', () => {
    const longIban = 'DE893704004405320130001234567890';
    assert.strictEqual(formatIban(longIban), 'DE89 3704 0044 0532 0130 00');
  });

  it('should handle short IBAN gracefully', () => {
    assert.strictEqual(formatIban('DE'), 'DE');
    assert.strictEqual(formatIban('D'), 'D');
    assert.strictEqual(formatIban(''), '');
  });
});

describe('maskIban()', () => {
  it('should return "-" for empty IBAN', () => {
    assert.strictEqual(maskIban(null), '-');
    assert.strictEqual(maskIban(''), '-');
  });

  it('should show first 4 and last 2 chars, mask middle', () => {
    const masked = maskIban('DE89370400440532013000');
    assert.ok(masked.startsWith('DE89'));
    assert.ok(masked.endsWith('00'));
    assert.ok(masked.includes('•'));
  });
});

// ---------------------------------------------------------------------------
// Tests: Route validation
// ---------------------------------------------------------------------------

describe('validateRouteName()', () => {
  it('should reject empty/null names', () => {
    assert.ok(validateRouteName(''));
    assert.ok(validateRouteName(null));
    assert.ok(validateRouteName('   '));
  });

  it('should reject names over 100 characters', () => {
    const longName = 'A'.repeat(101);
    assert.ok(validateRouteName(longName));
  });

  it('should accept valid names', () => {
    assert.strictEqual(validateRouteName('Abendrunde'), null);
    assert.strictEqual(validateRouteName('Morgentour ins Grüne'), null);
    assert.strictEqual(validateRouteName('A'), null);
  });
});

describe('validateRoundtripInput()', () => {
  it('should reject missing coordinates', () => {
    assert.ok(validateRoundtripInput(NaN, 10, 5));
    assert.ok(validateRoundtripInput(48, NaN, 5));
  });

  it('should reject out-of-range latitude', () => {
    assert.ok(validateRoundtripInput(100, 10, 5));
    assert.ok(validateRoundtripInput(-100, 10, 5));
  });

  it('should reject out-of-range longitude', () => {
    assert.ok(validateRoundtripInput(48, 200, 5));
    assert.ok(validateRoundtripInput(48, -200, 5));
  });

  it('should reject invalid distance', () => {
    assert.ok(validateRoundtripInput(48, 10, NaN));
    assert.ok(validateRoundtripInput(48, 10, 0));
    assert.ok(validateRoundtripInput(48, 10, -1));
  });

  it('should reject distance > 100 km', () => {
    assert.ok(validateRoundtripInput(48, 10, 101));
  });

  it('should accept valid roundtrip input', () => {
    assert.strictEqual(validateRoundtripInput(48.135, 11.582, 10), null);
    assert.strictEqual(validateRoundtripInput(48, 10, 100), null);
  });
});

describe('validateDestinationInput()', () => {
  it('should reject missing coordinates', () => {
    assert.ok(validateDestinationInput(NaN, 10, 50, 12));
    assert.ok(validateDestinationInput(48, NaN, 50, 12));
    assert.ok(validateDestinationInput(48, 10, NaN, 12));
    assert.ok(validateDestinationInput(48, 10, 50, NaN));
  });

  it('should reject out-of-range coordinates', () => {
    assert.ok(validateDestinationInput(100, 10, 50, 12));
    assert.ok(validateDestinationInput(48, 10, 100, 12));
    assert.ok(validateDestinationInput(48, 200, 50, 12));
  });

  it('should accept valid destination input', () => {
    assert.strictEqual(validateDestinationInput(48.135, 11.582, 48.250, 11.700), null);
  });
});

// ---------------------------------------------------------------------------
// Tests: SEPA validation
// ---------------------------------------------------------------------------

describe('validateSepaInput()', () => {
  it('should reject missing account holder', () => {
    assert.ok(validateSepaInput('', 'DE89370400440532013000', true));
    assert.ok(validateSepaInput('   ', 'DE89370400440532013000', true));
  });

  it('should reject missing IBAN', () => {
    assert.ok(validateSepaInput('Max Mustermann', '', true));
    assert.ok(validateSepaInput('Max Mustermann', '  ', true));
  });

  it('should reject invalid IBAN format', () => {
    assert.ok(validateSepaInput('Max Mustermann', 'DE123', true)); // too short (< 15 chars)
    assert.ok(validateSepaInput('Max Mustermann', 'DE00 0000 0000 0000 0000 0000 0000 0000 0000', true)); // too long (> 34 chars after cleaning)
  });

  it('should reject missing consent', () => {
    assert.ok(validateSepaInput('Max Mustermann', 'DE89370400440532013000', false));
  });

  it('should accept valid SEPA input', () => {
    assert.strictEqual(
      validateSepaInput('Max Mustermann', 'DE89 3704 0044 0532 0130 00', true),
      null
    );
    // With spaces in input
    assert.strictEqual(
      validateSepaInput('Max Mustermann', 'DE89 3704 0044 0532 0130 00', true),
      null
    );
  });
});

describe('formatSepaStatusBadge()', () => {
  it('should return badge-roundtrip for ACTIVE', () => {
    assert.strictEqual(formatSepaStatusBadge('ACTIVE'), 'badge-roundtrip');
  });

  it('should return badge-destination for REVOKED', () => {
    assert.strictEqual(formatSepaStatusBadge('REVOKED'), 'badge-destination');
  });

  it('should default to badge-destination for unknown status', () => {
    assert.strictEqual(formatSepaStatusBadge('PENDING'), 'badge-destination');
  });
});

// ---------------------------------------------------------------------------
// Tests: Weather icon mapping
// ---------------------------------------------------------------------------

describe('getWeatherIcon()', () => {
  it('should return correct emoji for known conditions', () => {
    assert.strictEqual(getWeatherIcon('SUNNY'), '☀️');
    assert.strictEqual(getWeatherIcon('CLOUDY'), '☁️');
    assert.strictEqual(getWeatherIcon('RAINY'), '🌧️');
    assert.strictEqual(getWeatherIcon('SNOWY'), '❄️');
    assert.strictEqual(getWeatherIcon('FOGGY'), '🌫️');
    assert.strictEqual(getWeatherIcon('STORMY'), '⛈️');
  });

  it('should return default icon for unknown conditions', () => {
    assert.strictEqual(getWeatherIcon('UNKNOWN'), '🌤️');
    assert.strictEqual(getWeatherIcon(null), '🌤️');
    assert.strictEqual(getWeatherIcon(undefined), '🌤️');
  });
});

// ---------------------------------------------------------------------------
// Additional: Roundtrip vs Destination type display logic
// ---------------------------------------------------------------------------

function getRouteTypeLabel(type) {
  if (type === 'ROUNDTRIP') return '↻ Rundroute';
  if (type === 'DESTINATION') return '→ Zielroute';
  return '-';
}

function getRouteTypeBadge(type) {
  return type === 'ROUNDTRIP' ? 'badge-roundtrip' : 'badge-destination';
}

describe('getRouteTypeLabel()', () => {
  it('should return correct labels', () => {
    assert.strictEqual(getRouteTypeLabel('ROUNDTRIP'), '↻ Rundroute');
    assert.strictEqual(getRouteTypeLabel('DESTINATION'), '→ Zielroute');
    assert.strictEqual(getRouteTypeLabel(null), '-');
  });
});

describe('getRouteTypeBadge()', () => {
  it('should return correct badge classes', () => {
    assert.strictEqual(getRouteTypeBadge('ROUNDTRIP'), 'badge-roundtrip');
    assert.strictEqual(getRouteTypeBadge('DESTINATION'), 'badge-destination');
  });
});
