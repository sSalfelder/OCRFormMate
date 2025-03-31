import csv
import json

postleitzahlen = set()

with open("csvs/postleitzahlen.csv", encoding="utf-8") as f:
    reader = csv.DictReader(f, delimiter=";")  # Semikolon als Trennzeichen
    for row in reader:
        plz = row["PLZ"].strip()
        if plz:
            postleitzahlen.add(plz)

# Optional: speichern
with open("dictionaries/Buergergeld.json", "r+", encoding="utf-8") as f:
    data = json.load(f)
    data["postleitzahl"] = sorted(postleitzahlen)
    f.seek(0)
    json.dump(data, f, indent=2, ensure_ascii=False)
    f.truncate()

