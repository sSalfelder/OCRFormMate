package com.github.ssalfelder.ocrformmate.service;

import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.*;
import org.springframework.stereotype.Service;

import static org.bytedeco.opencv.global.opencv_imgcodecs.*;

@Service
public class ImageScalerService {

    // Zielgröße: A4 bei 300 DPI
    private final Size targetSize = new Size(2480, 3508);

    public String scaleToTargetSize(String inputPath, String outputPath) {
        Mat original = imread(inputPath);
        if (original.empty()) {
            throw new RuntimeException("Bild konnte nicht geladen werden: " + inputPath);
        }

        Mat scaled = new Mat();
        opencv_imgproc.resize(original, scaled, targetSize);
        imwrite(outputPath, scaled);
        return outputPath;
    }
}
