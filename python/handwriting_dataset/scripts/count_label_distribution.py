import os
from pathlib import Path
from collections import defaultdict
from tqdm import tqdm

BASE_DATASET_DIRS = [
    Path("D:/dataset"),
    Path("D:/dataset_augmented_chars"),
]

label_counts = defaultdict(int)

# Durchsuche alle Unterordner
for base_dir in BASE_DATASET_DIRS:
    for subdir in sorted(base_dir.iterdir()):
        if subdir.is_dir():
            count = len(list(subdir.glob("*.png")))
            label_counts[subdir.name] += count

print(f"\Verteilung der verfügbaren Beispiele pro Label (Ordnername):\n")
for label, count in sorted(label_counts.items(), key=lambda x: -x[1]):
    print(f"{label:<15} {count:>5} Bilder")
