# Feedback-Board REST API

Eigenständiger Feedback-Service für das Feedback-Board (Issue #55) in `apps/feedback-service/`.

## Build & Test

```bash
cd apps/feedback-service
mvn clean test
```

## Starten

```bash
cd apps/feedback-service
mvn spring-boot:run
```

Läuft auf **http://localhost:8088**.

## API-Endpunkte

### POST /feedback — Eintrag anlegen

```bash
curl -s -X POST localhost:8088/feedback \
  -H 'Content-Type: application/json' \
  -d '{"title":"Fehler auf Mobilgerät","description":"Layout bricht um","category":"Bug"}'
```

→ **201 Created** mit

```json
{
  "id": 1,
  "title": "Fehler auf Mobilgerät",
  "description": "Layout bricht um",
  "category": "Bug",
  "createdAt": "2026-08-25T21:00:00Z"
}
```

Felder:

- `title` – Pflichtfeld, max. 100 Zeichen
- `description` – optional, max. 500 Zeichen
- `category` – Pflichtfeld, einer von `Bug`, `Idee`, `Sonstiges`

### GET /feedback — Alle Einträge abrufen

```bash
curl -s localhost:8088/feedback
```

→ **200 OK** mit Array aller Einträge (aufsteigend nach `id`).

### Validierung (400 Bad Request)

```bash
curl -s -o /dev/null -w '%{http_code}' -X POST localhost:8088/feedback \
  -H 'Content-Type: application/json' \
  -d '{"title":"","category":"Bug"}'
# → 400

curl -s -X POST localhost:8088/feedback \
  -H 'Content-Type: application/json' \
  -d '{"title":"","category":"Bug"}'
# → {"error":"title must not be blank"}

curl -s -o /dev/null -w '%{http_code}' -X POST localhost:8088/feedback \
  -H 'Content-Type: application/json' \
  -d '{"title":"t","category":"Unbekannt"}'
# → 400 (ungültige Kategorie)
```

## Persistenz

Minimale In-Memory-Persistenz (`ConcurrentHashMap` + `AtomicLong`) analog zu den
bestehenden Standalone-Services (guestbook-api, todo-service). Bewusst keine
Datenbank: laut Issue #55 ist keine unnötig komplexe Persistenzarchitektur gefordert.

## CORS

CORS ist für `http://localhost:5173` (Feedback-Board-Frontend) auf `/feedback` aktiviert.

## Technologie

- Java 17 (LTS, mit dem verfügbaren Container-Runtime verifizierbar; Spring Boot 3.2.x unterstützt 17–21)
- Spring Boot 3.2.x (Web, Validation)
- In-Memory-Speicher (ConcurrentHashMap)
- Port 8088
