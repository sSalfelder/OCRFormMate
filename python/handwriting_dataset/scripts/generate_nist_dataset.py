import os
import json
from pathlib import Path
from PIL import Image, ImageOps
from tqdm import tqdm
from handwriting_dataset.mappings.folder_to_label import FOLDER_TO_LABEL

# === Nur eine Klasse
LABEL = "hH"
SOURCE_DIR = Path("D:/dataset") / LABEL
BASE_OUTPUT_DIR = Path("D:/dataset_output_nist") / LABEL
OUTPUT_DIR = BASE_OUTPUT_DIR / "images"
JSONL_PATH = BASE_OUTPUT_DIR / "dataset.jsonl"
FINAL_SIZE = (384, 384)

os.makedirs(OUTPUT_DIR, exist_ok=True)
os.makedirs(JSONL_PATH.parent, exist_ok=True)

def prepare_image(image: Image.Image, size=(384, 384)) -> Image.Image:
    return ImageOps.pad(image, size, color="white", centering=(0.5, 0.5), method=Image.BICUBIC)

image_files = sorted(SOURCE_DIR.glob("*.png"))
print(f"Klasse {LABEL}: {len(image_files)} Bilder gefunden")

with open(JSONL_PATH, "w", encoding="utf-8") as jsonl_file:
    for i, image_path in enumerate(tqdm(image_files, desc=f"Verarbeite Klasse {LABEL}")):
        try:
            image = Image.open(image_path).convert("RGB")
            padded = prepare_image(image, size=FINAL_SIZE)

            out_filename = f"{LABEL}_{i:04}.png"
            out_path = OUTPUT_DIR / out_filename
            padded.save(out_path)

            feld_typ = "nummer" if LABEL.isdigit() else "zeichen"
            if len(LABEL) > 1:
                true_letter = FOLDER_TO_LABEL[LABEL]
            else:
                true_letter = LABEL
            jsonl_file.write(json.dumps({
                "image": f"images/{out_filename}",
                "text": f"FELD:{feld_typ} TEXT:{true_letter}"
            }, ensure_ascii=False) + "\n")

        except Exception as e:
            print(f"Fehler bei Bild {image_path}: {e}")

print(f"\n Fertig. {len(image_files)} Bilder gespeichert in '{OUTPUT_DIR}'")
print(f" JSONL-Datei: {JSONL_PATH}")
