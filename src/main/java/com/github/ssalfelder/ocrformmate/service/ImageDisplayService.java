package com.github.ssalfelder.ocrformmate.service;

import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Service;

import static org.bytedeco.opencv.global.opencv_imgcodecs.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

@Service
public class ImageDisplayService {

    /**
     * Entfernt den Alphakanal (Transparenz) aus einem Bild, falls vorhanden.
     * Speichert das Ergebnis als neue Datei im RGB-Format.
     *
     * @param inputPath  Pfad zur PNG-Datei mit möglichem Alpha
     * @param outputPath Zielpfad für das Bild ohne Alpha
     * @return true, wenn erfolgreich
     */
    public boolean removeAlphaChannel(String inputPath, String outputPath) {
        Mat input = imread(inputPath, IMREAD_UNCHANGED);
        if (input.empty()) {
            System.err.println("Bild konnte nicht geladen werden: " + inputPath);
            return false;
        }

        if (input.channels() == 4) {
            Mat rgb = new Mat();
            cvtColor(input, rgb, COLOR_BGRA2BGR);
            return imwrite(outputPath, rgb);
        } else {
            return imwrite(outputPath, input);
        }
    }
}
