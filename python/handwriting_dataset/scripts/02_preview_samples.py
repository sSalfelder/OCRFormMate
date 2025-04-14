import json
from pathlib import Path
from PIL import Image
import matplotlib
matplotlib.use("TkAgg")
import matplotlib.pyplot as plt
import tkinter

BASE_DIR = Path(__file__).resolve().parent.parent
JSONL_PATH = BASE_DIR / "data/dataset.jsonl"
IMAGE_DIR = BASE_DIR / "data"

# Anzahl der Previews
N = 9

with open(JSONL_PATH, "r", encoding="utf-8") as f:
    lines = [json.loads(line) for line in f.readlines()[:N]]

for i, item in enumerate(lines):
    relative_image_path = item["image"]
    image_path = IMAGE_DIR / relative_image_path
    text = item["text"]

    img = Image.open(image_path)

    plt.figure(figsize=(6, 2))
    plt.imshow(img, cmap="gray")
    plt.title(f"{i+1}. {text}")
    plt.axis("off")

plt.show()

