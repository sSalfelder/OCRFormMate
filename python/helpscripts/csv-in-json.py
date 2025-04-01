import csv
import json

cities = set()

import json

cities = set()

# Textdatei Zeile für Zeile lesen
with open("../csvs/orte.csv", encoding="utf-8") as f:
    for line in f:
        city = line.strip().strip('"')  # Entferne Zeilenumbruch und Anführungszeichen
        if city:
            cities.add(city)

# Wörterbuch aktualisieren
with open("../dictionaries/Buergergeld.json", "r+", encoding="utf-8") as f:
    data = json.load(f)
    data["wohnort"] = sorted(cities)
    f.seek(0)
    json.dump(data, f, indent=2, ensure_ascii=False)
    f.truncate()
