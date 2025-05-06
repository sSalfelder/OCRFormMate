import json
import random
import shutil
from pathlib import Path
from collections import defaultdict
from PIL import Image, ImageOps
from tqdm import tqdm
from concurrent.futures import ThreadPoolExecutor, as_completed

from handwriting_dataset.mappings.folder_to_label import FOLDER_TO_LABEL

BASE_DATASET_DIRS = [
    Path("E:/dataset"),
    Path("E:/dataset_augmented_chars"),
]

AUGMENTED_FOLDER_NAME = "dataset_augmented_chars"
OUTPUT_BASE = Path("E:/huggingface_datasets/handwriting_chunks")
EXAMPLES_PER_CLASS_PER_CHUNK = 100
MAX_CHUNKS = 25
FINAL_SIZE = (384, 384)

OUTPUT_BASE.mkdir(parents=True, exist_ok=True)
OUTPUT_IMAGES = OUTPUT_BASE / "images"
OUTPUT_IMAGES.mkdir(exist_ok=True)

def normalize_contrast(image: Image.Image) -> Image.Image:
    gray = image.convert("L")
    mean_pixel = sum(gray.getdata()) / (gray.width * gray.height)
    if mean_pixel < 127:
        gray = ImageOps.invert(gray)
    return gray.convert("RGB")

def prepare_image(image: Image.Image, size=(384, 384)) -> Image.Image:
    return ImageOps.pad(image, size, color="white", centering=(0.5, 0.5), method=Image.BICUBIC)

def generate_jsonls_if_missing(base_input: Path):
    for subdir in sorted(base_input.iterdir()):
        if not subdir.is_dir():
            continue

        image_files = list(subdir.glob("*.png"))
        if not image_files:
            continue

        jsonl_path = subdir / "dataset.jsonl"
        images_dir = subdir / "images"

        if jsonl_path.exists() and len(list(images_dir.glob("*.png"))) >= len(image_files):
            tqdm.write(f"Überspringe {subdir.name}, da bereits verarbeitet.")
            continue

        images_dir.mkdir(parents=True, exist_ok=True)

        folder_label = subdir.name
        true_label = FOLDER_TO_LABEL.get(folder_label, folder_label if len(folder_label) == 1 else None)
        if true_label is None:
            print(f"Kein Mapping für '{folder_label}' – übersprungen.")
            continue

        with open(jsonl_path, "w", encoding="utf-8") as f_jsonl:
            for i, image_path in enumerate(tqdm(image_files, desc=f"Autogen: {folder_label}")):
                try:
                    image = Image.open(image_path).convert("RGB")
                    if base_input.name != AUGMENTED_FOLDER_NAME:
                        image = normalize_contrast(image)
                    image = prepare_image(image)

                    new_name = f"{folder_label}_{i:05}.png"
                    image.save(images_dir / new_name)

                    f_jsonl.write(json.dumps({
                        "image": f"images/{new_name}",
                        "text": true_label
                    }, ensure_ascii=False) + "\n")
                except Exception as e:
                    print(f"Fehler bei {image_path}: {e}")

tqdm.write("Prüfe auf fehlende dataset.jsonl-Dateien")
for dataset_dir in BASE_DATASET_DIRS:
    generate_jsonls_if_missing(dataset_dir)

def process_jsonl_entry(entry, source_dir, is_augmented):
    try:
        label = entry["text"].split("TEXT:")[-1].strip()
        relative_path = Path(entry["image"]).relative_to("images")
        src_image_path = source_dir / relative_path

        base_stem = Path(entry["image"]).stem
        new_name = f"{base_stem}_{random.randint(1000, 9999)}.png"

        dest_image_path = OUTPUT_IMAGES / new_name

        if is_augmented:
            shutil.copy2(src_image_path, dest_image_path)
        else:
            with Image.open(src_image_path) as img:
                norm_img = normalize_contrast(img)
                norm_img.save(dest_image_path)

        entry["image"] = f"images/{new_name}"
        return label, entry
    except Exception as e:
        print(f"Fehler bei {entry.get('image', '???')}: {e}")
        return None

label_to_entries = defaultdict(list)

jsonl_paths = []
for base_dir in BASE_DATASET_DIRS:
    jsonl_paths.extend(base_dir.rglob("dataset.jsonl"))

tqdm.write("Verarbeite JSONLs parallel...")

with ThreadPoolExecutor(max_workers=8) as executor:
    futures = []
    for path in jsonl_paths:
        source_dir = path.parent / "images"
        is_augmented = AUGMENTED_FOLDER_NAME in str(path.resolve())
        with open(path, "r", encoding="utf-8") as f:
            for line in f:
                entry = json.loads(line)
                futures.append(executor.submit(process_jsonl_entry, entry, source_dir, is_augmented))

    for future in tqdm(as_completed(futures), total=len(futures), desc="Einträge kopieren"):
        result = future.result()
        if result is not None:
            label, processed_entry = result
            label_to_entries[label].append(processed_entry)

possible_chunks = min(len(entries) // EXAMPLES_PER_CLASS_PER_CHUNK for entries in label_to_entries.values())
num_chunks = min(possible_chunks, MAX_CHUNKS)
print(f"\nMaximal {num_chunks} vollständige Chunks möglich.")

for chunk_idx in range(1, num_chunks + 1):
    chunk_entries = []
    for label, entries in label_to_entries.items():
        if len(entries) >= EXAMPLES_PER_CLASS_PER_CHUNK:
            selected = random.sample(entries, EXAMPLES_PER_CLASS_PER_CHUNK)
        else:
            selected = []
        chunk_entries.extend(selected)
        label_to_entries[label] = [e for e in entries if e not in selected]

    random.shuffle(chunk_entries)
    chunk_path = OUTPUT_BASE / f"dataset_chunk_{chunk_idx:02}.jsonl"
    with open(chunk_path, "w", encoding="utf-8") as f_out:
        for entry in chunk_entries:
            f_out.write(json.dumps(entry, ensure_ascii=False) + "\n")

    print(f"Chunk {chunk_idx:02} gespeichert mit {len(chunk_entries)} Beispielen → {chunk_path}")

print("\nAlle Chunks erstellt.")
