#!/bin/bash

echo "Starte Einrichtung für TrOCR (Handschnrift-OCR)..."

# 1. Prüfe Python-Version
PYTHON_VERSION=$(python --version 2>&1)
echo "Gefundene Python-Version: $PYTHON_VERSION"

# 2. Neues virtuelles Environment anlegen
echo "Erstelle virtuelle Umgebung 'venv'..."
python -m venv venv

# 3. Aktivieren (plattformabhängig)
if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "win32" || "$OSTYPE" == "cygwin" ]]; then
    ACTIVATE_SCRIPT="./venv/Scripts/activate"
else
    ACTIVATE_SCRIPT="./venv/bin/activate"
fi

if [[ -f "$ACTIVATE_SCRIPT" ]]; then
    source "$ACTIVATE_SCRIPT"
    echo "Virtuelle Umgebung aktiviert."
else
    echo "Fehler: Aktivierungsskript nicht gefunden!"
    exit 1
fi

# 4. Pakete installieren
echo "📥 Installiere TrOCR-Abhängigkeiten..."
pip install --upgrade pip
pip install torch==2.1.2
pip install transformers==4.39.3
pip install flask==3.0.2
pip install pillow==10.2.0
pip install numpy==1.26.4

echo "Fertig! TrOCR-Umgebung ist einsatzbereit."
echo "Starte den Server mit: python trocr_handwriting_api.py"
