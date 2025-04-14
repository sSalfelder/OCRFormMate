from datasets import load_dataset
from transformers import TrOCRProcessor
from pathlib import Path
from PIL import Image
import numpy as np
import matplotlib.pyplot as plt
from image_preprocessing import prepare_image

JSONL_PATH = Path(__file__).resolve().parent.parent / "data/dataset.jsonl"
MODEL_PATH = Path(__file__).resolve().parent.parent.parent / "models/fhswf_trocr"

# Prozessor
processor = TrOCRProcessor.from_pretrained(MODEL_PATH.as_posix(), local_files_only=True)
# Datensatz
dataset = load_dataset("json", data_files=str(JSONL_PATH), split="train")

# Preprocessing-Funktion
def preprocess(example):
    image = Image.open(example["image"]).convert("RGB")
    image = prepare_image(image)
    text = example["text"]
    # Bild vorbereiten
    pixel_values = processor.image_processor(image, return_tensors="pt").pixel_values[0]

    print("Originalgröße:", image.size)  # (852, 91)
    print("Nach Verarbeitung:", pixel_values.shape)

    labels = processor.tokenizer(text, padding="max_length", truncation=True, return_tensors="pt").input_ids[0]
    return {
            "pixel_values": pixel_values,
            "labels": labels
    }

# Dataset transformieren
processed_dataset = dataset.map(preprocess, remove_columns=dataset.column_names)

# Optional speichern
processed_dataset.save_to_disk("handwriting_dataset/processed")

# Beispiel aus Dataset
sample = processed_dataset[0]

# Liste → NumPy-Array → (C, H, W) → (H, W, C)
img_array = np.array(sample["pixel_values"]).transpose(1, 2, 0)
plt.imshow(img_array, cmap="gray")

# Bild anzeigen
plt.title("Trainingsbild")
plt.axis("off")
plt.show()

# Text decodieren
decoded = processor.tokenizer.decode(sample["labels"], skip_special_tokens=True)
print(f"Decodierter Text:\n{decoded}")
