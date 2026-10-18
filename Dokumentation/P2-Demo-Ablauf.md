# P2 – Demo-Ablauf

Die Demo dauert ungefähr 10 bis 15 Minuten.

## 1. Vorbereitung

Zuerst prüfen wir den Branch und den Git-Status:

```bash
git status
git branch --show-current
```

Danach starten wir beide Services:

```bash
./start-p2.sh
```

Erwartete Ausgabe:

```text
Java: openjdk version "21..."
Started FlughafenServiceApplication on port 8081
Started FlugServiceApplication on port 8082
```

## 2. Projektstruktur

```text
Code/P2/
├── flughafen-service/
├── flug-service/
└── planung.md
```

Wir haben zwei getrennte Spring-Boot-Microservices. Jeder Service hat eigene Endpoints, Tests, Maven-Konfigurationen und eine eigene Datenbank.

Der Flugservice greift nicht direkt auf die Flughafendatenbank zu, sondern ruft den Flughafenservice über den `AirportClient` auf.

## 3. Flughafenservice

### Flughäfen abrufen

```bash
curl -i http://localhost:8081/api/airports
```

Erwartet wird `HTTP/1.1 200`.

### Flughafen erstellen

```bash
curl -i -X POST http://localhost:8081/api/airports \
  -H "Content-Type: application/json" \
  -d '{"name":"John F. Kennedy Airport","code":"JFK","capacity":25000}'
```

Erwartet wird `HTTP/1.1 201`.

### Doppeltes Kürzel testen

```bash
curl -i -X POST http://localhost:8081/api/airports \
  -H "Content-Type: application/json" \
  -d '{"name":"Another Zurich Airport","code":"ZRH","capacity":30000}'
```

Erwartet wird `HTTP/1.1 409`, weil das Kürzel bereits existiert.

## 4. Flugservice

### Einen gültigen Flug erstellen

```bash
curl -i -X POST http://localhost:8082/api/flights \
  -H "Content-Type: application/json" \
  -d '{
    "departureAirportCode":"ZRH",
    "arrivalAirportCode":"JFK",
    "departureAt":"2026-10-01T08:00:00Z",
    "arrivalAt":"2026-10-01T16:00:00Z",
    "aircraftType":"Boeing 727"
  }'
```

Erwartet wird `HTTP/1.1 201`.

Dabei prüft der Flugservice über die Flughafen-API, ob ZRH und JFK existieren.

### Gleiche Flughäfen testen

```bash
curl -i -X POST http://localhost:8082/api/flights \
  -H "Content-Type: application/json" \
  -d '{
    "departureAirportCode":"ZRH",
    "arrivalAirportCode":"ZRH",
    "departureAt":"2026-10-01T08:00:00Z",
    "arrivalAt":"2026-10-01T16:00:00Z",
    "aircraftType":"Boeing 727"
  }'
```

Erwartet wird `HTTP/1.1 400`.

### Unbekannten Flughafen testen

```bash
curl -i -X POST http://localhost:8082/api/flights \
  -H "Content-Type: application/json" \
  -d '{
    "departureAirportCode":"ZRH",
    "arrivalAirportCode":"XXX",
    "departureAt":"2026-10-01T08:00:00Z",
    "arrivalAt":"2026-10-01T16:00:00Z",
    "aircraftType":"Boeing 727"
  }'
```

Erwartet wird `HTTP/1.1 404`.

Für `/api/flights` gibt es keinen GET-Endpoint. Ein GET liefert deshalb korrekt `405 Method Not Allowed`.

## 5. MongoDB Atlas

In MongoDB Atlas zeigen wir:

```text
Cluster0
├── flughafen_db
│   └── airports
└── flug_db
    └── flights
```

Nach dem Erstellen eines Flugs aktualisieren wir die Collection `flug_db.flights` und zeigen den gespeicherten Datensatz.

Passwörter und Connection Strings werden nicht gezeigt.

## 6. Unit-Tests

```bash
mvn -f Code/P2/flughafen-service/pom.xml test
mvn -f Code/P2/flug-service/pom.xml test
```

Erwartetes Ergebnis:

```text
flughafen-service: 5 Tests, 0 Fehler
flug-service: 6 Tests, 0 Fehler
```

Danach zeigen wir:

```text
AirportControllerTest.java
FlightControllerTest.java
FlightServiceIntegrationTest.java
```

Die Tests prüfen Happy Paths und Sad Paths. Die Testmethoden sind mit JavaDoc beschrieben.

## 7. Integrationstest

Der `FlightServiceIntegrationTest` prüft die Kommunikation vom Flugservice zum Flughafenservice.

Getestet wird:

- Flug mit vorhandenen Flughäfen wird erstellt.
- Flug mit unbekanntem Flughafen wird abgelehnt.

Der Test benötigt keine echte Datenbank und keine Passwörter.

## 8. CI-Pipeline

Auf GitHub öffnen wir:

```text
Actions → CI → letzter Lauf
```

Zusätzlich zeigen wir die Datei:

```text
.github/workflows/ci.yml
```

Die Pipeline führt die Tests beider Maven-Projekte bei Pushes und Pull Requests aus. Details zur CI stehen in `P3-CI.md`.

## 9. Git-Prozess und Code Review

Wir zeigen auf GitHub:

- Issues und Sub-Issues
- Feature-Branches
- Pull Requests
- Review-Kommentare
- behobene Review-Kommentare
- den Merge nach `main`

Wir arbeiten mit Feature-Branches. Änderungen werden über Pull Requests eingereicht und vor dem Merge reviewed.

## 10. KI-Nachweis

Die Datei `Dokumentation/P2-KI-Nachweis.md` wird geöffnet.

Als Beispiel erklären wir:

> Bei einem MongoDB-SSL-Fehler habe ich die Logs analysiert, die IP-Freigabe in Atlas geprüft und die Verbindung danach erneut getestet. KI-Vorschläge wurden nicht ungeprüft übernommen, sondern mit Tests und manuellen Prüfungen kontrolliert.

## Abschluss

Zum Schluss zeigen wir, dass die Struktur, die beiden Microservices, die API-Kommunikation, die Datenbanken, die Tests, die CI-Pipeline und der Review-Prozess vorhanden und dokumentiert sind.
