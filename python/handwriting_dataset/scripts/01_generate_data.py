# handwriting_dataset/scripts/generate_data.py

import os
import random
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont, ImageOps
from tqdm import tqdm
import json

# Konfiguration
FIELD_NAME = "vorname"
FIELD_SIZE = (852, 91)  # Originale Größe vor Padding
FINAL_SIZE = (384, 384)  # Zielgröße für Modell
NUM_IMAGES = 20000
BASE_DIR = Path(__file__).resolve().parent.parent

BASE_OUTPUT_DIR = Path("D:/dataset_output_vornamen")
OUTPUT_DIR = BASE_OUTPUT_DIR / "images"
JSONL_PATH = BASE_OUTPUT_DIR / "dataset.jsonl"

FONT_DIR = BASE_DIR.parent / "fonts"
DICT_PATH = BASE_DIR.parent / "dictionaries/vornamen.json"

# Setup
os.makedirs(OUTPUT_DIR, exist_ok=True)
os.makedirs(JSONL_PATH.parent, exist_ok=True)

# prepare_image Funktion
def prepare_image(image: Image.Image, size=(384, 384)) -> Image.Image:
    return ImageOps.pad(image, size, color="white", centering=(0.5, 0.5), method=Image.BICUBIC)

# Datenquelle: Vornamen laden
with open(DICT_PATH, "r", encoding="utf-8") as f:
    name_list = json.load(f)

# Fonts laden
fonts = list(FONT_DIR.glob("*.ttf"))
if not fonts:
    raise RuntimeError(f"Keine .ttf-Dateien im Font-Verzeichnis gefunden: {FONT_DIR}")

# Bild- und JSONL-Erzeugung
with open(JSONL_PATH, "w", encoding="utf-8") as jsonl_file:
    for i in tqdm(range(NUM_IMAGES), desc="Generiere Handschriftbilder"):
        name = random.choice(name_list)
        font_path = random.choice(fonts)
        font_size = random.randint(40, 55)

        # Ursprüngliches, nicht gepaddetes Bild
        image = Image.new("RGB", FIELD_SIZE, color="white")
        draw = ImageDraw.Draw(image)
        font = ImageFont.truetype(str(font_path), font_size)

        # Text mittig platzieren
        bbox = draw.textbbox((0, 0), name, font=font)
        text_width = bbox[2] - bbox[0]
        text_height = bbox[3] - bbox[1]
        x = (FIELD_SIZE[0] - text_width) // 2
        y = (FIELD_SIZE[1] - text_height) // 2
        draw.text((x, y), name, font=font, fill="black")

        # Bild vorbereiten (Padding auf 384x384)
        padded_image = prepare_image(image, size=FINAL_SIZE)

        # Speichern
        image_filename = f"{FIELD_NAME}_{i:04}.png"
        image_path = OUTPUT_DIR / image_filename
        padded_image.save(image_path)

        # JSONL-Eintrag
        jsonl_file.write(json.dumps({
            "image": f"images/{image_filename}",
            "text": f"FELD:{FIELD_NAME} TEXT:{name}"
        }, ensure_ascii=False) + "\n")

print(f"\n {NUM_IMAGES} Bilder wurden mit Padding in '{OUTPUT_DIR}' gespeichert.")
print(f" JSONL-Datei: {JSONL_PATH}")
