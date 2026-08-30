/**
 * API-Client für den Taschenrechner.
 *
 * Konsumiert den Backend-Vertrag aus Sub-Issue #82:
 *   POST /api/calculate  { left, operator, right } → { result }
 *   GET  /api/health     → { status: "UP" }
 *
 * Operatoren: ADD | SUBTRACT | MULTIPLY | DIVIDE | PERCENT
 *
 * Backend-Fehler (HTTP 400) liefern { error, message } — die menschenlesbare
 * `message` wird dem Nutzer angezeigt, nicht der Statuscode.
 */

const API_BASE = import.meta.env.VITE_API_BASE ?? "";

export type Operator = "ADD" | "SUBTRACT" | "MULTIPLY" | "DIVIDE" | "PERCENT";

export interface CalculationRequest {
  left: number;
  operator: Operator;
  right: number;
}

export interface CalculationResponse {
  result: number;
}

export interface ApiErrorBody {
  error?: string;
  message?: string;
}

/**
 * Führt eine Rechnung über `POST /api/calculate` aus.
 *
 * @throws Error mit einer lesbaren Nachricht (Backend-Message oder Fallback).
 */
export async function calculate(
  req: CalculationRequest,
  signal?: AbortSignal,
): Promise<number> {
  const url = `${API_BASE}/api/calculate`;
  const response = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(req),
    signal,
  });

  if (!response.ok) {
    let message = `Server-Fehler (HTTP ${response.status})`;
    try {
      const body: ApiErrorBody = await response.json();
      if (body.message) message = body.message;
      else if (body.error) message = `Server-Fehler (${body.error})`;
    } catch {
      // Kein JSON-Body → Fallback-Nachricht behalten.
    }
    throw new Error(message);
  }

  const data: CalculationResponse = await response.json();
  return data.result;
}

/**
 * Prüft, ob das Backend erreichbar ist (GET /api/health).
 */
export async function health(): Promise<boolean> {
  try {
    const response = await fetch(`${API_BASE}/api/health`);
    if (!response.ok) return false;
    const body = await response.json();
    return body?.status === "UP";
  } catch {
    return false;
  }
}
