# multi-agent-setup

Multi-Agent Orchestrierung mit OpenClaw – ein technischer Prototyp zur Validierung von
agentengesteuerter Softwareentwicklung auf Basis eines heterogenen Tech-Stacks.

> **Status:** MVP-Phase | Aktive Komponenten: Node.js-Dienst (Hello World, Schaltjahr-Rechner)

---

## Projektbeschreibung

Dieses Repository enthält den Quellcode für eine **Multi-Agent-Entwicklungsumgebung**,
die über GitHub Issues und Projects orchestriert wird. Das System demonstriert, wie
mehrere KI-Agenten (Code Crafter, Sprint Orchestrator, QA) zusammenarbeiten, um
Software iterativ zu entwickeln, zu testen und zu deployen.

Längerfristig ist die Integration folgender Backend-Komponenten geplant:

- **Java 21 / Spring Boot 3** – Geschäftslogik und REST-API
- **PostgreSQL** – persistente Datenspeicherung
- **Flyway** – versionierte Datenbankmigrationen
- **Docker Compose** – lokale Entwicklungs- und Testumgebung
- **openrouteservice API** – Routenberechnung und Distanzermittlung
- **Open-Meteo API** – Wetterdaten für travel-bezogene Use Cases
- **SEPA-Prototyp** – simulierter Lastschrift-Prozess (Demo, keine echten Transaktionen)

---

## Tech-Stack (aktuell & geplant)

| Komponente                   | Aktuell                                 | Geplant / In Planung            |
|------------------------------|-----------------------------------------|----------------------------------|
| **Sprache**                  | Node.js (JavaScript)                    | Java 21 (Temurin)                |
| **Framework**                | –                                       | Spring Boot 3.x                  |
| **Build-Tool**               | – (node reicht)                         | Maven (Wrapper)                  |
| **Datenbank**                | –                                       | PostgreSQL 16                    |
| **Migrationen**              | –                                       | Flyway                           |
| **API-Gateway / Proxy**      | –                                       | Spring Cloud Gateway / Traefik   |
| **Containerisierung**        | –                                       | Docker Compose                   |
| **Test-Framework**           | `node:test` (Node.js built-in runner)   | JUnit 5 + Mockito                |
| **CI/CD**                    | – (GitHub Actions vorbereitet)          | GitHub Actions                   |
| **Statische Analyse**        | –                                       | Checkstyle, SpotBugs, PMD        |
| **Externe APIs**             | –                                       | openrouteservice, Open-Meteo     |
| **KI-Orchestrierung**        | OpenClaw (Code Crafter, Orchestrator)   | – (unverändert)                  |

---

## Voraussetzungen

- **Node.js** 22+ (für aktuelle Komponenten)
- **Java** 21 (Temurin JDK, für geplante Backend-Komponenten)
- **Maven** 3.9+ (Wrapper `./mvnw` liegt im Repo, sobald Java-Komponenten hinzugefügt sind)
- **Docker** 24+ & **Docker Compose** v2 (für Container-Start geplanter Services)
- **Git** (für Workflow-Integration mit GitHub)

> Hinweis: Die Java-, Docker- und PostgreSQL-Komponenten befinden sich noch im Aufbau.
> Die aktuell lauffähigen Komponenten (siehe [Aktuelle Komponenten](#aktuelle-komponenten))
> benötigen nur Node.js.

---

## Aktuelle Komponenten

### 1. `hello.js` – Hello World

Einfachste Ausgabe zur Validierung der Node.js-Umgebung.

```bash
node hello.js
# Ausgabe: Hello, World!
```

### 2. `leap-year.js` – Schaltjahr-Rechner

Berechnet die Anzahl der Tage bis zum nächsten Schaltjahr.
Enthält die exportierten Funktionen `isLeapYear`, `formatDate` und `daysUntilNextLeapYear`.

**Direkter Aufruf:**
```bash
node leap-year.js
# Beispiel-Ausgabe:
# Heute: 2026-06-23
# Nächstes Schaltjahr beginnt am 2028-01-01
# Noch 557 Tage bis zum nächsten Schaltjahr
```

**Als Modul:**
```js
const { isLeapYear, daysUntilNextLeapYear } = require('./leap-year.js');

console.log(isLeapYear(2024)); // true
console.log(isLeapYear(2025)); // false
```

### 3. `leap-year.test.js` – Tests (Node.js Test Runner)

Das Projekt verwendet den integrierten Node.js Test Runner (`node:test`).
Keine externen Test-Frameworks nötig.

```bash
node --test leap-year.test.js
```

Oder alle Tests im Projekt ausführen:
```bash
node --test
```

---

## Entwicklungsumgebung starten (geplant: Docker Compose)

Sobald die Java-Backend-Komponenten hinzugefügt sind, erfolgt der Start über Docker Compose:

```bash
docker compose up --build
```

Damit werden gestartet:
- Spring Boot App (Port 8080)
- PostgreSQL (Port 5432)
- Flyway-Migrationen beim App-Start

> **Aktuell:** Dieser Schritt ist noch nicht umgesetzt. Für die Node.js-Komponenten
> ist kein Container erforderlich – `node *.js` genügt.

---

## Environment-Variablen

Folgende Umgebungsvariablen werden von den geplanten Backend-Komponenten benötigt:

| Variable                     | Beschreibung                                  | Beispielwert                          |
|------------------------------|-----------------------------------------------|---------------------------------------|
| `DATABASE_URL`               | JDBC-URL zur PostgreSQL-Datenbank             | `jdbc:postgresql://localhost:5432/mas` |
| `DATABASE_USER`              | PostgreSQL-Benutzer                           | `mas_user`                            |
| `DATABASE_PASSWORD`          | PostgreSQL-Passwort                           | `<sicheres Passwort>`                 |
| `OPENROUTESERVICE_API_KEY`   | API-Key für openrouteservice (Routing)        | `<dein-key>`                          |
| `OPEN_METEO_BASE_URL`        | Basis-URL für Open-Meteo API                  | `https://api.open-meteo.com/v1`       |
| `SEPA_MOCK_MODE`             | SEPA im Mock-Modus (keine echten Transaktionen)| `true`                                |
| `TLS_ENABLED`                | TLS aktivieren (produktionsnahes Deployment)  | `false` (im Dev) / `true` (Produktion)|
| `TLS_CERT_PATH`              | Pfad zum TLS-Zertifikat                       | `/etc/certs/server.crt`               |
| `TLS_KEY_PATH`               | Pfad zum TLS-Private-Key                      | `/etc/certs/server.key`               |

Ein Template für lokale Entwicklung liegt unter `.env.example` (sobald erstellt).

> **Sicherheitshinweis:** `.env`-Dateien sind in `.gitignore` – niemals Secrets committen.
> Verwende stattdessen `.env.example` als Vorlage und fülle die Werte lokal.

---

## API-Key-Konfiguration

### openrouteservice

Für Routenberechnung wird ein kostenloser API-Key von [openrouteservice.org](https://openrouteservice.org/) benötigt:

1. Registrieren unter https://openrouteservice.org/dev/#/signup
2. API-Key kopieren
3. In Umgebungsvariable `OPENROUTESERVICE_API_KEY` setzen

### Open-Meteo

[Open-Meteo](https://open-meteo.com/) ist eine freie Wetter-API ohne API-Key.
Die Basis-URL `https://api.open-meteo.com/v1` ist der Default-Wert.

---

## Datenbankmigrationen (Flyway) – geplant

Sobald die Datenbank-Komponente integriert ist, werden Migrationen via Flyway verwaltet:

```
src/main/resources/db/migration/
├── V1__init_schema.sql
├── V2__add_users.sql
├── V3__add_routes.sql
└── V4__add_sepa_mandates.sql
```

Migrationen laufen automatisch beim Spring Boot-Start (dank `spring.flyway.enabled=true`).
Manuelles Ausführen:

```bash
./mvnw flyway:migrate -Dflyway.url=jdbc:postgresql://localhost:5432/mas \
                       -Dflyway.user=mas_user \
                       -Dflyway.password=<pass>
```

---

## Testausführung

### Aktuelle Node.js-Komponenten

```bash
# Alle Tests ausführen
node --test

# Nur leap-year-Tests
node --test leap-year.test.js

# Coverage (sobald c8/nanocollector verfügbar)
# npx c8 node --test
```

### Geplante Java-Komponenten

```bash
./mvnw test                  # Unit-Tests
./mvnw verify                # Integration-Tests + Quality-Gates
./mvnw checkstyle:check      # Statische Code-Analyse
```

---

## Bekannte Einschränkungen

1. **Node.js als Übergangslösung** – Die aktuelle Codebasis ist in JavaScript, die
   Ziel-Architektur sieht Java 21 / Spring Boot vor. Sobald das Java-Backend steht,
   werden die Node.js-Komponenten obsolet.
2. **Keine Persistenz aktuell** – `leap-year.js` arbeitet rein im Speicher.
3. **Kein API-Endpunkt** – Der Dienst wird aktuell nur per CLI genutzt.
4. **Docker Compose fehlt** – Container-Start ist vorbereitet, aber noch nicht implementiert.
5. **Flyway ohne Migrationen** – Datenbank-Migrationsskripte sind noch nicht erstellt.
6. **Keine Fehlerbehandlung bei fehlenden API-Keys** – In der aktuellen Version nicht
   relevant, da noch keine externen APIs angebunden sind.
7. **`.gitignore` enthält Java-typische Muster** – Diese wurden aus einem Template übernommen
   und sind für das Node.js-Projekt aktuell nicht relevant, aber unschädlich.

---

## MVP-Abgrenzung (Nicht-Ziele)

Die folgenden Features sind **bewusst nicht** Teil des aktuellen MVPs:

- ❌ **Mobile App** – Kein iOS/Android-Client. Die Interaktion erfolgt über GitHub und CLI.
- ❌ **Vollständiges Offline** – Externe APIs (openrouteservice, Open-Meteo) setzen
  Netzwerkverbindung voraus. Lokales Caching ist nicht vorgesehen.
- ❌ **Bankanbindung / Echt-Abbuchung** – SEPA-Transaktionen sind rein simuliert.
  Es findet keine Kommunikation mit echten Banken oder Zahlungsdienstleistern statt.

---

## SEPA-Prototyp

Der SEPA-Prototyp ist eine **Demo-Komponente** ohne echte Abbuchungsfunktion:

- Lastschrift-Mandate werden in der Datenbank gespeichert, aber **nie** an eine Bank übermittelt.
- Die Umgebungsvariable `SEPA_MOCK_MODE=true` stellt sicher, dass keine externen
  Zahlungssysteme kontaktiert werden.
- IBANs und Mandatsreferenzen dienen ausschließlich der UI-Validierung und Logik-Tests.
- **Niemals** echte Bankdaten in Entwicklungsumgebungen verwenden!

---

## Datenschutz & Standortdaten

- **Standortdaten** (Start-/Zielkoordinaten für Routenberechnung) werden nur temporär
  für die API-Anfrage an openrouteservice verwendet und **nicht persistent gespeichert**.
- **Open-Meteo liefert Wetterdaten ohne Tracking** – keine Cookies, keine Nutzerprofile.
- **Keine Personenbezogene Daten** werden durch die aktuellen Komponenten verarbeitet.
  Sobald die User-Tabelle eingeführt wird, gelten die Vorgaben der DSGVO (Art. 5, 6, 17).

---

## TLS-Konfiguration (produktionsnahes Deployment)

Für ein TLS-fähiges Deployment wird die Umgebungsvariable `TLS_ENABLED=true` gesetzt.
Die Zertifikats- und Key-Pfade werden über `TLS_CERT_PATH` und `TLS_KEY_PATH`
konfiguriert.

Beispiel für selbstsignierte Zertifikate (lokale Entwicklung):

```bash
# Selbstsigniertes Zertifikat erstellen
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout server.key -out server.crt \
  -subj "/CN=localhost"

# Umgebungsvariablen setzen
export TLS_ENABLED=true
export TLS_CERT_PATH=/etc/certs/server.crt
export TLS_KEY_PATH=/etc/certs/server.key
```

> **Hinweis:** Selbstsignierte Zertifikate sind nur für lokale Tests geeignet.
> Für produktive Umgebungen sind CA-signierte Zertifikate (z. B. Let's Encrypt) zu verwenden.

---

## Mögliche spätere Ausbaustufen

- **REST-API mit Spring Boot** – Vollständiges Backend mit CRUD-Endpunkten
- **Docker Compose** – Orchestrierte Container für App, DB, und Reverse Proxy
- **CI/CD via GitHub Actions** – Automatisierte Builds, Tests, Deployments
- **Monitoring** – Health-Endpunkte, Metriken (Micrometer / Prometheus), Log-Aggregation
- **Multi-Tenant-Fähigkeit** – Isolation von Kundendaten
- **Rate Limiting & Auth** – API-Key-Verwaltung, JWT-Authentifizierung
- **Webhook-Integration** – Ereignisgesteuerte Benachrichtigungen
- **KI-gestützte Route-Optimierung** – Automatisierte Vorschläge basierend auf Wetter + Verkehr

---

## Lizenz

Dieses Projekt ist ein Prototyp und unterliegt keiner spezifischen Lizenz.
Alle Rechte vorbehalten – ccq-test-repo.
