import * as React from "react";
import { Loader2, RefreshCw } from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { GuestbookForm } from "@/components/guestbook-form";
import { GuestbookList } from "@/components/guestbook-list";
import { fetchMessages } from "@/lib/api";
import type { GuestbookEntry } from "@/types/message";

export default function App() {
  const [entries, setEntries] = React.useState<GuestbookEntry[]>([]);
  const [isLoading, setIsLoading] = React.useState(true);
  const [loadError, setLoadError] = React.useState<string | null>(null);

  const load = React.useCallback(async (signal?: AbortSignal) => {
    setIsLoading(true);
    setLoadError(null);
    try {
      const data = await fetchMessages(signal);
      setEntries(data);
    } catch (err) {
      if (signal?.aborted) return;
      setLoadError(
        err instanceof Error
          ? err.message
          : "Einträge konnten nicht geladen werden.",
      );
    } finally {
      if (!signal?.aborted) setIsLoading(false);
    }
  }, []);

  React.useEffect(() => {
    const controller = new AbortController();
    void load(controller.signal);
    return () => controller.abort();
  }, [load]);

  function handleCreated(newEntry: GuestbookEntry) {
    setEntries((prev) => [newEntry, ...prev]);
  }

  return (
    <div className="min-h-screen flex flex-col">
      <main className="mx-auto w-full max-w-2xl flex-1 px-4 py-8 sm:py-12">
        <header className="mb-8 text-center">
          <h1 className="text-2xl font-semibold tracking-tight sm:text-3xl">
            Gästebuch
          </h1>
          <p className="mt-2 text-sm text-muted-foreground sm:text-base">
            Hinterlasse eine Nachricht und lies, was andere geschrieben haben.
          </p>
        </header>

        <Card className="mb-8">
          <CardHeader>
            <CardTitle>Neuer Eintrag</CardTitle>
            <CardDescription>
              Name und Nachricht dürfen nicht leer sein.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <GuestbookForm onCreated={handleCreated} />
          </CardContent>
        </Card>

        <section aria-labelledby="entries-heading">
          <h2 id="entries-heading" className="mb-4 text-lg font-semibold">
            Einträge
          </h2>

          {isLoading ? (
            <Card>
              <CardContent className="flex items-center justify-center gap-2 py-12 text-sm text-muted-foreground">
                <Loader2 className="h-5 w-5 animate-spin" aria-hidden="true" />
                Einträge werden geladen …
              </CardContent>
            </Card>
          ) : loadError ? (
            <Alert variant="destructive">
              <AlertDescription>{loadError}</AlertDescription>
              <Button
                variant="outline"
                size="sm"
                className="mt-3"
                onClick={() => void load()}
              >
                <RefreshCw className="mr-2 h-4 w-4" aria-hidden="true" />
                Erneut versuchen
              </Button>
            </Alert>
          ) : (
            <GuestbookList entries={entries} />
          )}
        </section>
      </main>

      <footer className="mx-auto w-full max-w-2xl px-4 pb-8 text-center text-xs text-muted-foreground">
        <p>Gästebuch — alle Einträge werden serverseitig gespeichert.</p>
      </footer>
    </div>
  );
}
