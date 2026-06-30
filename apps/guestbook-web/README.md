# Guestbook Web

React-Frontend für das Gästebuch. Zeigt Einträge aus der Guestbook-API an
und erlaubt das Erstellen neuer Einträge.

## Voraussetzungen

- Node.js 20+
- Backend `apps/guestbook-api` läuft auf http://localhost:8088

## Entwicklung

```bash
# Abhängigkeiten installieren
npm install

# Dev-Server starten (Port 5173)
npm run dev
```

## Bauen

```bash
npm run build
```

## Umgebungsvariablen

| Variable       | Default                     | Beschreibung              |
|----------------|-----------------------------|---------------------------|
| `VITE_API_BASE` | `http://localhost:8088`     | Basis-URL der Backend-API |

## Projektstruktur

```
src/
├── App.tsx                 # Root-Komponente
├── main.tsx                # Einstiegspunkt
├── index.css               # Tailwind-Import + Design-Tokens
├── components/
│   ├── guestbook.tsx       # Haupt-Gästebuch-Komponente
│   └── ui/                 # shadcn/ui-Bausteine
│       ├── button.tsx
│       ├── card.tsx
│       ├── input.tsx
│       ├── label.tsx
│       └── textarea.tsx
└── lib/
    ├── api.ts              # API-Client mit fetchMessages / createMessage
    └── utils.ts            # cn()-Helper
```
