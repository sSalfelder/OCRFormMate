from datasets import load_dataset, Features, Value, Image
from pathlib import Path
import json

base_dir = Path("D:/dataset_output_nist/0")
jsonl_path = base_dir / "dataset.jsonl"
image_dir = base_dir / "images"
fixed_jsonl = base_dir / "dataset_temp.jsonl"

# === JSON korrigieren (absoluter Bildpfad)
with open(jsonl_path, "r", encoding="utf-8") as f_in, open(fixed_jsonl, "w", encoding="utf-8") as f_out:
    for line in f_in:
        entry = json.loads(line)
        image_path = image_dir / Path(entry["image"]).name
        entry["image"] = str(image_path.resolve())
        f_out.write(json.dumps(entry, ensure_ascii=False) + "\n")

# === Datensatz definieren
features = Features({
    "image": Image(),
    "text": Value("string")
})

# === Laden & Push
dataset = load_dataset(
    "json",
    data_files=str(fixed_jsonl),
    split="train",
    features=features
)

dataset.push_to_hub("sSalfelder/handwriting-dataset-number0", private=True)

print("Upload abgeschlossen.")
