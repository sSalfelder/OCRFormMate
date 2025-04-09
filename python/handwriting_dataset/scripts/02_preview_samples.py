import json
from pathlib import Path
from PIL import Image
import matplotlib
matplotlib.use("TkAgg")
import matplotlib.pyplot as plt
import tkinter
print("tkinter funktioniert")

# Pfad zur JSONL-Datei
BASE_DIR = Path(__file__).resolve().parent.parent  # → z. B. handwriting_dataset/
JSONL_PATH = BASE_DIR / "data/dataset.jsonl"
IMAGE_DIR = BASE_DIR / "data"  # da liegt auch der Ordner "images/..."

# Anzahl Bilder anzeigen
N = 9

with open(JSONL_PATH, "r", encoding="utf-8") as f:
    lines = [json.loads(line) for line in f.readlines()[:N]]

for i, item in enumerate(lines):
    relative_image_path = item["image"]  # z. B. "images/vorname_0001.png"
    image_path = IMAGE_DIR / relative_image_path  # ergibt z. B. data/images/vorname_0001.png
    text = item["text"]

    img = Image.open(image_path)

    plt.figure(figsize=(6, 2))
    plt.imshow(img, cmap="gray")
    plt.title(f"{i+1}. {text}")
    plt.axis("off")

plt.show()

