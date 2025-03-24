package com.github.ssalfelder.ocrformmate.service;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class DocumentAlignmentService {

    /**
     * Lädt ein Bild von disk und führt die perspektivische Entzerrung durch,
     * falls ein rechteckiges Dokument gefunden wurde.
     *
     * @param inputImagePath Pfad zum Eingabebild (Foto des Formulars)
     * @param outputImagePath Pfad, unter dem das entzerrte Bild gespeichert wird
     * @return true, wenn erfolgreich entzerrt und gespeichert, false sonst
     */
    public boolean alignDocument(String inputImagePath, String outputImagePath) {
        // 1) Bild laden
        Mat original = Imgcodecs.imread(inputImagePath);
        if (original.empty()) {
            System.err.println("Konnte Bild nicht laden: " + inputImagePath);
            return false;
        }

        // 2) Vorverarbeitung: Graustufen + Kantenerkennung
        Mat gray = new Mat();
        Imgproc.cvtColor(original, gray, Imgproc.COLOR_BGR2GRAY);

        Mat blurred = new Mat();
        Imgproc.GaussianBlur(gray, blurred, new Size(5, 5), 0);

        Mat edges = new Mat();
        Imgproc.Canny(blurred, edges, 75, 200);

        // 3) Konturen finden
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        // 4) Kontur wählen, die vermutlich das Dokument ist
        // z. B. größte Kontur, die ~4 Ecken hat
        MatOfPoint2f docContour = findDocumentContour(contours, original.size());
        if (docContour == null) {
            System.err.println("Kein geeignetes Dokument gefunden.");
            return false;
        }

        // 5) Perspektivische Transformation vorbereiten
        // Sortiere Ecken: top-left, top-right, bottom-right, bottom-left
        List<Point> sortedCorners = reorderCorners(docContour);

        // 6) Ziel-Rechteck definieren (z. B. Breite/Höhe)
        // Hier nur ein grobes Beispiel:
        double width = 800;   // z. B. 800 px
        double height = 1000; // z. B. 1000 px

        MatOfPoint2f dstCorners = new MatOfPoint2f(
                new Point(0, 0), new Point(width, 0),
                new Point(width, height), new Point(0, height)
        );

        // 7) Transformation berechnen und anwenden
        MatOfPoint2f sortedCornersMat = new MatOfPoint2f();
        sortedCornersMat.fromList(sortedCorners);

        Mat transform = Imgproc.getPerspectiveTransform(sortedCornersMat, dstCorners);
        Mat aligned = new Mat();
        Imgproc.warpPerspective(original, aligned, transform, new Size(width, height));

        // 8) Ergebnis speichern
        boolean success = Imgcodecs.imwrite(outputImagePath, aligned);
        if (!success) {
            System.err.println("Konnte ausgerichtetes Bild nicht speichern: " + outputImagePath);
        }
        return success;
    }

    /**
     * Sucht nach einer Kontur mit 4 Ecken (Rechteck / Polygon),
     * die groß genug ist, um als Dokument durchzugehen.
     */
    private MatOfPoint2f findDocumentContour(List<MatOfPoint> contours, Size imgSize) {
        // Sortiere nach absteigender Konturflaeche
        contours.sort(Comparator.comparingDouble(Imgproc::contourArea));
        Collections.reverse(contours);

        for (MatOfPoint c : contours) {
            double area = Imgproc.contourArea(c);
            if (area < 1000) {
                // Wenn zu klein, ignorieren
                continue;
            }

            // Approximiere Polygon
            MatOfPoint2f approx = approximatePolygon(c);
            if (approx.total() == 4) {
                // 4 Ecken => könnte unser Dokument sein
                return approx;
            }
        }
        return null;
    }

    /**
     * Wandelt MatOfPoint in MatOfPoint2f und approximiert
     * (vereinfacht) die Kontur zu einem Polygon.
     */
    private MatOfPoint2f approximatePolygon(MatOfPoint contour) {
        MatOfPoint2f contour2f = new MatOfPoint2f(contour.toArray());
        double peri = Imgproc.arcLength(contour2f, true);
        MatOfPoint2f approx = new MatOfPoint2f();
        Imgproc.approxPolyDP(contour2f, approx, 0.02 * peri, true);
        return approx;
    }

    /**
     * Re-Order Ecken: Sucht top-left, top-right, bottom-right, bottom-left
     * Basierend auf https://stackoverflow.com/a/38373300
     */
    private List<Point> reorderCorners(MatOfPoint2f polygon) {
        List<Point> pts = new ArrayList<>(polygon.toList());
        // Sortiere nach y
        pts.sort(Comparator.comparingDouble(p -> p.y));

        // Oberste zwei
        Point top1 = pts.get(0);
        Point top2 = pts.get(1);
        // Unterste zwei
        Point bottom1 = pts.get(2);
        Point bottom2 = pts.get(3);

        // Sortiere top1, top2 nach x
        Point topLeft, topRight;
        if (top1.x < top2.x) {
            topLeft = top1;
            topRight = top2;
        } else {
            topLeft = top2;
            topRight = top1;
        }

        // Sortiere bottom1, bottom2 nach x
        Point bottomLeft, bottomRight;
        if (bottom1.x < bottom2.x) {
            bottomLeft = bottom1;
            bottomRight = bottom2;
        } else {
            bottomLeft = bottom2;
            bottomRight = bottom1;
        }

        List<Point> ordered = new ArrayList<>();
        ordered.add(topLeft);
        ordered.add(topRight);
        ordered.add(bottomRight);
        ordered.add(bottomLeft);
        return ordered;
    }
}
