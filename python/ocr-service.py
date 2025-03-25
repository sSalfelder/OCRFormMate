#!/usr/bin/env python3
# ocr-service.py

# !/usr/bin/env python3
# ocr-service.py

import os
import numpy as np
from flask import Flask, request, jsonify
from PIL import Image

from paddleocr import PaddleOCR

app = Flask(__name__)

# Sie können bei Bedarf weitere Parameter einstellen,
# z.B. use_angle_cls=True für Schräglagen-Klassifikation usw.
# Für Deutsche Schrift: lang='de'
ocr_model = PaddleOCR(use_angle_cls=True, lang="en")  # Beispiel: englisches Modell


@app.route("/handwriting/recognize", methods=["POST"])
def recognize_handwriting():
    """
    Erwartet multipart/form-data mit Key "image".
    Führt PaddleOCR auf dem Bild aus und gibt erkannten Text als JSON zurück.
    """
    if "image" not in request.files:
        return jsonify({"error": "No 'image' file in request"}), 400

    file = request.files["image"]
    if file.filename == "":
        return jsonify({"error": "Empty filename"}), 400

    try:
        pil_img = Image.open(file).convert("RGB")
    except Exception as e:
        return jsonify({"error": f"Cannot open file: {e}"}), 400

    # In NumPy-Array umwandeln
    np_img = np.array(pil_img)

    try:
        # PaddleOCR gibt eine Liste von Seiten zurück.
        # Jede Seite ist wieder eine Liste erkannter Textbereiche
        # Format: [ [Koordinaten, (Text, Konfidenz)], ... ]
        result = ocr_model.ocr(np_img, cls=True)

        if not result or all(len(line) == 0 for line in result):
            return jsonify({"error": "No text detected in image."}), 4

        # Wir extrahieren aus jedem erkannten Bereich nur den Text
        recognized_lines = []
        for page in result:
            for box_data in page:
                text, confidence = box_data[1]
                recognized_lines.append(text)

        recognized_text = "\n".join(recognized_lines)

    except Exception as e:
        return jsonify({"error": f"Error in recognition: {e}"}), 500

    return jsonify({"text": recognized_text})


if __name__ == "__main__":
    # Beispiel: 127.0.0.1:5000
    app.run(host="127.0.0.1", port=5000, debug=True)

