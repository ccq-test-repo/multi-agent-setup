/**
 * API-Client für das Gästebuch.
 * Ruft die /api/messages-Endpunkte des Backends auf (Port 8088, CORS für localhost:5173).
 *
 * GET  /api/messages → GuestbookEntry[]
 * POST /api/messages → 201 GuestbookEntry | 400 { error }
 */

import type {
  ApiErrorBody,
  CreateMessageRequest,
  GuestbookEntry,
} from "@/types/message";

const API_BASE = import.meta.env.VITE_API_BASE ?? "http://localhost:8088";

/**
 * Lädt alle vorhandenen Gästebuch-Einträge.
 *
 * @param signal Optionales AbortSignal zum Verwerfen der Anfrage
 * @returns      Liste der Einträge
 * @throws       Error mit lesbarer Nachricht bei Fehlern
 */
export async function fetchMessages(signal?: AbortSignal): Promise<GuestbookEntry[]> {
  const response = await fetch(`${API_BASE}/api/messages`, { signal });

  if (!response.ok) {
    throw new Error(`Server-Fehler (HTTP ${response.status})`);
  }

  return (await response.json()) as GuestbookEntry[];
}

/**
 * Erstellt einen neuen Gästebuch-Eintrag.
 *
 * @param request Autor (Name) und Nachricht
 * @param signal  Optionales AbortSignal zum Verwerfen der Anfrage
 * @returns       Der angelegte Eintrag inkl. id
 * @throws        Error mit lesbarer Nachricht bei Validierungs- oder Server-Fehlern
 */
export async function createMessage(
  request: CreateMessageRequest,
  signal?: AbortSignal,
): Promise<GuestbookEntry> {
  const response = await fetch(`${API_BASE}/api/messages`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(request),
    signal,
  });

  if (!response.ok) {
    let errorMessage: string;
    try {
      const errBody: ApiErrorBody = await response.json();
      errorMessage =
        errBody.error ?? `Server-Fehler (HTTP ${response.status})`;
    } catch {
      errorMessage = `Server-Fehler (HTTP ${response.status})`;
    }
    throw new Error(errorMessage);
  }

  return (await response.json()) as GuestbookEntry;
}
