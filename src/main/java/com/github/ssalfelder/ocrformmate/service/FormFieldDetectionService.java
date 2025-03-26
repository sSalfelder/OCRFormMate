package com.github.ssalfelder.ocrformmate.service;

import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_imgproc.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_imgproc.*;

@Service
public class FormFieldDetectionService {

    public Mat detectAndDrawFields(Mat alignedDoc) {
        // 1) In Graustufen umwandeln
        Mat gray = new Mat();
        cvtColor(alignedDoc, gray, COLOR_BGR2GRAY);

        // 2) Threshold oder adaptives Threshold
        Mat binary = new Mat();
        threshold(gray, binary, 120, 255, THRESH_BINARY_INV);

        // 3) Morphologische Operation (Close)
        Mat kernel = getStructuringElement(MORPH_RECT, new Size(3, 3));
        morphologyEx(binary, binary, MORPH_CLOSE, kernel);

        // 4) Konturen finden
        MatVector contours = new MatVector();
        Mat hierarchy = new Mat();
        findContours(binary, contours, hierarchy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);

        // 5) Konturen durchlaufen, ggf. filtern
        for (long i = 0; i < contours.size(); i++) {
            Mat contour = contours.get(i);
            Rect rect = boundingRect(contour);

            if (rect.width() > 30 && rect.height() > 10) {
                rectangle(alignedDoc, rect, new Scalar(0, 0, 255, 0), 2, LINE_8, 0);
            }
        }

        return alignedDoc;
    }
}

