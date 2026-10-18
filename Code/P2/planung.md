# P2 – Schritt-für-Schritt-Plan

## Vorbereitung – erledigt

- Java 17/21 installiert und geprüft
- Maven installiert und geprüft
- (MacBook) Mac-Architektur und Java-Version funktionieren

## Schritt 1: Planung und Grundstruktur — erledigt

- Architektur der beiden Microservices festlegen.
- Endpunkte und Datenmodelle definieren.
- Fehlerfälle und HTTP-Statuscodes festlegen.
- MongoDB-Atlas-Datenbanken lokal mit den Sces verbinden und prüfen.
- Zwei getrennte Spring-Boot-Maven-Projekte unter `/P2` erstellen.
- Branch-, Pull-Request- und Review-Prozess dokumentieren.

**Kontrolle:** Beide Projekte lassen sich mit Maven bauen. Die Atlas-Verbindung funktioniert; die leere Flughafendatenbank liefert korrekt `404 Not Found`. (Im nächsten Schritt werden Objekte erstellt)

## Schritt 2: Microservice Flughäfen 

1. Flughafen-Datenmodell und Datenbankanbindung erstellen.
2. `POST /api/airports` implementieren.
3. Eingaben, ID, Erstellungsdatum und doppelte Kürzel behandeln.
4. `GET /api/airports` implementieren.
5. Pro Endpoint mindestens zwei bis drei Unit Tests mit JavaDoc schreiben.

**Kontrolle:** Flughäfen können erstellt, gespeichert und abgerufen werden; alle Tests sind erfolgreich.

## Schritt 3: Microservice Flüge 

1. Flug-Datenmodell und Datenbankanbindung erstellen.
2. `POST /api/flights` implementieren.
3. Start, Landung, Flugzeugtyp und unterschiedliche Flughäfen validieren.
4. Flughafenservice über dessen API aufrufen.
5. Fehler beim Flughafenservice behandeln.
6. Mindestens zwei bis drei Unit Tests mit JavaDoc schreiben.

**Kontrolle:** Gültige Flüge werden gespeichert und ungültige Eingaben korrekt abgelehnt; alle Unit Tests sind erfolgreich.

## Schritt 4: Integration und Abschluss — umgesetzt

1. Kommunikation zwischen beiden Services testen.
2. Integrationstests erstellen.
3. Unit- und Integrationstests in GitHub Actions ausführen.
4. Testresultate und Logs sichern.
5. Code Reviews durchführen.
6. KI-Nutzung dokumentieren.
7. Gesamte Abgabe kontrollieren.

**Kontrolle:** Alle Tests laufen lokal erfolgreich. Die CI-Pipeline ist eingerichtet und wird nach dem Push in GitHub ausgeführt.

**Ergebnis:** Beide Unit-Test-Suites und der Integrationstest laufen lokal erfolgreich. Die GitHub-Actions-Datei führt dieselben Tests bei Pushes und Pull Requests aus.
