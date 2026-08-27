import { BookOpen, MessageSquare } from "lucide-react";
import { Card, CardContent } from "@/components/ui/card";
import type { GuestbookEntry } from "@/types/message";

interface GuestbookListProps {
  entries: GuestbookEntry[];
}

export function GuestbookList({ entries }: GuestbookListProps) {
  if (entries.length === 0) {
    return (
      <Card className="border-dashed">
        <CardContent className="flex flex-col items-center justify-center gap-2 py-12 text-center">
          <BookOpen
            className="h-10 w-10 text-muted-foreground"
            aria-hidden="true"
          />
          <p className="text-base font-medium">Noch keine Einträge</p>
          <p className="text-sm text-muted-foreground">
            Sei die erste Person und schreibe eine Nachricht ins Gästebuch.
          </p>
        </CardContent>
      </Card>
    );
  }

  return (
    <ul className="space-y-4" aria-label="Gästebuch-Einträge">
      {entries.map((entry) => (
        <li key={entry.id}>
          <Card>
            <CardContent className="p-4 sm:p-5">
              <p className="flex items-center gap-2 text-sm font-semibold">
                <MessageSquare
                  className="h-4 w-4 text-muted-foreground"
                  aria-hidden="true"
                />
                <span>{entry.author}</span>
              </p>
              <p className="mt-2 whitespace-pre-wrap break-words text-sm leading-relaxed">
                {entry.text}
              </p>
            </CardContent>
          </Card>
        </li>
      ))}
    </ul>
  );
}
