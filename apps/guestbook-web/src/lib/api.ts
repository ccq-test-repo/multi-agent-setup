const API_BASE = import.meta.env.VITE_API_BASE ?? "http://localhost:8088";

export interface GuestbookEntry {
  id: string;
  author: string;
  message: string;
  createdAt: string;
}

export interface CreateMessagePayload {
  author: string;
  message: string;
}

export interface ApiErrorResponse {
  error?: string;
  message?: string;
  status?: number;
}

export async function fetchMessages(signal?: AbortSignal): Promise<GuestbookEntry[]> {
  const url = `${API_BASE}/api/messages`;
  const response = await fetch(url, { signal });

  if (!response.ok) {
    let errorMessage: string;
    try {
      const errBody: ApiErrorResponse = await response.json();
      errorMessage = errBody.error ?? errBody.message ?? `Server-Fehler (HTTP ${response.status})`;
    } catch {
      errorMessage = `Server-Fehler (HTTP ${response.status})`;
    }
    throw new Error(errorMessage);
  }

  return response.json();
}

export async function createMessage(payload: CreateMessagePayload, signal?: AbortSignal): Promise<GuestbookEntry> {
  const url = `${API_BASE}/api/messages`;
  const response = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
    signal,
  });

  if (!response.ok) {
    let errorMessage: string;
    try {
      const errBody: ApiErrorResponse = await response.json();
      errorMessage = errBody.error ?? errBody.message ?? `Server-Fehler (HTTP ${response.status})`;
    } catch {
      errorMessage = `Server-Fehler (HTTP ${response.status})`;
    }
    throw new Error(errorMessage);
  }

  return response.json();
}
