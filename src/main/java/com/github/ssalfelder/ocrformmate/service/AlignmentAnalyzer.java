package com.github.ssalfelder.ocrformmate.service;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_imgproc.Vec2fVector;
import org.springframework.stereotype.Component;

import static org.bytedeco.opencv.global.opencv_imgproc.*;

@Component
public class AlignmentAnalyzer {

    public void analyzeLines(Mat image, String label) {
        // In Graustufen umwandeln
        Mat gray = new Mat();
        if (image.channels() > 1) {
            cvtColor(image, gray, COLOR_BGR2GRAY);
        } else {
            gray = image.clone();
        }

        // Canny-Kantenerkennung
        Mat edges = new Mat();
        Canny(gray, edges, 50, 200);

        // Hough-Linien finden
        Vec2fVector lines = new Vec2fVector();
        HoughLines(edges, lines, 1, Math.PI / 180, 100, 0, 0, 0, Math.PI);

        // Linienklassifikation
        int totalLines = (int) lines.size();
        int horizontal = 0, vertical = 0, diagonal = 0;

        for (int i = 0; i < totalLines; i++) {
            float rho = lines.get(i).get(0);
            float theta = lines.get(i).get(1);

            double angleDeg = Math.toDegrees(theta);

            if (angleDeg >= 85 && angleDeg <= 95) {
                horizontal++;
            } else if (angleDeg <= 5 || angleDeg >= 175) {
                vertical++;
            } else {
                diagonal++;
            }
        }

        System.out.println("Analyse für: " + label);
        System.out.println("  ➤ Gesamtlinien: " + totalLines);
        System.out.println("  ➤ Horizontal:   " + horizontal);
        System.out.println("  ➤ Vertikal:     " + vertical);
        System.out.println("  ➤ Diagonal:     " + diagonal);
        System.out.println("----------------------------------");
    }
}
