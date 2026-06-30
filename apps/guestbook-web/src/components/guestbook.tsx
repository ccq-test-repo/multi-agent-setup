import { useCallback, useEffect, useRef, useState } from "react";
import { MessageCircle, RefreshCw, Send, User } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { type GuestbookEntry, fetchMessages, createMessage } from "@/lib/api";

type ViewState = "loading" | "error" | "empty" | "data";

export function Guestbook() {
  const [entries, setEntries] = useState<GuestbookEntry[]>([]);
  const [viewState, setViewState] = useState<ViewState>("loading");
  const [errorMessage, setErrorMessage] = useState<string>("");
  const [author, setAuthor] = useState("");
  const [message, setMessage] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string>("");
  const abortRef = useRef<AbortController | null>(null);

  const loadMessages = useCallback(async () => {
    abortRef.current?.abort();
    const controller = new AbortController();
    abortRef.current = controller;

    setViewState("loading");

    try {
      const data = await fetchMessages(controller.signal);
      setEntries(data);
      setViewState(data.length === 0 ? "empty" : "data");
      setErrorMessage("");
    } catch (err) {
      if (err instanceof DOMException && err.name === "AbortError") return;
      setErrorMessage(err instanceof Error ? err.message : "Unbekannter Fehler");
      setViewState("error");
    }
  }, []);

  useEffect(() => {
    loadMessages();
    return () => abortRef.current?.abort();
  }, [loadMessages]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!author.trim() || !message.trim() || submitting) return;

    setSubmitting(true);
    setSubmitError("");

    try {
      const newEntry = await createMessage({ author: author.trim(), message: message.trim() });
      setEntries((prev) => [newEntry, ...prev]);
      setAuthor("");
      setMessage("");
      if (viewState === "empty") setViewState("data");
    } catch (err) {
      setSubmitError(err instanceof Error ? err.message : "Fehler beim Senden");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="mx-auto max-w-2xl space-y-8">
      {/* Header */}
      <header className="text-center">
        <h1 className="text-3xl font-semibold tracking-tight">Gästebuch</h1>
        <p className="mt-1 text-sm text-muted-foreground">
          Hinterlasse einen Eintrag
        </p>
      </header>

      {/* Formular */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-lg">
            <MessageCircle className="h-5 w-5" />
            Neuer Eintrag
          </CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4" aria-label="Neuen Gästebucheintrag erstellen">
            <div className="space-y-2">
              <Label htmlFor="author">Name</Label>
              <Input
                id="author"
                placeholder="Dein Name"
                value={author}
                onChange={(e) => setAuthor(e.target.value)}
                disabled={submitting}
                required
                aria-required="true"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="message">Nachricht</Label>
              <Textarea
                id="message"
                placeholder="Schreibe etwas …"
                value={message}
                onChange={(e) => setMessage(e.target.value)}
                disabled={submitting}
                required
                aria-required="true"
              />
            </div>
            {submitError && (
              <p className="text-sm text-destructive" role="alert">
                {submitError}
              </p>
            )}
            <Button type="submit" disabled={submitting || !author.trim() || !message.trim()}>
              {submitting ? (
                <>
                  <RefreshCw className="mr-2 h-4 w-4 animate-spin" />
                  Wird gesendet …
                </>
              ) : (
                <>
                  <Send className="mr-2 h-4 w-4" />
                  Absenden
                </>
              )}
            </Button>
          </form>
        </CardContent>
      </Card>

      {/* Einträge */}
      <section aria-label="Gästebucheinträge">
        <div className="mb-4 flex items-center justify-between">
          <h2 className="text-xl font-semibold">Einträge</h2>
          <Button
            variant="ghost"
            size="sm"
            onClick={loadMessages}
            disabled={viewState === "loading"}
            aria-label="Einträge neu laden"
          >
            <RefreshCw className={`mr-1 h-4 w-4 ${viewState === "loading" ? "animate-spin" : ""}`} />
            Neu laden
          </Button>
        </div>

        {viewState === "loading" && (
          <div className="flex items-center justify-center py-12" role="status">
            <RefreshCw className="h-6 w-6 animate-spin text-muted-foreground" />
            <span className="ml-2 text-sm text-muted-foreground">Lade Einträge …</span>
          </div>
        )}

        {viewState === "error" && (
          <Card className="border-destructive">
            <CardContent className="py-6 text-center">
              <p className="text-destructive" role="alert">
                Fehler beim Laden: {errorMessage}
              </p>
              <Button variant="outline" size="sm" className="mt-4" onClick={loadMessages}>
                <RefreshCw className="mr-2 h-4 w-4" />
                Erneut versuchen
              </Button>
            </CardContent>
          </Card>
        )}

        {viewState === "empty" && (
          <div className="py-12 text-center">
            <MessageCircle className="mx-auto h-12 w-12 text-muted-foreground" />
            <p className="mt-4 text-lg font-medium">Noch keine Einträge</p>
            <p className="mt-1 text-sm text-muted-foreground">
              Sei der oder die Erste! Schreibe oben eine Nachricht.
            </p>
          </div>
        )}

        {viewState === "data" && (
          <ul className="space-y-4" role="list">
            {entries.map((entry) => (
              <li key={entry.id}>
                <Card>
                  <CardHeader>
                    <div className="flex items-center gap-2">
                      <User className="h-4 w-4 text-muted-foreground" />
                      <CardTitle className="text-base font-medium">{entry.author}</CardTitle>
                    </div>
                  </CardHeader>
                  <CardContent>
                    <p className="text-sm whitespace-pre-wrap">{entry.message}</p>
                    <p className="mt-2 text-xs text-muted-foreground">
                      {new Date(entry.createdAt).toLocaleDateString("de-DE", {
                        year: "numeric",
                        month: "long",
                        day: "numeric",
                        hour: "2-digit",
                        minute: "2-digit",
                      })}
                    </p>
                  </CardContent>
                </Card>
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  );
}
