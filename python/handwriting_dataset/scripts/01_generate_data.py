# handwriting_dataset/scripts/generate_data.py

import os
import random
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont
from tqdm import tqdm
import json

# === Konfiguration ===
FIELD_NAME = "vorname"
FIELD_SIZE = (852, 91)  # Breite × Höhe in Pixel (aus FormTemplate)
NUM_IMAGES = 100
BASE_DIR = Path(__file__).resolve().parent.parent  # -> handwriting_dataset/

FONT_DIR = BASE_DIR.parent / "fonts"
DICT_PATH = BASE_DIR.parent / "dictionaries/vornamen.json"
OUTPUT_DIR = BASE_DIR / "data/images"
JSONL_PATH = BASE_DIR / "data/dataset.jsonl"

# === Setup ===
os.makedirs(OUTPUT_DIR, exist_ok=True)
os.makedirs(JSONL_PATH.parent, exist_ok=True)

# === Datenquelle: Echte Vornamen laden ===
with open(DICT_PATH, "r", encoding="utf-8") as f:
    name_list = json.load(f)

# === Fonts laden ===
fonts = list(FONT_DIR.glob("*.ttf"))
if not fonts:
    raise RuntimeError(f"⚠️ Keine .ttf-Dateien im Font-Verzeichnis gefunden: {FONT_DIR}")

# === Bild- und JSONL-Erzeugung ===
with open(JSONL_PATH, "w", encoding="utf-8") as jsonl_file:
    for i in tqdm(range(NUM_IMAGES), desc="📄 Generiere Handschriftbilder"):
        name = random.choice(name_list)
        font_path = random.choice(fonts)
        font_size = random.randint(40, 55)

        image = Image.new("RGB", FIELD_SIZE, color="white")
        draw = ImageDraw.Draw(image)
        font = ImageFont.truetype(str(font_path), font_size)

        # Textbreite/-höhe bestimmen
        bbox = draw.textbbox((0, 0), name, font=font)
        text_width = bbox[2] - bbox[0]
        text_height = bbox[3] - bbox[1]

        # Text mittig platzieren
        x = (FIELD_SIZE[0] - text_width) // 2
        y = (FIELD_SIZE[1] - text_height) // 2
        draw.text((x, y), name, font=font, fill="black")

        image_filename = f"{FIELD_NAME}_{i:04}.png"
        image_path = OUTPUT_DIR / image_filename
        image.save(image_path)

        jsonl_file.write(json.dumps({
            "image": str(image_path),
            "text": f"FELD:{FIELD_NAME} TEXT:{name}"
        }, ensure_ascii=False) + "\n")

print(f"\n✅ {NUM_IMAGES} Bilder wurden in '{OUTPUT_DIR}' gespeichert.")
print(f"📄 JSONL-Datei: {JSONL_PATH}")
