package com.github.ssalfelder.ocrformmate.service;

import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class DocumentAlignmentService {

    public boolean alignDocument(String inputImagePath, String outputImagePath) {
        Mat original = opencv_imgcodecs.imread(inputImagePath);
        if (original.empty()) {
            System.err.println("Konnte Bild nicht laden: " + inputImagePath);
            return false;
        }

        // Graustufen umwandeln, blurren, Kanten finden
        Mat gray = new Mat();
        opencv_imgproc.cvtColor(original, gray, opencv_imgproc.COLOR_BGR2GRAY);

        Mat blurred = new Mat();
        opencv_imgproc.GaussianBlur(gray, blurred, new Size(5, 5), 0);

        Mat edges = new Mat();
        opencv_imgproc.Canny(blurred, edges, 75, 200);

        // Konturen finden
        MatVector contours = new MatVector();
        Mat hierarchy = new Mat();
        opencv_imgproc.findContours(
                edges,
                contours,
                hierarchy,
                opencv_imgproc.RETR_LIST,
                opencv_imgproc.CHAIN_APPROX_SIMPLE
        );

        // Dokument-Kontur suchen
        Point2fVector docContour = findDocumentContour(contours);
        if (docContour == null) {
            System.out.println("Kein Formularrahmen erkannt – verwende Originalbild.");
            opencv_imgcodecs.imwrite(outputImagePath, original);
            return true;
        }

        // Ecken neu anordnen
        Point2fVector sortedCorners = reorderCorners(docContour);

        // Zielgröße definieren
        double width = 800;
        double height = 1000;
        Point2fVector dstCorners = new Point2fVector(
                new Point2f(0, 0),
                new Point2f((float) width, 0),
                new Point2f((float) width, (float) height),
                new Point2f(0, (float) height)
        );

        Mat srcMat = new Mat(sortedCorners);
        Mat dstMat = new Mat(dstCorners);

        // Perspektivische Transformation berechnen
        Mat transform = opencv_imgproc.getPerspectiveTransform(srcMat, dstMat);
        Mat aligned = new Mat();
        opencv_imgproc.warpPerspective(original, aligned, transform, new Size((int) width, (int) height));

        boolean success = opencv_imgcodecs.imwrite(outputImagePath, aligned);
        if (!success) {
            System.err.println("Konnte ausgerichtetes Bild nicht speichern: " + outputImagePath);
        }
        return success;
    }

    private Point2fVector findDocumentContour(MatVector contours) {
        List<Mat> candidates = new ArrayList<>();
        for (long i = 0; i < contours.size(); i++) {
            Mat contour = contours.get(i);
            double area = opencv_imgproc.contourArea(contour);
            if (area < 1000) continue;

            Mat approxCurve = new Mat();
            opencv_imgproc.approxPolyDP(contour, approxCurve, 0.02 * opencv_imgproc.arcLength(contour, true), true);

            Point2fVector approx = new Point2fVector(approxCurve);
            if (approx.size() == 4) {
                candidates.add(contour);
            }
        }

        if (!candidates.isEmpty()) {
            Mat largest = Collections.max(candidates, Comparator.comparingDouble(opencv_imgproc::contourArea));
            Mat approxCurve = new Mat();
            opencv_imgproc.approxPolyDP(largest, approxCurve, 0.02 * opencv_imgproc.arcLength(largest, true), true);

            Point2fVector approx = new Point2fVector(approxCurve);
            return approx;
        }
        return null;
    }

    private Point2fVector reorderCorners(Point2fVector polygon) {
        // Listenkonvertierung für Sortierung
        List<Point2f> pts = new ArrayList<>();
        for (long i = 0; i < polygon.size(); i++) {
            pts.add(polygon.get(i));
        }

        // Sortiere nach Y
        pts.sort(Comparator.comparingDouble(p -> p.y()));

        Point2f top1 = pts.get(0);
        Point2f top2 = pts.get(1);
        Point2f bottom1 = pts.get(2);
        Point2f bottom2 = pts.get(3);

        Point2f topLeft, topRight;
        if (top1.x() < top2.x()) {
            topLeft = top1;
            topRight = top2;
        } else {
            topLeft = top2;
            topRight = top1;
        }

        Point2f bottomLeft, bottomRight;
        if (bottom1.x() < bottom2.x()) {
            bottomLeft = bottom1;
            bottomRight = bottom2;
        } else {
            bottomLeft = bottom2;
            bottomRight = bottom1;
        }

        Point2fVector ordered = new Point2fVector(4);
        ordered.put(0, topLeft);
        ordered.put(1, topRight);
        ordered.put(2, bottomRight);
        ordered.put(3, bottomLeft);

        return ordered;
    }
}
