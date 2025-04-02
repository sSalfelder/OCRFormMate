# handwriting_dataset/scripts/04_train.py

from pathlib import Path
from datasets import load_from_disk
from transformers import VisionEncoderDecoderModel, Seq2SeqTrainer, Seq2SeqTrainingArguments, TrOCRProcessor
import torch

# === Pfade ===
BASE_DIR = Path(__file__).resolve().parent.parent
MODEL_DIR = BASE_DIR / "models/fhswf_trocr"
DATASET_DIR = BASE_DIR / "processed"
OUTPUT_DIR = BASE_DIR / "output"

# === Gerät wählen (CPU-Fallback)
device = "cuda" if torch.cuda.is_available() else "cpu"
print(f"🚀 Verwende Gerät: {device}")

# === Lade Modell & Processor
model = VisionEncoderDecoderModel.from_pretrained(MODEL_DIR.as_posix(), local_files_only=True)
processor = TrOCRProcessor.from_pretrained(MODEL_DIR.as_posix(), local_files_only=True)

model.to(device)

# === Lade vorbereiteten Datensatz
dataset = load_from_disk(str(DATASET_DIR))

# === Daten-Kollator (vereinfacht)
def collate_fn(batch):
    pixel_values = torch.stack([example["pixel_values"] for example in batch])
    labels = torch.nn.utils.rnn.pad_sequence([torch.tensor(example["labels"]) for example in batch],
                                             batch_first=True, padding_value=processor.tokenizer.pad_token_id)
    return {
        "pixel_values": pixel_values,
        "labels": labels
    }

# === Trainingsparameter
training_args = Seq2SeqTrainingArguments(
    output_dir=str(OUTPUT_DIR),
    per_device_train_batch_size=2,
    predict_with_generate=True,
    num_train_epochs=3,
    logging_dir=str(OUTPUT_DIR / "logs"),
    logging_steps=10,
    save_steps=50,
    save_total_limit=2,
    evaluation_strategy="no",
    report_to="none"
)

# === Trainer vorbereiten
trainer = Seq2SeqTrainer(
    model=model,
    args=training_args,
    train_dataset=dataset,
    tokenizer=processor,
    data_collator=collate_fn
)

# === Training starten
trainer.train()

# === Modell speichern
model.save_pretrained(OUTPUT_DIR / "model")
processor.save_pretrained(OUTPUT_DIR / "processor")

print("\n✅ Training abgeschlossen. Modell gespeichert in:", OUTPUT_DIR / "model")
