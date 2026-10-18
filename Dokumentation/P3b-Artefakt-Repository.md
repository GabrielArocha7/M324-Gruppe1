# P3b – Artefakt-Repository

## 1. Artefakt-Typ und Repository

Bei uns gibt es drei Stufen:

- **Sourcecode:** der Java-Code im Repository unter `Code/P2/`.
- **Build-Artefakt:** das JAR, das `mvn package` im Job `build` erzeugt. Es braucht zum Starten noch eine passende Java-Installation.
- **Deployment-Artefakt:** ein Docker-Image, das das JAR und Java 17 enthält. Es läuft überall, wo Docker installiert ist.

Wir publizieren **Docker-Images** in die **GitHub Container Registry (GHCR)**.

Gründe:

- Unsere Services sollen in P4 deployt werden. Ein Image kann man direkt mit Docker starten, ein JAR bräuchte auf dem Server noch Java und ein Startskript.
- Für beide Services gab es aus P3 schon ein Dockerfile, der Systemtest startet die Services bereits als Container.
- Code, Pipeline und Images liegen alle auf GitHub. Die Pipeline meldet sich mit dem `GITHUB_TOKEN` an, den GitHub automatisch bereitstellt. Wir brauchen kein eigenes Passwort.

Verglichene Alternativen:

- **Docker Hub:** braucht einen eigenen Account und ein Access Token als Secret. Im Gratis-Plan gibt es Download-Limits.
- **GitHub Packages (Maven):** würde das JAR publizieren. Das passt eher zu einer Bibliothek, die andere Projekte einbinden, nicht zu einem Service, den man startet.

## 2. Namenskonvention und Versionierung

```text
ghcr.io/ellisabeta/m324-flughafen-service:<tag>
ghcr.io/ellisabeta/m324-flug-service:<tag>
```

Der Name besteht aus dem Modul (`m324`) und dem Ordnernamen des Services. GHCR verlangt Kleinbuchstaben.

| Tag | Wann | Beispiel | Zweck |
|---|---|---|---|
| `sha-<commit>` | bei jedem Publish | `sha-3f2a1c9` | eindeutig, führt direkt zum Commit |
| Branchname | Push auf `main` oder manueller Start | `main` | aktueller Stand eines Branches |
| SemVer | Git-Tag `v1.0.0` | `1.0.0`, `1.0` | Release-Version für P4 |
| `latest` | nur bei einem Release-Tag | `latest` | neuestes Release |

Ein Release entsteht so:

```bash
git tag v1.0.0
git push upstream v1.0.0
```

Die Pipeline baut, testet und publiziert dann die Tags `1.0.0`, `1.0`, `latest` und `sha-…`. Ein Branch-Tag wie `main` wird bei jedem Push überschrieben. Eine Version wie `1.0.0` setzen wir nur einmal, sie zeigt also immer auf dasselbe Image. Für ein Deployment verwenden wir darum immer eine Version oder einen `sha-`-Tag.

Die Version in der `pom.xml` (`0.0.1-SNAPSHOT`) haben wir nicht angepasst. Massgebend für das Image ist der Git-Tag.

## 3. Registry einrichten

GHCR muss nicht separat eingerichtet werden. Beim ersten Publish legt GitHub die Packages automatisch an. Über das Label `org.opencontainers.image.source` werden sie mit unserem Repository verknüpft und erscheinen dort rechts unter „Packages“.

Neue Packages sind zuerst privat. Damit die Lehrperson die Images ohne Login ziehen kann, stellt die Owner-Person (`ellisabeta`) sie auf öffentlich: Package öffnen → „Package settings“ → „Change visibility“ → „Public“.

## 4. Erweiterung der CI-Pipeline

Die Jobs aus P3 bleiben unverändert. In `ci.yml` kommt der Job `publish` dazu:

```text
build → unit-test → integration-test → system-test → publish
```

- `publish` hat `needs: system-test`. Er startet nur, wenn Build und alle Tests grün sind.
- Er läuft nur bei Push auf `main`, bei Release-Tags `v*` und beim manuellen Start. Bei Feature-Branches und Pull Requests wird nichts publiziert. So landet nur geprüfter Code in der Registry.
- Er lädt das JAR aus dem Job `build`. Im Image steckt also genau das JAR, das vorher getestet wurde.
- Er läuft per Matrix für beide Services.
- Am Ende schreibt er Image-Name, Tags und Digest ins Log und in die Zusammenfassung des Runs.

## 5. Zugriff, Rechte und Secrets

- **Publizieren:** Die Pipeline verwendet den `GITHUB_TOKEN`. GitHub erstellt ihn für jeden Run automatisch, und er ist nach dem Run ungültig. Es liegt kein Token im Repository oder in den Repository-Secrets.
- **Rechte:** Nur der Job `publish` hat `packages: write`. Alle anderen Jobs dürfen das Repository nur lesen (`contents: read`).
- **Pull Requests:** Bei Pull Requests läuft `publish` nicht. Fremder Code kann also nichts in unsere Registry schreiben.
- **Images herunterladen:** Öffentliche Images kann jeder ohne Login ziehen. Bei einem privaten Image braucht man `docker login ghcr.io` mit einem persönlichen Token mit `read:packages`. Dieses Token gehört nur in den eigenen Rechner, nie ins Repository.
- **Datenbank-Zugangsdaten:** Die Atlas-Zugangsdaten sind nicht im Image. Sie werden erst beim Start als Umgebungsvariablen übergeben (`MONGODB_AIRPORT_URI`, `MONGODB_FLIGHT_URI`).

## 6. Nachweis

*(folgt nach dem ersten Publish: Link zum Run, publizierte Tags, Screenshot der Package-Seite und `docker pull`)*

## 7. Verwendung in P4

In P4 wird nicht mehr neu gebaut, sondern ein publiziertes Image mit einer festen Version gestartet. Zum Beispiel mit docker compose:

```yaml
services:
  flughafen-service:
    image: ghcr.io/ellisabeta/m324-flughafen-service:1.0.0
    ports:
      - "8081:8081"
    environment:
      MONGODB_AIRPORT_URI: ${MONGODB_AIRPORT_URI}

  flug-service:
    image: ghcr.io/ellisabeta/m324-flug-service:1.0.0
    ports:
      - "8082:8082"
    environment:
      MONGODB_FLIGHT_URI: ${MONGODB_FLIGHT_URI}
      AIRPORT_SERVICE_URL: http://flughafen-service:8081
```

```bash
docker compose pull
docker compose up -d
```

- Für ein Update ändert man nur die Version, z. B. auf `1.1.0`.
- Für einen Rollback setzt man die alte Version wieder ein.
- Getestet wird damit dasselbe Image, das später auch deployt wird.

Wohin wir in P4 deployen, ist noch offen. Der Ablauf bleibt aber gleich: Image mit Version aus GHCR ziehen und starten.

## 8. KI-Nachweis

**Prompt:** Wir haben Claude Code den P3b-Auftrag gegeben und gefragt, wie wir ihn am besten lösen.

**KI-Hinweis:**

- GHCR statt Docker Hub, weil kein eigenes Secret nötig ist.
- Kein `latest` bei jedem Push, sondern nur bei Releases.
- Zusätzlich ein `sha-`-Tag, damit jedes Image zu einem Commit zurückverfolgt werden kann.
- `packages: write` nur im Publish-Job, und bei Pull Requests nicht publizieren.

**Geprüfte Stelle:** die Bedingung des Publish-Jobs in `ci.yml`:

```yaml
needs: system-test
if: github.ref == 'refs/heads/main' || startsWith(github.ref, 'refs/tags/v') || github.event_name == 'workflow_dispatch'
```

**Resultat:** *(folgt nach dem ersten Lauf auf einem Feature-Branch und auf `main`)*

**Schlussfolgerung:** *(folgt mit dem Resultat)*
