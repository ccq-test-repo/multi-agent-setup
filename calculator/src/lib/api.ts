/**
 * API-Client für den Taschenrechner.
 * Kommuniziert mit POST /api/calculator/calculate.
 */

const API_BASE = import.meta.env.VITE_API_BASE ?? "";

export interface CalculateRequest {
  expression: string;
}

export interface CalculateResponse {
  result: number;
}

export interface ApiError {
  error?: string;
  message?: string;
  status?: number;
}

export async function calculateExpression(
  expression: string,
  signal?: AbortSignal
): Promise<number> {
  const url = `${API_BASE}/api/calculator/calculate`;

  const response = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ expression } satisfies CalculateRequest),
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
