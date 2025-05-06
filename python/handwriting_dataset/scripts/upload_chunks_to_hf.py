from datasets import load_dataset, Features, Value, Image
from pathlib import Path
import json

base_dir = Path("E:/huggingface_datasets/handwriting_chunks")
image_dir = base_dir / "images"


chunk_files = sorted(base_dir.glob("dataset_chunk_*.jsonl"))

features = Features({
    "image": Image(),
    "text": Value("string")
})

for chunk_file in chunk_files:
    chunk_name = chunk_file.stem
    chunk_index = int(chunk_name.split("_")[-1])

    print(f"\nVerarbeite {chunk_name}...")

    fixed_jsonl = base_dir / f"dataset_temp_{chunk_index:02}.jsonl"

    with open(chunk_file, "r", encoding="utf-8") as f_in, open(fixed_jsonl, "w", encoding="utf-8") as f_out:
        for line in f_in:
            entry = json.loads(line)
            image_path = image_dir / Path(entry["image"]).name
            entry["image"] = str(image_path.resolve())
            f_out.write(json.dumps(entry, ensure_ascii=False) + "\n")

    dataset = load_dataset(
        "json",
        data_files=str(fixed_jsonl),
        split="train",
        features=features
    )

    repo_name = f"sSalfelder/handwriting_sl_chunk{chunk_index}"
    dataset.push_to_hub(repo_name, private=True)

    print(f"{chunk_name} erfolgreich hochgeladen nach {repo_name}!")

print("\nAlle Chunks wurden erfolgreich hochgeladen!")
