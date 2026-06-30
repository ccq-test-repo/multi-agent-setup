# Standalone Calculator REST Service

Ein unabhängiger Calculator REST-Service, der via Docker gestartet werden kann.

## Quick Start mit Docker

```bash
cd apps/calculator

# Bauen
docker build -t calculator-app .

# Starten (Port 8088)
docker run -d -p 8088:8088 --name calculator calculator-app

# testen
curl -s -X POST localhost:8088/api/calculator/calculate \
  -H 'Content-Type: application/json' \
  -d '{"expression":"2+3*4"}'
# → {"result":14.0}

# Container stoppen
docker stop calculator && docker rm calculator
```

## Lokal bauen und starten (ohne Docker)

```bash
cd apps/calculator
mvn clean test        # Tests ausführen
mvn spring-boot:run   # Starten auf Port 8088
```

## API

### POST /api/calculator/calculate

**Request:** `{ "expression": "<arithmetic expression>" }`

**Response (200):** `{ "result": <number> }`

**Response (400):** `{ "error": "<message>" }`

Unterstützt: `+`, `-`, `*`, `/`, Klammern, negative Zahlen, Dezimalzahlen.

### Beispiele

```bash
curl -s -X POST localhost:8088/api/calculator/calculate \
  -H 'Content-Type: application/json' \
  -d '{"expression":"2+3"}'         → {"result":5.0}

curl -s -X POST localhost:8088/api/calculator/calculate \
  -H 'Content-Type: application/json' \
  -d '{"expression":"(2+3)*4"}'    → {"result":20.0}

curl -s -X POST localhost:8088/api/calculator/calculate \
  -H 'Content-Type: application/json' \
  -d '{"expression":"1/0"}'        → 400 {"error":"Division by zero"}
```

## Technologie

- Java 21, Spring Boot 3.2.x
- Docker (Multi-Stage Build, ~150 MB final image)
- Port 8088
