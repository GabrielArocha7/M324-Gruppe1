# P2 – Testdokumentation

## Unit-Tests

Beide Services enthalten Unit-Tests für erfolgreiche und fehlerhafte Eingaben.

Die Tests werden lokal mit diesen Befehlen ausgeführt:

```bash
mvn -f Code/P2/flughafen-service/pom.xml test
mvn -f Code/P2/flug-service/pom.xml test
```

## Integrationstest

Der Test `FlightServiceIntegrationTest` prüft die Kommunikation vom Flugservice zum Flughafenservice.

Dabei wird die API-Anfrage an `/api/airports` simuliert. Es wird geprüft, dass:

- ein Flug mit vorhandenen Flughäfen erstellt werden kann;
- ein Flug mit einem unbekannten Flughafen abgelehnt wird.

Der Test verwendet keine echte Datenbank und keine echten Zugangsdaten. Er kann deshalb lokal und in GitHub Actions ausgeführt werden.

## CI

Die Datei `.github/workflows/p2-ci.yml` führt die Tests beider Services bei Pushes und Pull Requests gegen `main` aus. Die Testresultate sind danach im GitHub-Actions-Lauf sichtbar.

## Letztes lokales Testresultat

```text
flughafen-service: 5 Tests, 0 Fehler
flug-service: 4 Unit-Tests und 2 Integrationstests, 0 Fehler
```
