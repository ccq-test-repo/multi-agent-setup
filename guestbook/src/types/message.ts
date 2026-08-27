/**
 * Typen für den Gästebuch-API-Vertrag (Backend: apps/guestbook-api, Port 8088).
 *
 * GET  /api/messages → 200: GuestbookEntry[]
 * POST /api/messages ({ author, text }) → 201: GuestbookEntry | 400: { error }
 *
 * Felder: author (Name), text (Nachricht) — beide müssen nicht-leer sein.
 */

export interface GuestbookEntry {
  id: number;
  author: string;
  text: string;
}

export interface CreateMessageRequest {
  author: string;
  text: string;
}

export interface ApiErrorBody {
  error?: string;
}
