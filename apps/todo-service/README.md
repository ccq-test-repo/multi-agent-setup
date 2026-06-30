# To-Do REST Service

Eigenständiger, sofort lauffähiger To-Do-REST-Service für End-to-End-Pipeline-Tests.

## Build & Test

```bash
cd apps/todo-service
mvn clean test
```

## Starten

```bash
cd apps/todo-service
mvn spring-boot:run
```

Der Dienst läuft dann auf **http://localhost:8088**.

## API-Endpunkte

### POST /api/todos — Neues To-Do anlegen

```bash
curl -s -X POST localhost:8088/api/todos \
  -H 'Content-Type: application/json' \
  -d '{"title":"Milch kaufen"}'
```

→ **201 Created** mit Body `{ "id": 1, "title": "Milch kaufen", "done": false }`

### GET /api/todos — Alle To-Dos abrufen

```bash
curl -s localhost:8088/api/todos
```

→ **200 OK** mit Array aller To-Dos

### GET /api/todos/{id} — Einzelnes To-Do abrufen

```bash
curl -s localhost:8088/api/todos/1
```

→ **200 OK** mit dem To-Do oder **404 Not Found**

### PUT /api/todos/{id}/done — To-Do als erledigt markieren

```bash
curl -s -X PUT localhost:8088/api/todos/1/done
```

→ **200 OK** mit `"done": true` oder **404 Not Found**

### DELETE /api/todos/{id} — To-Do löschen

```bash
curl -s -X DELETE localhost:8088/api/todos/1
```

→ **204 No Content** oder **404 Not Found**

### Validierung

Leerer oder fehlender `title` → **400 Bad Request**

```bash
curl -s -o /dev/null -w '%{http_code}' -X POST localhost:8088/api/todos \
  -H 'Content-Type: application/json' \
  -d '{"title":""}'
```

→ `400`

## Beispiel-Session

```bash
# starten
mvn spring-boot:run &

# To-Do anlegen
curl -s -X POST localhost:8088/api/todos -H 'Content-Type: application/json' \
  -d '{"title":"Milch kaufen"}'
# → 201 { "id":1, "title":"Milch kaufen", "done":false }

# Liste abrufen
curl -s localhost:8088/api/todos
# → [ { "id":1, "title":"Milch kaufen", "done":false } ]

# Nicht vorhandene ID
curl -s -o /dev/null -w '%{http_code}' localhost:8088/api/todos/999
# → 404
```

## Technologie

- Java 21
- Spring Boot 3.2.x (Web, Validation)
- In-Memory-Speicher (ConcurrentHashMap)
- Eingebetteter Tomcat auf Port 8088
