#!/usr/bin/env python3
# paddle_ocr_api.py

import os
import numpy as np
from flask import Flask, request, jsonify
from PIL import Image
from paddleocr import PaddleOCR
import json

app = Flask(__name__)

# PaddleOCR-Modell initialisieren
ocr_model = PaddleOCR(use_angle_cls=True, lang="en")  # Drucktext

@app.route("/handwriting/recognize", methods=["POST"])
def recognize_handwriting():
    if "image" not in request.files:
        return jsonify({"error": "No 'image' file in request"}), 400

    file = request.files["image"]
    if file.filename == "":
        return jsonify({"error": "Empty filename"}), 400

    try:
        pil_img = Image.open(file).convert("RGB")
    except Exception as e:
        return jsonify({"error": f"Cannot open file: {e}"}), 400

    np_img = np.array(pil_img)

    try:
        result = ocr_model.ocr(np_img, cls=True)

        # Handschrift-Erkennung anhand niedriger Konfidenz
        handwritten_blocks = [
            box_data[1][0]  # erkannter Text
            for page in result
            for box_data in page
            if box_data[1][1] < 0.85  # Konfidenz unter Schwelle
        ]

        if not handwritten_blocks:
            return jsonify({
                "text": "",
                "skipped": True,
                "reason": "Nur gedruckter Text erkannt"
            }), 200

        if not result or all(len(line) == 0 for line in result):
            return jsonify({"error": "No text detected in image."}), 400

        recognized_lines = []
        for page in result:
            for box_data in page:
                text, confidence = box_data[1]
                recognized_lines.append(text)

        recognized_text = "\n".join(recognized_lines)

    except Exception as e:
        return jsonify({"error": f"Error in recognition: {e}"}), 500

    return jsonify({"text": recognized_text})

@app.route("/handwriting/recognizeFields", methods=["POST"])
def recognize_fields():
    if "image" not in request.files or "fields" not in request.form:
        return jsonify({"error": "Image or fields missing"}), 400

    file = request.files["image"]
    fields_raw = request.form["fields"]

    try:
        pil_img = Image.open(file).convert("RGB")
        np_img = np.array(pil_img)
        field_boxes = json.loads(fields_raw)  # {"feldname": [x, y, w, h]}
    except Exception as e:
        return jsonify({"error": f"Error parsing image or fields: {e}"}), 400

    results = {}
    for name, (x, y, w, h) in field_boxes.items():
        try:
            crop = np_img[y:y+h, x:x+w]
            ocr_result = ocr_model.ocr(crop, cls=True)

            if ocr_result and len(ocr_result[0]) > 0:
                text, _ = ocr_result[0][0][1]
                results[name] = text.strip()
            else:
                results[name] = "[leer]"
        except Exception as e:
            results[name] = "[nicht erkannt]"

    return jsonify(results)

if __name__ == "__main__":
    app.run(host="127.0.0.1", port=5000, debug=True)

