import { Guestbook } from "@/components/guestbook";

export default function App() {
  return (
    <div className="min-h-screen py-8 px-4">
      <main>
        <Guestbook />
      </main>
      <footer className="mt-12 text-center text-xs text-muted-foreground">
        Powered by React + Spring Boot
      </footer>
    </div>
  );
}
