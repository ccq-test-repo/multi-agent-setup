import * as React from "react";
import { Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { createMessage } from "@/lib/api";
import type { GuestbookEntry } from "@/types/message";

interface GuestbookFormProps {
  /** Wird nach erfolgreichem Anlegen mit dem neuen Eintrag aufgerufen. */
  onCreated: (entry: GuestbookEntry) => void;
}

interface FormErrors {
  author?: string;
  text?: string;
}

export function GuestbookForm({ onCreated }: GuestbookFormProps) {
  const [author, setAuthor] = React.useState("");
  const [text, setText] = React.useState("");
  const [errors, setErrors] = React.useState<FormErrors>({});
  const [submitError, setSubmitError] = React.useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = React.useState(false);

  function validate(): boolean {
    const next: FormErrors = {};
    if (author.trim() === "") {
      next.author = "Bitte gib deinen Namen ein.";
    }
    if (text.trim() === "") {
      next.text = "Bitte schreibe eine Nachricht.";
    }
    setErrors(next);
    return Object.keys(next).length === 0;
  }

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSubmitError(null);
    if (!validate()) {
      return;
    }

    setIsSubmitting(true);
    try {
      const entry = await createMessage({ author: author.trim(), text: text.trim() });
      setAuthor("");
      setText("");
      onCreated(entry);
    } catch (err) {
      setSubmitError(
        err instanceof Error ? err.message : "Eintrag konnte nicht gespeichert werden.",
      );
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} noValidate className="space-y-4">
      <div className="space-y-2">
        <Label htmlFor="guestbook-author">Name</Label>
        <Input
          id="guestbook-author"
          name="author"
          value={author}
          onChange={(e) => {
            setAuthor(e.target.value);
            if (errors.author) {
              setErrors((prev) => ({ ...prev, author: undefined }));
            }
          }}
          placeholder="Dein Name"
          aria-invalid={Boolean(errors.author)}
          aria-describedby={errors.author ? "guestbook-author-error" : undefined}
          disabled={isSubmitting}
          autoComplete="name"
        />
        {errors.author ? (
          <p id="guestbook-author-error" className="text-sm text-destructive">
            {errors.author}
          </p>
        ) : null}
      </div>

      <div className="space-y-2">
        <Label htmlFor="guestbook-text">Nachricht</Label>
        <Textarea
          id="guestbook-text"
          name="text"
          value={text}
          onChange={(e) => {
            setText(e.target.value);
            if (errors.text) {
              setErrors((prev) => ({ ...prev, text: undefined }));
            }
          }}
          placeholder="Schreibe etwas ins Gästebuch …"
          aria-invalid={Boolean(errors.text)}
          aria-describedby={errors.text ? "guestbook-text-error" : undefined}
          disabled={isSubmitting}
        />
        {errors.text ? (
          <p id="guestbook-text-error" className="text-sm text-destructive">
            {errors.text}
          </p>
        ) : null}
      </div>

      {submitError ? (
        <Alert variant="destructive">
          <AlertDescription>
            {submitError}
          </AlertDescription>
        </Alert>
      ) : null}

      <Button type="submit" disabled={isSubmitting} className="w-full sm:w-auto">
        {isSubmitting ? (
          <>
            <Loader2 className="mr-2 h-4 w-4 animate-spin" aria-hidden="true" />
            Wird gespeichert …
          </>
        ) : (
          "Eintrag absenden"
        )}
      </Button>
    </form>
  );
}
