# OCRFormMate

OCRFormMate ist ein vollumfängliches OCR-Tool zur Erkennung und strukturierten Verarbeitung deutscher handschriftlicher Formulare. Es kombiniert moderne KI-Modelle (Transformers) mit einem Desktop-Frontend (JavaFX) und einem persistenzfähigen Backend (Spring Boot) zur Speicherung von OCR-Ergebnissen.

---

## Projektüberblick

- **OCR-Modul in Python** mit HuggingFace Transformers und TrOCR-Modell
- **JavaFX-Frontend** zur Bildauswahl, Felddefinition und Visualisierung der Ergebnisse
- **Spring Boot Backend** zur Speicherung und Verwaltung von Formularen in einer Datenbank
- **Modular aufgebaut**: Jede Komponente ist separat einsetzbar und wartbar

---

## Technologie-Stack

| Komponente    | Technologie                    |
| ------------- | ------------------------------ |
| OCR-Erkennung | Python, Flask, Transformers    |
| Frontend      | Java 17+, JavaFX               |
| Backend       | Java 17+, Spring Boot, JPA     |
| Persistenz    | H2 / PostgreSQL / beliebige DB |

---

## Setup (OCR-Modul)

### 1. Klonen & Umgebung vorbereiten

```bash
git clone https://github.com/sSalfelder/OCRFormMate.git
cd OCRFormMate
python -m venv venv
source venv/bin/activate  # oder venv\Scripts\activate.bat auf Windows
pip install -r requirements.txt
```

### 2. TrOCR-Modell herunterladen (einmalig)

```bash
# Hugging Face CLI
huggingface-cli download fhswf/TrOCR_german_handwritten --local-dir ./models/fhswf_trocr --local-dir-use-symlinks False
```

### 3. Flask-App starten

```bash
python main.py
```

Die API läuft dann standardmäßig auf `http://127.0.0.1:6000`

---

## Setup (JavaFX-Frontend)

### Anforderungen:

- Java 17+
- Maven
- JavaFX SDK

### Hauptklassen:

- `MainApp.java` – Einstiegspunkt
- `Controller.java` – GUI-Logik
- `FormImageView.java` – Bildanzeige
- `OCRResultDialog.java` – Ergebnisanzeige
- `FieldDefinition.java` – Datenmodell für Formularfelder

### Funktionen:

- Laden von Formularbildern
- Definition von OCR-Feldern per Mausklick
- Übergabe an Flask-OCR-Backend via HTTP
- Anzeige & Bearbeitung der Textergebnisse

---

## Setup (Spring Boot Backend)

### Anforderungen:

- Java 17+
- Spring Boot (Maven-basiert)
- MariaDB-Datenbank (bspw. über XAMPP)

### Hauptklassen:

- `Application.java` – Einstiegspunkt
- `FormDataController.java` – REST-Schnittstelle
- `FormDataRepository.java` – DB-Zugriff über JPA
- `FormData.java` – Entity-Klasse

### Datenbank einrichten

Die Anwendung erwartet eine laufende MariaDB-Instanz mit folgenden Eigenschaften:

- Host: `localhost`
- Port: `3311`
- Datenbankname: `ocr_formmate`
- Benutzer: `root`
- Passwort: *(leer)*

Stelle sicher, dass bspw. XAMPP gestartet wird, bevor du die Anwendung ausführst. Die MariaDB-Datenbank muss erreichbar sein, sonst kann sich Spring Boot nicht verbinden.
Falls du eine andere Konfiguration verwendest (z. B. anderen Port oder Passwort), musst du die Datei `src/main/resources/application.properties` entsprechend anpassen:

`spring.datasource.url=jdbc:mariadb://localhost:3311/ocr_formmate
spring.datasource.username=root
spring.datasource.password=`

### Funktionen:

- Speichern & Laden von erkannten Formularinhalten

---

## Beispielablauf

1. Nutzer startet die JavaFX-GUI
2. Ein Formularbild wird geladen & Felder definiert
3. Bildausschnitte werden an das OCR-Backend gesendet (Flask)
4. Ergebnisse werden in der GUI angezeigt und auf Wunsch an das Spring-Backend übergeben

---

## Lizenz

Dieses Projekt steht unter der **MIT-Lizenz** (siehe `LICENSE`-Datei).

Das OCR-Modell [`fhswf/TrOCR_german_handwritten`](https://huggingface.co/fhswf/TrOCR_german_handwritten) basiert auf [Microsoft TrOCR](https://github.com/microsoft/unilm/tree/master/trocr), welches ebenfalls unter MIT-Lizenz steht. Die Verantwortung für das angepasste Modell liegt bei der FH Südwestfalen.

---

## Hinweise

- Das Modell ist **nicht im Git-Repo enthalten**, muss aber lokal unter `./models/fhswf_trocr` abgelegt werden
- Eine Beispielbild-Datei und Beispielausgabe liegen im Ordner `examples/`
- Alle Komponenten sind unabhängig testbar (Backend/API, GUI, DB)

---

## Weiterentwicklung (TODOs)

- Webschnittstelle für Sachbearbeiter Datenbankzugriff
- Mobile-App als Inputgerät für Formulardaten
- Mehr Eingangsformate integrieren

---

## Hintergrund

Dieses Projekt entstand während einer Do-IT-Projektphase im Rahmen meiner Umschulung zum Fachinformatiker für Anwendungsentwicklung.

