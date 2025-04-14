from flask import Flask, request, jsonify
from PIL import Image
from transformers import TrOCRProcessor, VisionEncoderDecoderModel, GenerationConfig
from postprocessing import postprocess_all
import torch

app = Flask(__name__)

LOCAL_MODEL_PATH = "./models/fhswf_trocr"

processor = TrOCRProcessor.from_pretrained(LOCAL_MODEL_PATH, local_files_only=True)
model = VisionEncoderDecoderModel.from_pretrained(LOCAL_MODEL_PATH, local_files_only=True)
model.eval()

generation_config = GenerationConfig.from_pretrained(LOCAL_MODEL_PATH, local_files_only=True)
generation_config.max_length = 64

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

    results = {}
    for name, (x, y, w, h) in field_boxes.items():
        try:
            crop = image.crop((x, y, x + w, y + h))
            pixel_values = processor(images=crop, return_tensors="pt").pixel_values
            with torch.no_grad():
                generated_ids = model.generate(pixel_values, generation_config=generation_config)
            text = processor.batch_decode(generated_ids, skip_special_tokens=True)[0]
            results[name] = text.strip()
        except Exception as e:
            results[name] = f"[Fehler: {e}]"

    form_type = request.form.get("formType", "default")
    corrected_results = postprocess_all(results, form_type=form_type)
    return jsonify(corrected_results)

if __name__ == "__main__":
    app.run(host="127.0.0.1", port=6000, debug=True)
