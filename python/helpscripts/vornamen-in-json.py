import csv
import json

firstnames = set()

with open("../csvs/vornamen.csv", encoding="utf-8") as f:
    reader = csv.DictReader(f, delimiter=";")  # Semikolon als Trennzeichen
    for row in reader:
        firstname = row["vorname"].strip()
        if firstname:
            firstnames.add(firstname)

# Optional: speichern
with open("../dictionaries/Buergergeld.json", "r+", encoding="utf-8") as f:
    data = json.load(f)
    data["vorname"] = sorted(firstnames)
    f.seek(0)
    json.dump(data, f, indent=2, ensure_ascii=False)
    f.truncate()

