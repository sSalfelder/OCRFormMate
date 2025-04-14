import json
import random
import shutil
from pathlib import Path
from PIL import Image, ImageOps
from tqdm import tqdm

BASE_DATASET_DIRS = [
    Path("D:/dataset_augmented_chars"),
    Path("D:/dataset"),
]

AUGMENTED_FOLDER_NAME = "dataset_augmented_chars"
LIMIT_PER_FILE = 2000  # Max. Einträge pro JSONL-Datei

output_base = Path("D:/huggingface_datasets/handwriting_single_chunk1")
output_images = output_base / "images"
output_jsonl = output_base / "dataset.jsonl"

output_base.mkdir(parents=True, exist_ok=True)
output_images.mkdir(parents=True, exist_ok=True)

# Kontrast-Normalisierung
def normalize_contrast(image: Image.Image) -> Image.Image:
    gray = image.convert("L")
    mean_pixel = sum(gray.getdata()) / (gray.width * gray.height)
    if mean_pixel < 127:
        gray = ImageOps.invert(gray)
    return gray.convert("RGB")

# JSONL-Dateien automatisch finden
jsonl_paths = []
for base_dir in BASE_DATASET_DIRS:
    jsonl_paths.extend(base_dir.rglob("dataset.jsonl"))

print(f"{len(jsonl_paths)} JSONL-Dateien gefunden.")

all_entries = []
used_names = set()

# Datensatzverarbeitung mit Fortschrittsbalken
for path in tqdm(jsonl_paths, desc="Datensätze", unit="dataset"):
    source_dir = path.parent / "images"
    is_augmented = AUGMENTED_FOLDER_NAME in str(path.resolve())

    with open(path, "r", encoding="utf-8") as f:
        lines = f.readlines()
        random.shuffle(lines)
        if LIMIT_PER_FILE:
            lines = lines[:LIMIT_PER_FILE]

        for line in tqdm(lines, desc=f"Bilder aus {path.parent.name}", leave=False, unit="bild"):
            entry = json.loads(line)
            relative_path = Path(entry["image"]).relative_to("images")
            src_image_path = source_dir / relative_path

            base_stem = Path(src_image_path).stem
            new_name = f"{base_stem}_{random.randint(1000, 9999)}.png"
            while new_name in used_names:
                new_name = f"{base_stem}_{random.randint(1000, 9999)}.png"
            used_names.add(new_name)

            dest_image_path = output_images / new_name

            try:
                if is_augmented:
                    shutil.copy2(src_image_path, dest_image_path)
                else:
                    with Image.open(src_image_path) as img:
                        norm_img = normalize_contrast(img)
                        norm_img.save(dest_image_path)
            except Exception as e:
                print(f"Fehler bei {src_image_path}: {e}")
                continue

            entry["image"] = f"images/{new_name}"
            all_entries.append(entry)

print(f"\nInsgesamt {len(all_entries)} Einträge geladen & Bilder verarbeitet.")

# Zufälliges Mischen
random.shuffle(all_entries)

with tqdm(total=len(all_entries), desc="Schreibe JSONL", unit="eintrag") as pbar:
    with open(output_jsonl, "w", encoding="utf-8") as f_out:
        for entry in all_entries:
            f_out.write(json.dumps(entry, ensure_ascii=False) + "\n")
            pbar.update(1)

print(f"Kombinierter Datensatz gespeichert unter: {output_jsonl}")
print(f"Bilder liegen unter: {output_images}")
