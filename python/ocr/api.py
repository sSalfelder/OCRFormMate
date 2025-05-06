from flask import Flask, request, jsonify
from PIL import Image
from pathlib import Path
from transformers import TrOCRProcessor, VisionEncoderDecoderModel, GenerationConfig
from ocr.postprocessing import postprocess_all
from concurrent.futures import ThreadPoolExecutor, as_completed
import torch

app = Flask(__name__)

LOCAL_MODEL_PATH = Path("../models/fhswf_trocr").resolve()

if not LOCAL_MODEL_PATH.exists():
    raise FileNotFoundError(f"Model path not found: {LOCAL_MODEL_PATH}")

processor = TrOCRProcessor.from_pretrained(LOCAL_MODEL_PATH, local_files_only=True, use_fast=True)
model = VisionEncoderDecoderModel.from_pretrained(LOCAL_MODEL_PATH, local_files_only=True)
model.eval()

generation_config = GenerationConfig.from_pretrained(LOCAL_MODEL_PATH, local_files_only=True)


def optimize_generation_config_for_speed(config):
    config.num_beams = 1
    config.do_sample = False
    config.length_penalty = 1.0
    config.no_repeat_ngram_size = 0
    config.early_stopping = False
    config.use_cache = True
    config.max_length = 50
    return config


generation_config = optimize_generation_config_for_speed(generation_config)

# OCR-Feldfunktion (für parallele Threads)
def recognize_single_field(name, crop, processor, model, config):
    try:
        pixel_values = processor(images=crop, return_tensors="pt").pixel_values
        print(f"[{name}] final config in use:")
        for k, v in config.to_dict().items():
            print(f"{k:25}: {v}")
        with torch.no_grad():
            generated_ids = model.generate(pixel_values, generation_config=config)
        text = processor.batch_decode(generated_ids, skip_special_tokens=True)[0]
        return name, text.strip()
    except Exception as e:
        return name, f"[Fehler: {e}]"

@app.route("/handwriting/recognize", methods=["POST"])
def recognize_handwriting():
    if "image" not in request.files:
        return jsonify({"error": "No 'image' file in request"}), 400

    file = request.files["image"]
    try:
        image = Image.open(file.stream).convert("RGB")
    except Exception as e:
        return jsonify({"error": f"Cannot open image: {e}"}), 400

    try:
        pixel_values = processor(images=image, return_tensors="pt").pixel_values
        print("Final config in use:")
        print(generation_config)
        with torch.no_grad():
            generated_ids = model.generate(pixel_values, generation_config=generation_config)
        text = processor.batch_decode(generated_ids, skip_special_tokens=True)[0]
        return jsonify({"text": text.strip()})
    except Exception as e:
        return jsonify({"error": f"OCR failed: {e}"}), 500

@app.route("/handwriting/recognizeFields", methods=["POST"])
def recognize_fields():
    if "image" not in request.files or "fields" not in request.form:
        return jsonify({"error": "Image or fields missing"}), 400

    try:
        image = Image.open(request.files["image"]).convert("RGB")
        fields = request.form.get("fields")
        field_boxes = eval(fields)  # Format: {"feldname": [x, y, w, h]}
    except Exception as e:
        return jsonify({"error": f"Parsing error: {e}"}), 400

    # Parallelisierte Felderkennung
    with ThreadPoolExecutor(max_workers=6) as executor:
        futures = []
        for name, (x, y, w, h) in field_boxes.items():
            crop = image.crop((x, y, x + w, y + h))
            futures.append(executor.submit(recognize_single_field, name, crop, processor, model, generation_config))

        results = {}
        for future in as_completed(futures):
            name, text = future.result()
            results[name] = text

    form_type = request.form.get("formType", "default")
    corrected_results = postprocess_all(results, form_type=form_type)
    return jsonify(corrected_results)

if __name__ == "__main__":
    app.run(host="127.0.0.1", port=6000, debug=True, use_reloader=False)
