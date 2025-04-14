import numpy as np
from scipy.io import loadmat
from pathlib import Path
from PIL import Image
from tqdm import tqdm  # Fortschrittsbalken

MAT_PATH = r"D:\EMNIST\emnist-byclass.mat"
OUTPUT_DIR = Path(r"D:\dataset_EMNIST")

print("Lade .mat-Datei...")
mat = loadmat(MAT_PATH)

dataset = mat['dataset']
images = dataset['train'][0][0]['images'][0][0]
labels = dataset['train'][0][0]['labels'][0][0]
mapping = dataset['mapping'][0][0]

print(f"{len(images)} Bilder geladen.")

# Zielverzeichnis
OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

# Verarbeitungsschleife
for i in tqdm(range(len(images)), desc="Verarbeite Bilder"):
    label_idx = labels[i][0]
    unicode_val = mapping[label_idx][1]
    char = chr(unicode_val)

    # Groß-/Kleinschreibung prüfen
    if char.isalpha():
        if char.isupper():
            label_dir = OUTPUT_DIR / f"{char.lower()}_upper"
        else:
            label_dir = OUTPUT_DIR / char
    else:
        label_dir = OUTPUT_DIR / char

    label_dir.mkdir(parents=True, exist_ok=True)

    img_array = images[i].reshape(28, 28).T
    img = Image.fromarray((img_array * 255).astype(np.uint8), mode='L')
    img.save(label_dir / f"{i:06}.png")

print("Fertig. Alle Bilder wurden gespeichert und sortiert.")
