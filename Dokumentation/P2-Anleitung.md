# P2 – Anleitung für Teammitglieder

Diese Anleitung zeigt, wie man das P2-Projekt lokal einrichtet und den Flughafenservice startet.

## 1. Voraussetzungen

Benötigt werden:

- Git
- Java 17 oder höher
- Maven 3.9 oder höher
- ein MongoDB-Atlas-Zugang zum Projekt
- eine in MongoDB Atlas freigegebene IP-Adresse

Versionen prüfen:

```bash
java -version
mvn -version
```

Auf einem Apple-Silicon-Mac muss ein passendes ARM-JDK verwendet werden:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17 -a arm64)
export PATH="$JAVA_HOME/bin:$PATH"
```

Danach nochmals prüfen:

```bash
java -version
mvn -version
```

## 2. Projekt holen

Repository klonen und in den Projektordner wechseln:

```bash
git clone https://github.com/ellisabeta/M324-Gruppe1.git
cd M324-Gruppe1
```

Für Änderungen einen eigenen Branch erstellen:

```bash
git switch -c feature/P2-deine-aufgabe
```

Direkte Commits auf `main` sollen vermieden werden.

## 3. MongoDB Atlas vorbereiten

Im Atlas-Projekt müssen diese zwei Datenbankbenutzer vorhanden sein:

```text
flughafen_db
flug_db
```

Die Benutzer verwenden jeweils ihre eigene Datenbank:

```text
flughafen_db → flughafen_db
flug_db      → flug_db
```

Beide Benutzer brauchen mindestens die Rolle `readWrite` für ihre Datenbank.

Zusätzlich muss unter **Security → Network Access** die aktuelle öffentliche IP-Adresse eingetragen werden. Dafür kann **Add My Current IP Address** verwendet werden. Die IP-Adresse kann sich später ändern und muss dann erneut eingetragen werden.

## 4. `.env` erstellen

Die `.env` liegt im Hauptordner des Repositories und wird nicht zu GitHub hochgeladen:

```text
M324-Gruppe1/
├── .env
└── Code/
    └── P2/
```

Inhalt der `.env`:

```env
  
AIRPORT_SERVICE_URL='http://localhost:8081'
```

Die echten Connection Strings werden in MongoDB Atlas unter **Connect → Drivers → Java** kopiert. `PASSWORT` muss ersetzt werden. Wenn ein Passwort Sonderzeichen wie `@` enthält, muss es URL-codiert werden, zum Beispiel `@` als `%40`.

Die vollständigen Connection Strings dürfen nicht in GitHub, Chats oder Screenshots veröffentlicht werden.

## 5. Variablen laden

Im Hauptordner des Repositories ausführen:

```bash
set -a
source .env
set +a
```

Dieser Schritt muss nach jeder Änderung an der `.env` und vor dem Start der Anwendung ausgeführt werden.

## 6. Flughafenservice starten

```bash
cd Code/P2/flughafen-service
mvn spring-boot:run
```

Der Service läuft danach auf Port `8081`.

## 7. Flughafen erstellen

In einem zweiten Terminal ausführen:

```bash
curl -i -X POST http://localhost:8081/api/airports \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Flughafen Zürich",
    "code": "ZRH",
    "capacity": 30000
  }'
```

Bei Erfolg kommt `201 Created`. Die ID und das Erstellungsdatum werden automatisch erzeugt.

## 8. Alle Flughäfen abrufen

```bash
curl -i http://localhost:8081/api/airports
```

Mögliche Antworten:

- `200 OK`: Flughäfen wurden gefunden
- `404 Not Found`: Die Datenbank ist leer
- `500 Internal Server Error`: MongoDB- oder Verbindungsproblem prüfen

## 9. Unit Tests ausführen

Die Unit Tests benötigen keine Atlas-Verbindung:

```bash
cd Code/P2/flughafen-service
mvn test
```

## 10. Häufige Fehler

### Java funktioniert nicht

Prüfen, ob ein passendes JDK installiert ist und `JAVA_HOME` auf dieses JDK zeigt.

### `500 Internal Server Error`

- `.env` wurde nicht mit `source .env` geladen
- die IP-Adresse ist in Atlas nicht freigegeben
- Benutzername oder Passwort ist falsch
- der Connection String enthält einen Fehler
- Sonderzeichen im Passwort wurden nicht URL-codiert

### `404 Not Found` beim Abrufen

Wenn die Nachricht lautet, dass keine Flughäfen vorhanden sind, funktioniert die Verbindung. Die Datenbank ist einfach noch leer.

## 11. Git-Prozess

Nach der Arbeit:

1. Änderungen testen
2. Änderungen committen
3. Feature-Branch pushen
4. Pull Request auf `main` erstellen
5. Code Review abwarten
6. Erst nach erfolgreicher CI in `main` mergen
