import os
import pandas as pd
import numpy as np
from PIL import Image
from tqdm import tqdm
import re

def clean_label_for_folder(label):
    return re.sub(r'[<>:"/\\|?*]', '_', label)  # _ als Ordnername für blockierte Sonderzeichen

# CSV einlesen
df = pd.read_csv('train-gocr.csv', low_memory=False)

# Spaltennamen prüfen
print("Spaltennamen:", df.columns[:10])

LABEL_COLUMN = df.columns[0]
PIXEL_COLUMNS = df.columns[1:]            # Restlichen Spalten für das Bild

# Versuche automatisch Bildgröße zu erraten
pixel_count = len(PIXEL_COLUMNS)
img_size = int(np.sqrt(pixel_count))
if img_size * img_size != pixel_count:
    raise ValueError(f"Bildgröße unklar – {pixel_count} Pixel ergeben kein Quadrat.")

print(f"Bildgröße erkannt: {img_size}x{img_size}")

# Output-Ordner
output_root = 'D:/dataset_gocr3'
os.makedirs(output_root, exist_ok=True)

# Durchlaufen mit Fortschrittsanzeige
for idx, row in tqdm(df.iterrows(), total=len(df), desc="Speichere Bilder"):
    try:
        label = str(row[LABEL_COLUMN]).strip()

        # Zielordner bestimmen
        if label.isupper():
            folder_name = clean_label_for_folder(label.lower() + label)
        else:
            folder_name = clean_label_for_folder(label)

        folder_path = os.path.join(output_root, folder_name)
        os.makedirs(folder_path, exist_ok=True)

        # Bilddaten in ein numpy-Array umwandeln
        pixels = row[PIXEL_COLUMNS].astype(float).to_numpy().reshape((img_size, img_size))
        img = Image.fromarray(np.uint8(pixels), mode='L')  # 'L' = 8-bit-Graustufen
        img_path = os.path.join(folder_path, f'image_{idx}.png')
        img.save(img_path)

    except Exception as e:
        print(f"Fehler bei Zeile {idx}: {e}")
