# Guestbook REST API

Eigenständiger Gästebuch-REST-Service in `apps/guestbook-api/`.

## Build & Test

```bash
cd apps/guestbook-api
mvn clean test
```

## Starten

```bash
cd apps/guestbook-api
mvn spring-boot:run
```

Läuft auf **http://localhost:8088**.

## API-Endpunkte

### POST /api/messages — Eintrag anlegen

```bash
curl -s -X POST localhost:8088/api/messages \
  -H 'Content-Type: application/json' \
  -d '{"author":"Alice","text":"Hallo Welt"}'
```

→ **201 Created** mit `{ "id": 1, "author": "Alice", "text": "Hallo Welt" }`

### GET /api/messages — Alle Einträge abrufen

```bash
curl -s localhost:8088/api/messages
```

→ **200 OK** mit Array aller Einträge

### Validierung (400 Bad Request)

```bash
curl -s -o /dev/null -w '%{http_code}' -X POST localhost:8088/api/messages \
  -H 'Content-Type: application/json' \
  -d '{"author":"","text":""}'
# → 400

curl -s -X POST localhost:8088/api/messages \
  -H 'Content-Type: application/json' \
  -d '{"author":"","text":""}'
# → {"error":"author must not be blank"}
```

## CORS

CORS ist für `http://localhost:5173` aktiviert (alle `api/**`-Endpunkte).

## Technologie

- Java 21
- Spring Boot 3.2.x (Web, Validation)
- In-Memory-Speicher (ConcurrentHashMap)
- Port 8088
