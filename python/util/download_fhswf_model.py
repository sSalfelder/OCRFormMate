from transformers import TrOCRProcessor, VisionEncoderDecoderModel
import os

save_dir = "../models/fhswf_trocr"

os.makedirs(save_dir, exist_ok=True)

print("Lade Prozessor...")
processor = TrOCRProcessor.from_pretrained("fhswf/TrOCR_german_handwritten")
processor.save_pretrained(save_dir)

print("Lade Modell...")
model = VisionEncoderDecoderModel.from_pretrained("fhswf/TrOCR_german_handwritten")
model.save_pretrained(save_dir)

print("\n Inhalt des Modellverzeichnisses:")
for fname in os.listdir(save_dir):
    print(" -", fname)

expected_files = ["config.json", "pytorch_model.bin"]
missing = [f for f in expected_files if not os.path.exists(os.path.join(save_dir, f))]
if missing:
    print("\nFehlende Dateien:", missing)
    print("Der Download scheint unvollständig. Bitte prüfe deine Internetverbindung oder versuche es mit 'local_files_only=False'.")
else:
    print("\nAlle wichtigen Dateien vorhanden. Du kannst das Modell jetzt lokal verwenden.")
