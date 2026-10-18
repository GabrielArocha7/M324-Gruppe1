# P3 – Continuous Integration

## 1. GitHub Actions

Unser Code liegt auf GitHub, deshalb verwenden wir GitHub Actions.

- **Workflow:** YAML-Datei in `.github/workflows/`. Er startet bei einem Event, z. B. `push`, `pull_request` oder manuell (`workflow_dispatch`).
- **Job:** läuft auf einem eigenen, frischen Runner (VM von GitHub). Jobs laufen parallel, ausser sie sind mit `needs` verkettet.
- **Step:** ein Befehl (`run`) oder eine fertige Action (`uses`) innerhalb eines Jobs.
- **Matrix:** startet denselben Job mehrmals mit anderen Werten, bei uns einmal pro Service.
- **Artefakt:** Datei, die ein Job hochlädt (z. B. JAR oder Testreport).
- **Cache:** Maven-Abhängigkeiten werden zwischen Runs wiederverwendet.

Weil jeder Job auf einem neuen Runner startet, muss jeder Job den Code neu auschecken und Java einrichten. Das JAR wird zwischen den Jobs als Artefakt weitergegeben.

## 2. Branching-Strategie und Trigger

Wir behalten die Strategie aus P2 ([P2-Prozess.md](P2-Prozess.md)): kurzlebige `feature/*`-Branches, Pull Request auf `main`, Review, Squash-Merge.

| Event | Branch | Workflow | Zweck |
|---|---|---|---|
| `push` | `feature/**` | `ci.yml` | Feedback während der Entwicklung |
| `pull_request` | Ziel `main` | `ci.yml` | Prüfung vor dem Merge |
| `push` | `main` | `ci.yml` | Stand nach dem Merge prüfen (in P3b auch Publish) |
| manuell | beliebig | alle Workflows | z. B. durch die Lehrperson |

Beim `push` läuft `ci.yml` nur, wenn sich etwas unter `Code/P2/` oder an der Pipeline ändert. Reine Doku-Änderungen lösen keinen Lauf aus. Beim Pull Request gibt es diesen Filter nicht, weil der Check für den Merge verlangt wird und sonst nie starten würde.

Die Vergleichsvarianten (`ci-variant-*.yml`) starten nur manuell, damit nicht bei jedem Push sechs Workflows laufen.

Regeln für `main`:

- Merge nur über Pull Request mit mindestens einem Review
- alle Jobs von `CI` müssen grün sein (Required Status Checks)
- keine direkten Pushes

Screenshot des Rulesets: *(folgt, sobald das Ruleset eingerichtet ist)*

Damit die Lehrperson die Pipelines selbst starten kann, muss sie Collaborator im Repository `ellisabeta/M324-Gruppe1` sein. Danach kann sie unter „Actions“ jeden Workflow mit „Run workflow“ starten.

## 3. Teststufen

| Stufe | Was wird geprüft | Dateien | Wo in `ci.yml` |
|---|---|---|---|
| Unit | Controller einzeln, Service gemockt | `AirportControllerTest`, `FlightControllerTest` | Job `unit-test` (`mvn test`) |
| Unit mit HTTP-Mock | Flugservice ruft den Flughafenservice auf, die Antwort wird simuliert | `FlightServiceIntegrationTest` (aus P2) | Job `unit-test` |
| Integration | ganzer Service mit echter MongoDB im Container (Testcontainers) | `AirportApiIT`, `FlightApiIT` | Job `integration-test` (`mvn verify -DskipUnitTests`) |
| System | beide Services und MongoDB mit docker compose, echte HTTP-Aufrufe | `Code/P2/system-test/system-test.sh` | Job `system-test` |

Maven trennt die Stufen über den Namen: `*Test` läuft mit Surefire (`mvn test`), `*IT` mit Failsafe (`mvn verify`). `FlightServiceIntegrationTest` braucht keine Datenbank und läuft deshalb bei den Unit Tests mit.

Integrationstests:

- `AirportApiIT`: Flughafen wird gespeichert und mit GET wieder geliefert, doppeltes Kürzel gibt `409`, leere Datenbank gibt `404`.
- `FlightApiIT`: gültiger Flug wird gespeichert, unbekannter Flughafen gibt `404` (nichts gespeichert), Flughafenservice nicht erreichbar gibt `502`.

Der Systemtest macht 11 Prüfungen: Flughäfen anlegen, Duplikat, ungültiges Kürzel, Flug anlegen, unbekannter Flughafen, gleicher Flughafen, Landung vor Start. Am Schluss wird der Flughafenservice gestoppt, dann muss der Flugservice `502` liefern.

## 4. Varianten

| | Datei | Aufbau | Cache | Systemtest |
|---|---|---|---|---|
| P2 | `p2-ci.yml` (bis P3) | 1 Job, nur Unit Tests | ja | nein |
| A | `ci-variant-a.yml` | 1 Job, alles nacheinander | nein | ja |
| B | `ci-variant-b.yml` | 4 Jobs nacheinander (Build, Unit, Integration, System) | ja | ja |
| C | `ci-variant-c.yml` | Matrix: 1 Job pro Service, danach Systemtest | ja | ja |
| D | `ci-variant-d-*.yml` | eigener Workflow pro Service, startet nur bei Änderungen an diesem Service | ja | nein |
| Final | `ci.yml` | 4 Stufen als Jobs, jede Stufe als Matrix pro Service | ja | ja |

### Messwerte

Laufzeit = Dauer des ganzen Runs. Runner-Zeit = Summe aller Jobs, also die Rechenzeit, die GitHub verbraucht.

| | Laufzeit Run 1 | Laufzeit Run 2 | Runner-Zeit Run 1 | Runner-Zeit Run 2 |
|---|---|---|---|---|
| P2 | – | 34 s | – | 30 s |
| A | 150 s | 154 s | 147 s | 150 s |
| B | 243 s | 218 s | 229 s | 204 s |
| C | 140 s | 70 s (Abbruch, siehe Kapitel 6) | 188 s | – |
| D (Flughafen / Flug) | 59 s / 65 s | 48 s / 56 s | 55 s / 60 s | 43 s / 51 s |
| Final | 238 s | 203 s | 302 s | 254 s |

Run 1: Commit `cb52f30` vom 27.09.2026 – [A](https://github.com/ellisabeta/M324-Gruppe1/actions/runs/36346484640), [B](https://github.com/ellisabeta/M324-Gruppe1/actions/runs/36346484745), [C](https://github.com/ellisabeta/M324-Gruppe1/actions/runs/36346484805), [D Flughafen](https://github.com/ellisabeta/M324-Gruppe1/actions/runs/36346484818), [D Flug](https://github.com/ellisabeta/M324-Gruppe1/actions/runs/36346485009), [Final](https://github.com/ellisabeta/M324-Gruppe1/actions/runs/36346484964)

Run 2: Commit `411b6fc` vom 27.09.2026 – [A](https://github.com/ellisabeta/M324-Gruppe1/actions/runs/36349819914), [B](https://github.com/ellisabeta/M324-Gruppe1/actions/runs/36349820045), [C](https://github.com/ellisabeta/M324-Gruppe1/actions/runs/36349820046), [D Flughafen](https://github.com/ellisabeta/M324-Gruppe1/actions/runs/36349820103), [D Flug](https://github.com/ellisabeta/M324-Gruppe1/actions/runs/36349820067), [Final](https://github.com/ellisabeta/M324-Gruppe1/actions/runs/36349820063)

P2: [Run auf main](https://github.com/ellisabeta/M324-Gruppe1/actions/runs/39150415534)

Testergebnis im finalen Workflow (Run 1): 5 + 4 Unit Tests, 3 + 3 Integrationstests und 11 von 11 Systemtest-Prüfungen bestanden.

### Beobachtungen

- **P2:** schnell, prüft aber nur Unit Tests. Fehler, die erst mit Datenbank oder im Zusammenspiel der Services auftreten, findet er nicht.
- **A:** einfach zu lesen. Wenn etwas fehlschlägt, muss man im langen Log suchen, welche Stufe es war.
- **B:** man sieht sofort, welche Stufe rot ist. Jeder Job setzt aber neu auf (Checkout, Java, Cache), dadurch ist B am langsamsten.
- **C:** am schnellsten mit Systemtest, weil die Services parallel laufen. Unit- und Integrationstests sind aber wieder in einem `mvn verify` zusammen.
- **D:** baut nur den geänderten Service und ist darum am schnellsten. Einen Systemtest gibt es nicht, weil kein Workflow beide Services kennt.

### Fehlerfall

Auf dem Branch `feature/P3-fehlerfall` erwartet ein Test in `AirportControllerTest` absichtlich `200` statt `409`.

*(Ergebnis folgt nach dem Lauf)*

## 5. Finale Variante

Wir verwenden `ci.yml`. Sie verbindet B und C: jede Teststufe ist ein eigener Job, und jede Stufe läuft parallel für beide Services.

- Bei einem Fehler sieht man direkt, welche Stufe und welcher Service betroffen ist.
- Die schnellen Tests laufen zuerst. Schlagen die Unit Tests fehl, starten Integrations- und Systemtest gar nicht.
- Durch die Matrix ist sie schneller als B (203 s statt 218 s in Run 2).
- Der Build-Job liefert pro Service ein JAR, daran hängt P3b den Publish-Schritt an.

Nachteil: C ist schneller, und die finale Variante braucht am meisten Runner-Zeit, weil jeder Job neu aufsetzt. Für zwei kleine Services nehmen wir das in Kauf, weil uns die klare Fehlerzuordnung wichtiger ist. D haben wir nicht gewählt, weil wir den Systemtest bei jeder Änderung wollen.

## 6. Effizienz und Stabilität

- **Cache:** Maven-Abhängigkeiten werden mit `actions/setup-java` (`cache: maven`) gespeichert. Im finalen Workflow dauerte der Build-Job in Run 1 49 s / 25 s, in Run 2 mit vollem Cache nur noch 14 s / 17 s. Bei B ging der Build-Job von 40 s auf 24 s.
- **Parallel:** Die Matrix lässt beide Services gleichzeitig laufen.
- **Alte Läufe abbrechen:** Ein neuer Push auf denselben Branch bricht den laufenden Run ab (`concurrency`).
- **Timeouts:** Jeder Job hat `timeout-minutes`, ein hängender Test blockiert die Pipeline also nicht.
- **Feste Runner-Version:** `ubuntu-24.04` statt `ubuntu-latest`.
- **Fehleranalyse:** Testreports werden auch bei Fehlern hochgeladen. Beim Systemtest werden bei einem Fehler die Container-Logs ausgegeben.
- **Rechte:** Die Pipeline darf das Repository nur lesen (`permissions: contents: read`).
- **Keine Secrets:** Die Tests nutzen eine MongoDB im Container, nicht unsere Atlas-Datenbank.

In Run 2 ist Variante C beim Systemtest abgebrochen, ohne dass ein Test falsch war. Docker Hub hat beim Herunterladen von `mongo:7.0` die Verbindung getrennt:

```text
Error response from daemon: Head "https://registry-1.docker.io/v2/library/mongo/manifests/7.0": ... connection reset by peer
```

Solche Fehler kommen von aussen. Man erkennt sie im Log daran, dass der Fehler vor dem ersten Test passiert. Die Lösung ist, den Run neu zu starten („Re-run failed jobs“).

## 7. Vorbereitung für P3b

Der Job `build` in `ci.yml` erstellt pro Service ein JAR und lädt es als Artefakt `<service>-<commit-sha>` hoch. In P3b kommt nach dem Systemtest ein Job dazu, der daraus ein Docker-Image baut und publiziert.

## 8. KI-Nachweis

Die Pipelines, die Integrationstests und der Systemtest wurden mit Claude Code erstellt. Wir haben jede Version auf GitHub laufen lassen und die Logs geprüft, bevor wir sie übernommen haben. Secrets oder Zugangsdaten wurden nicht in die KI eingegeben.

### Review 1: veraltete Actions

**Anfrage:** Claude Code sollte den ersten Lauf von Variante A prüfen ([Run](https://github.com/ellisabeta/M324-Gruppe1/actions/runs/36343278338)). Der Lauf war grün, zeigte aber Warnungen:

```text
! Node.js 20 is deprecated. The following actions target Node.js 20 but are being forced to run on Node.js 24: actions/checkout@v4, actions/setup-java@v4, actions/upload-artifact@v4.
! setup-java v4 is deprecated and will no longer receive updates. Please migrate to actions/setup-java@v5.
- The ubuntu-latest label will migrate to Ubuntu 26 beginning October 19, 2026.
```

**KI-Hinweis:** Die Actions auf die aktuellen Versionen heben und statt `ubuntu-latest` eine feste Runner-Version verwenden.

**Geänderte Stelle:**

```yaml
# vorher
runs-on: ubuntu-latest
- uses: actions/checkout@v4
- uses: actions/setup-java@v4
# nachher
runs-on: ubuntu-24.04
- uses: actions/checkout@v7
- uses: actions/setup-java@v6
```

**Validierung:** Im nächsten Lauf ([Run](https://github.com/ellisabeta/M324-Gruppe1/actions/runs/36346484964)) gab es keine Warnungen mehr, alle Jobs waren grün.

**Schlussfolgerung:** Ein grüner Run heisst nicht, dass alles in Ordnung ist. Die Warnungen hätten die Pipeline später kaputt gemacht. Mit der festen Runner-Version entscheiden wir selbst, wann wir wechseln.

### Review 2: Testcontainers-Version

**KI-Hinweis:** Beim Erstellen der Integrationstests wies Claude Code darauf hin, dass die von Spring Boot 3.3.5 mitgelieferte Testcontainers-Version 1.19.8 nicht zur aktuellen Docker-Version auf den GitHub-Runnern passt. Laut den Release Notes von Testcontainers behebt Version 1.21.4 das.

**Geänderte Stelle:** `pom.xml` beider Services:

```xml
<testcontainers.version>1.21.4</testcontainers.version>
```

**Validierung:** Im Job „Integrationstests“ startet der Container und die Tests laufen durch ([Run](https://github.com/ellisabeta/M324-Gruppe1/actions/runs/36346484964)):

```text
INFO tc.mongo:7.0 -- Container mongo:7.0 started in PT1.577904827S
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0 -- in ch.tbz.flug.flight.FlightApiIT
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0 -- in ch.tbz.flughafen.airport.AirportApiIT
```

**Schlussfolgerung:** Die Versionen, die Spring Boot vorgibt, passen nicht immer zur Umgebung in der CI. Version 1.19.8 haben wir nicht selbst ausprobiert, wir haben uns auf die Release Notes verlassen. Mit 1.21.4 laufen die Integrationstests in jedem Run.
