#!/bin/bash

# XData Backend Start-Skript für MacOS

# 1. Voraussetzungen prüfen
if ! command -v java &> /dev/null; then
    echo "Fehler: Java ist nicht installiert."
    exit 1
fi

# 2. Z3 Pfad (Optional, aber empfohlen für SQL-Grading)
Z3_PATH=$(which z3)
if [ -z "$Z3_PATH" ]; then
    echo "Warnung: z3 wurde nicht im PATH gefunden. SQL-Grading könnte fehlschlagen."
    echo "Empfehlung: brew install z3"
else
    echo "Z3 gefunden unter: $Z3_PATH"
fi

# 3. Backend starten
JAR_FILE="target/xdata-backend-0.0.1-SNAPSHOT.jar"

if [ ! -f "$JAR_FILE" ]; then
    echo "Fehler: JAR-Datei nicht gefunden. Bitte zuerst bauen (z.B. mit IntelliJ oder Maven)."
    exit 1
fi

echo "Starte XData Backend..."
java -jar "$JAR_FILE"
