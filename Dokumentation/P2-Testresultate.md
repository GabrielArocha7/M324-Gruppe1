# P2 – Testresultate

Die Tests wurden am 28.09.2026 lokal mit Java 17 ausgeführt.

```bash
mvn -f Code/P2/flughafen-service/pom.xml test
mvn -f Code/P2/flug-service/pom.xml test
```

Ergebnis:

```text
flughafen-service: 5 Tests, 0 Fehler
flug-service: 4 Unit-Tests und 2 Integrationstests, 0 Fehler
```

Die vollständigen CI-Logs sind nach dem Push im jeweiligen GitHub-Actions-Lauf sichtbar.
