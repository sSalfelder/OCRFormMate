import csv
import json

firstnames = set()

with open("../csvs/vornamen.csv", encoding="utf-8") as f:
    reader = csv.DictReader(f, delimiter=";")
    for row in reader:
        firstname = row["vorname"].strip()
        if firstname:
            firstnames.add(firstname)

with open("../dictionaries/vornamen.json", "w", encoding="utf-8") as f:
    json.dump(sorted(firstnames), f, indent=2, ensure_ascii=False)
