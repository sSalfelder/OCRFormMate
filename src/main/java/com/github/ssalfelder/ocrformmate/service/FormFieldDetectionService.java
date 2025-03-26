package com.github.ssalfelder.ocrformmate.service;

import org.bytedeco.opencv.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;
import org.springframework.stereotype.Service;

import static org.bytedeco.opencv.global.opencv_imgproc.*;

@Service
public class FormFieldDetectionService {

    public Mat detectAndDrawFields(Mat alignedDoc, String formType) {
        // 1) Graustufen
        Mat gray = new Mat();
        cvtColor(alignedDoc, gray, COLOR_BGR2GRAY);

        // 2) Threshold
        Mat binary = new Mat();
        threshold(gray, binary, 120, 255, THRESH_BINARY_INV);

        // 3) Morphologisches Closing
        Mat kernel = getStructuringElement(MORPH_RECT, new Size(3, 3));
        morphologyEx(binary, binary, MORPH_CLOSE, kernel);

        // 4) Konturen finden
        MatVector contours = new MatVector();
        Mat hierarchy = new Mat();
        findContours(binary, contours, hierarchy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);

        int fieldCount = 0;

        // 5) Durch alle Konturen
        for (long i = 0; i < contours.size(); i++) {
            Mat contour = contours.get(i);
            Rect rect = boundingRect(contour);

            if (isFieldCandidate(rect, formType)) {
                rectangle(alignedDoc, rect, new Scalar(0, 0, 255, 0), 2, LINE_8, 0);
                fieldCount++;
            }
        }

        System.out.println("Gefundene Formularfelder für Typ [" + formType + "]: " + fieldCount);

        // 6) Vorschau speichern
        imwrite("form_preview.png", alignedDoc);

        return alignedDoc;
    }

    private boolean isFieldCandidate(Rect rect, String formType) {
        int width = rect.width();
        int height = rect.height();
        int y = rect.y();

        switch (formType.toUpperCase()) {
            case "BUERGERGELD":
                return width > 80 && height < 60 && y < 1800;  // obere Formularhälfte, schmale Linien
            case "ANMELDUNG":
                return width > 60 && height > 10 && height < 50; // mittelhohe Felder, auch Kästchen
            case "AUTO":
                return width > 150 && height < 50; // z. B. Kfz-Formulare
            default: // GENERIC
                return width > 30 && height > 10 && width < 800;
        }
    }
}
