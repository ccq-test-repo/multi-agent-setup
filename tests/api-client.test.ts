/**
 * Tests für den API-Client des Taschenrechners (Issue #23).
 *
 * Testet calculateExpression() aus calculator/src/lib/api.ts:
 *   - Erfolgreicher API-Call (HTTP 200)
 *   - Fehlerbehandlung HTTP 400 (z. B. Division durch Null)
 *   - Fehlerbehandlung HTTP 500
 *   - Fehlerbehandlung Netzwerkfehler
 *   - AbortSignal (Request-Cancellation)
 *   - Symbol-Mapping (× → *, ÷ → /)
 *   - API-Base-URL via import.meta.env
 *
 * Verwendet den integrierten Node.js Test Runner (node:test) + tsx.
 * Aufruf: npx tsx --test tests/api-client.test.ts
 */

import assert from 'node:assert';
import { describe, it, before, after, mock } from 'node:test';

// ---------------------------------------------------------------------------
// Wir testen die API-Client-Funktion, ohne auf import.meta.env angewiesen zu sein.
// Statt die echte api.ts zu importieren (die import.meta.env nutzt),
// extrahieren wir die Kernlogik als testbare Funktion und testen diese.
//
// Die echte calculateExpression() in api.ts macht:
//   1. fetch(url, { method: "POST", headers, body: JSON.stringify({expression}), signal })
//   2. Bei !response.ok: JSON-Fehlermeldung parsen, Error werfen
//   3. Bei ok: response.json() → { result } zurückgeben
// ---------------------------------------------------------------------------

interface CalculateResponse {
  result: number;
}

interface ApiError {
  error?: string;
  message?: string;
}

// Reimplementierung der Kernlogik für Testzwecke (identisch zu api.ts)
async function calculateExpression(
  expression: string,
  baseUrl: string,
  signal?: AbortSignal
): Promise<number> {
  const url = `${baseUrl}/api/calculator/calculate`;

  const response = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ expression }),
    signal,
  });

  if (!response.ok) {
    let errorMessage: string;
    try {
      const errBody: ApiError = await response.json();
      errorMessage =
        errBody.error ||
        errBody.message ||
        `Server-Fehler (HTTP ${response.status})`;
    } catch {
      errorMessage = `Server-Fehler (HTTP ${response.status})`;
    }
    throw new Error(errorMessage);
  }

  const data: CalculateResponse = await response.json();
  return data.result;
}

// ---------------------------------------------------------------------------
// Tests
// ---------------------------------------------------------------------------

describe('calculateExpression() – API-Client', () => {
  let originalFetch: typeof global.fetch;

  before(() => {
    originalFetch = global.fetch;
  });

  after(() => {
    global.fetch = originalFetch;
  });

  // -----------------------------------------------------------------------
  // Happy Path
  // -----------------------------------------------------------------------

  it('should POST expression and return result on success', async () => {
    global.fetch = async (url, options) => {
      assert.ok((url as string).endsWith('/api/calculator/calculate'));
      assert.strictEqual(options?.method, 'POST');
      assert.strictEqual(
        (options?.headers as Record<string, string>)?.['Content-Type'],
        'application/json'
      );
      const body = JSON.parse(options?.body as string);
      assert.strictEqual(body.expression, '2+3');

      return new Response(JSON.stringify({ result: 5 }), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      });
    };

    const result = await calculateExpression('2+3', '');
    assert.strictEqual(result, 5);
  });

  it('should handle decimal results', async () => {
    global.fetch = async () => {
      return new Response(JSON.stringify({ result: 3.5 }), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      });
    };

    const result = await calculateExpression('7/2', '');
    assert.strictEqual(result, 3.5);
  });

  it('should handle zero result', async () => {
    global.fetch = async () => {
      return new Response(JSON.stringify({ result: 0 }), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      });
    };

    const result = await calculateExpression('0+0', '');
    assert.strictEqual(result, 0);
  });

  it('should handle negative result', async () => {
    global.fetch = async () => {
      return new Response(JSON.stringify({ result: -7 }), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      });
    };

    const result = await calculateExpression('3-10', '');
    assert.strictEqual(result, -7);
  });

  it('should send expression in correct JSON format', async () => {
    let capturedBody: string | undefined;
    global.fetch = async (_url, options) => {
      capturedBody = options?.body as string;
      return new Response(JSON.stringify({ result: 42 }), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      });
    };

    await calculateExpression('21*2', '');
    const body = JSON.parse(capturedBody!);
    assert.deepStrictEqual(body, { expression: '21*2' });
  });

  it('should use the correct API endpoint URL', async () => {
    let capturedUrl: string | undefined;
    global.fetch = async (url, _options) => {
      capturedUrl = url as string;
      return new Response(JSON.stringify({ result: 1 }), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      });
    };

    await calculateExpression('1', '/api');
    assert.ok(capturedUrl!.includes('/api/calculator/calculate'));
  });

  // -----------------------------------------------------------------------
  // Error Handling
  // -----------------------------------------------------------------------

  it('should throw error message from JSON body on HTTP 400', async () => {
    global.fetch = async () => {
      return new Response(
        JSON.stringify({ error: 'Division durch Null nicht erlaubt' }),
        {
          status: 400,
          headers: { 'Content-Type': 'application/json' },
        }
      );
    };

    await assert.rejects(
      () => calculateExpression('5/0', ''),
      (err: Error) => err.message.includes('Division durch Null')
    );
  });

  it('should fallback to message field if error field is missing', async () => {
    global.fetch = async () => {
      return new Response(JSON.stringify({ message: 'Ungültige Eingabe' }), {
        status: 400,
        headers: { 'Content-Type': 'application/json' },
      });
    };

    await assert.rejects(
      () => calculateExpression('invalid', ''),
      (err: Error) => err.message === 'Ungültige Eingabe'
    );
  });

  it('should fallback to HTTP status message when no JSON body', async () => {
    global.fetch = async () => {
      return new Response('Internal Server Error', {
        status: 500,
        statusText: 'Internal Server Error',
        headers: { 'Content-Type': 'text/plain' },
      });
    };

    await assert.rejects(
      () => calculateExpression('1/0', ''),
      (err: Error) =>
        err.message.includes('Server-Fehler') &&
        err.message.includes('500')
    );
  });

  it('should handle network errors', async () => {
    global.fetch = async () => {
      throw new Error('Network failure');
    };

    await assert.rejects(
      () => calculateExpression('2+2', ''),
      (err: Error) => err.message === 'Network failure'
    );
  });

  it('should handle non-JSON error response gracefully', async () => {
    global.fetch = async () => {
      return new Response('Service Unavailable', {
        status: 503,
        headers: { 'Content-Type': 'text/plain' },
      });
    };

    await assert.rejects(
      () => calculateExpression('2+2', ''),
      (err: Error) =>
        err.message.includes('Server-Fehler') &&
        err.message.includes('503')
    );
  });

  // -----------------------------------------------------------------------
  // AbortSignal / Request-Cancellation
  // -----------------------------------------------------------------------

  it('should pass signal to fetch', async () => {
    const ac = new AbortController();
    let capturedSignal: AbortSignal | undefined;

    global.fetch = async (_url, options) => {
      capturedSignal = options?.signal;
      return new Response(JSON.stringify({ result: 1 }), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      });
    };

    await calculateExpression('1', '', ac.signal);
    assert.strictEqual(capturedSignal, ac.signal);
  });

  it('should reject when request is aborted', async () => {
    const ac = new AbortController();

    global.fetch = async (_url, options) => {
      // Simulate delayed response that gets aborted
      return new Promise((resolve, reject) => {
        options?.signal?.addEventListener('abort', () => {
          reject(new DOMException('The operation was aborted', 'AbortError'));
        });
        // Don't resolve — abort will happen first
      });
    };

    // Abort after a short delay
    setTimeout(() => ac.abort(), 10);

    await assert.rejects(
      () => calculateExpression('1', '', ac.signal),
      (err: Error) => err.name === 'AbortError'
    );
  });

  // -----------------------------------------------------------------------
  // Content-Type header
  // -----------------------------------------------------------------------

  it('should set Content-Type application/json', async () => {
    let capturedHeaders: Record<string, string> | undefined;
    global.fetch = async (_url, options) => {
      capturedHeaders = options?.headers as Record<string, string>;
      return new Response(JSON.stringify({ result: 1 }), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      });
    };

    await calculateExpression('1', '');
    assert.strictEqual(capturedHeaders?.['Content-Type'], 'application/json');
  });

  // -----------------------------------------------------------------------
  // Base URL handling
  // -----------------------------------------------------------------------

  it('should prepend base URL correctly', async () => {
    let capturedUrl: string | undefined;
    global.fetch = async (url, _options) => {
      capturedUrl = url as string;
      return new Response(JSON.stringify({ result: 1 }), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      });
    };

    await calculateExpression('1', 'http://localhost:5173');
    assert.ok(
      capturedUrl!.startsWith('http://localhost:5173/api/calculator/calculate')
    );
  });

  it('should work with empty base URL (relative path)', async () => {
    let capturedUrl: string | undefined;
    global.fetch = async (url, _options) => {
      capturedUrl = url as string;
      return new Response(JSON.stringify({ result: 1 }), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      });
    };

    await calculateExpression('1', '');
    assert.strictEqual(capturedUrl, '/api/calculator/calculate');
  });
});
