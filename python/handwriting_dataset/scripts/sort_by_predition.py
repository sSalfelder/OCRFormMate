import os
from pathlib import Path
from PIL import Image
from tqdm import tqdm
import shutil
from transformers import TrOCRProcessor, VisionEncoderDecoderModel
import torch

# === 🔧 Konfigurierbare Parameter ===
TARGET_LETTER = "I"  # <-- das ist die erkannte Prediction, z. B. "O", "B", "I"
SOURCE_DIR = Path(f"D:/dataset/{TARGET_LETTER.lower()}")  # z. B. "d:/dataset/o"
TARGET_DIR = Path(f"D:/dataset/{TARGET_LETTER.lower()}{TARGET_LETTER}")  # z. B. "d:/dataset/oO"

# Lokaler Modellpfad
LOCAL_MODEL_PATH = "../../models/fhswf_trocr"

# Modell und Prozessor laden (nur lokal)
processor = TrOCRProcessor.from_pretrained(LOCAL_MODEL_PATH, local_files_only=True)
model = VisionEncoderDecoderModel.from_pretrained(LOCAL_MODEL_PATH, local_files_only=True)
model.eval()

# === Zielordner vorbereiten
TARGET_DIR.mkdir(parents=True, exist_ok=True)

# === Alle Bilder durchgehen
image_paths = sorted(SOURCE_DIR.glob("*.png"))[:100]
print(f"{len(image_paths)} Bilder gefunden in '{SOURCE_DIR}' – Ziel: {TARGET_LETTER}")

for image_path in tqdm(image_paths, desc=f"Bilderkennung ({TARGET_LETTER})", unit="bild"):
    try:
        image = Image.open(image_path).convert("RGB")
        pixel_values = processor(images=image, return_tensors="pt").pixel_values

        with torch.no_grad():
            generated_ids = model.generate(pixel_values, max_length=4)
        prediction = processor.batch_decode(generated_ids, skip_special_tokens=True)[0].strip()

        if prediction == TARGET_LETTER:
            target_path = TARGET_DIR / image_path.name
            shutil.move(str(image_path), str(target_path))
            print(f"Verschoben: {image_path.name} → {TARGET_LETTER}")

    except Exception as e:
        print(f"Fehler bei Bild {image_path.name}: {e}")
