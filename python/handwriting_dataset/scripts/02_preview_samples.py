# 02_preview_samples.py

import json
from pathlib import Path
from PIL import Image
import matplotlib
matplotlib.use("TkAgg")
import matplotlib.pyplot as plt
import tkinter
print("✅ tkinter funktioniert")


# Pfad zur JSONL-Datei
JSONL_PATH = Path(__file__).resolve().parent.parent / "data/dataset.jsonl"

# Anzahl Bilder anzeigen
N = 9

with open(JSONL_PATH, "r", encoding="utf-8") as f:
    lines = [json.loads(line) for line in f.readlines()[:N]]

for i, item in enumerate(lines):
    image_path = item["image"]
    text = item["text"]

    img = Image.open(image_path)

    plt.figure(figsize=(6, 2))
    plt.imshow(img, cmap="gray")
    plt.title(f"{i+1}. {text}")
    plt.axis("off")

plt.show()
