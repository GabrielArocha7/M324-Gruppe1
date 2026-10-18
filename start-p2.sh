#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

if [[ ! -f "$ROOT_DIR/.env" ]]; then
  echo "Fehler: Keine .env-Datei im Projektroot gefunden."
  exit 1
fi

set -a
source "$ROOT_DIR/.env"
set +a

if [[ "$(uname -m)" == "arm64" ]]; then
  export JAVA_HOME="$(/usr/libexec/java_home -v 21 -a arm64)"
else
  export JAVA_HOME="$(/usr/libexec/java_home -v 21)"
fi
export PATH="$JAVA_HOME/bin:$PATH"

echo "Java: $(java -version 2>&1 | head -n 1)"
echo "Starte Flughafenservice auf Port 8081..."
mvn -f "$ROOT_DIR/Code/P2/flughafen-service/pom.xml" spring-boot:run &
AIRPORT_PID=$!

echo "Starte Flugservice auf Port 8082..."
mvn -f "$ROOT_DIR/Code/P2/flug-service/pom.xml" spring-boot:run &
FLIGHT_PID=$!

cleanup() {
  echo "Stoppe P2-Services..."
  kill "$AIRPORT_PID" "$FLIGHT_PID" 2>/dev/null || true
}

trap cleanup INT TERM EXIT

echo "Beide Services laufen. Mit Ctrl+C beenden."
wait "$AIRPORT_PID" "$FLIGHT_PID"

curl -i -X POST http://localhost:8081/api/airports \
  -H "Content-Type: application/json" \
  -d '{"name":"Zurich Airport","code":"ZRH","capacity":30000}'


curl -i -X POST http://localhost:8082/api/flights \
  -H "Content-Type: application/json" \
  -d '{
    "departureAirportCode":"ZRH",
    "arrivalAirportCode":"JFK",
    "departureAt":"2026-10-01T08:00:00Z",
    "arrivalAt":"2026-10-01T16:00:00Z",
    "aircraftType":"Boeing 727"
  }'