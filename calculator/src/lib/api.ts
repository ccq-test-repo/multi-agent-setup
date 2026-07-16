/**
 * API-Client für den Taschenrechner.
 * Ruft die /api/calc/*-Endpunkte des Backends auf.
 *
 * POST /api/calc/add       → { "a": number, "b": number } → { "result": number }
 * POST /api/calc/subtract  → { "a": number, "b": number } → { "result": number }
 * POST /api/calc/multiply  → { "a": number, "b": number } → { "result": number }
 * POST /api/calc/divide    → { "a": number, "b": number } → { "result": number }
 *
 * Fehler (HTTP 400) → { "error": "invalid_input" }
 */

const API_BASE = import.meta.env.VITE_API_BASE ?? "";

export type CalcOperation = "add" | "subtract" | "multiply" | "divide";

export interface CalcRequest {
  a: number;
  b: number;
}

export interface CalcResponse {
  result: number;
}

export interface ApiErrorBody {
  error?: string;
}

function endpointFor(op: CalcOperation): string {
  return `${API_BASE}/api/calc/${op}`;
}

/**
 * Führt eine Rechenoperation über die Backend-API aus.
 *
 * @param op     Die Operation (add, subtract, multiply, divide)
 * @param a      Erster Operand
 * @param b      Zweiter Operand
 * @param signal Optionales AbortSignal zum Verwerfen der Anfrage
 * @returns      Das Ergebnis als number
 * @throws       Error mit lesbarer Nachricht bei Fehlern
 */
export async function calculateOperation(
  op: CalcOperation,
  a: number,
  b: number,
  signal?: AbortSignal,
): Promise<number> {
  const url = endpointFor(op);

  const response = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ a, b } satisfies CalcRequest),
    signal,
  });

  if (!response.ok) {
    let errorMessage: string;
    try {
      const errBody: ApiErrorBody = await response.json();
      errorMessage =
        errBody.error === "invalid_input"
          ? "Ungültige Eingabe"
          : errBody.error ?? `Server-Fehler (HTTP ${response.status})`;
    } catch {
      errorMessage = `Server-Fehler (HTTP ${response.status})`;
    }
    throw new Error(errorMessage);
  }

  const data: CalcResponse = await response.json();
  return data.result;
}
