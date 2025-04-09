import json
import random
import shutil
from pathlib import Path

# === Eingaben ===
jsonl_paths = [
    Path("D:/dataset_output_vornamen/dataset.jsonl"),
    Path("D:/dataset_output_nist/0/dataset.jsonl"),
    Path("D:/dataset_output_nist/1/dataset.jsonl"),
    # Weitere Datasets hier ergänzen ...
]

LIMIT_PER_FILE = 5000  # Max. Einträge pro JSONL-Datei (None = alle)

# === Zielordner (für JSONL und Bilder)
output_base = Path("D:/huggingface_datasets/handwriting_all")
output_images = output_base / "images"
output_jsonl = output_base / "dataset.jsonl"

# === Setup
output_base.mkdir(parents=True, exist_ok=True)
output_images.mkdir(parents=True, exist_ok=True)

# === Sammlung
all_entries = []
used_names = set()

for path in jsonl_paths:
    source_dir = path.parent / "images"

    with open(path, "r", encoding="utf-8") as f:
        lines = f.readlines()
        random.shuffle(lines)  # zufällige Auswahl
        if LIMIT_PER_FILE:
            lines = lines[:LIMIT_PER_FILE]

        for line in lines:
            entry = json.loads(line)
            image_name = Path(entry["image"]).name
            src_image_path = source_dir / image_name

            # Sicherer, eindeutiger neuer Bildname
            base_stem = Path(image_name).stem
            new_name = f"{base_stem}_{random.randint(1000, 9999)}.png"
            while new_name in used_names:
                new_name = f"{base_stem}_{random.randint(1000, 9999)}.png"
            used_names.add(new_name)

            dest_image_path = output_images / new_name
            try:
                shutil.copy2(src_image_path, dest_image_path)
            except Exception as e:
                print(f"Fehler beim Kopieren von {src_image_path}: {e}")
                continue

            entry["image"] = f"images/{new_name}"
            all_entries.append(entry)

print(f"\nInsgesamt {len(all_entries)} Einträge geladen & Bilder kopiert")

# === Mischen (gesamt)
random.shuffle(all_entries)

# === Schreiben
with open(output_jsonl, "w", encoding="utf-8") as f_out:
    for entry in all_entries:
        f_out.write(json.dumps(entry, ensure_ascii=False) + "\n")

print(f"Kombinierter Datensatz gespeichert unter: {output_jsonl}")
print(f"Bilder liegen unter: {output_images}")
