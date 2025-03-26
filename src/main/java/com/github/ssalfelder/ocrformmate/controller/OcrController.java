package com.github.ssalfelder.ocrformmate.controller;

import java.io.File;
import java.text.Normalizer;

import com.github.ssalfelder.ocrformmate.init.OpenCvLoader;
import com.github.ssalfelder.ocrformmate.service.DocumentAlignmentService;
import com.github.ssalfelder.ocrformmate.service.FormFieldDetectionService;
import com.github.ssalfelder.ocrformmate.service.OcrResultService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import com.github.ssalfelder.ocrformmate.service.OcrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.bytedeco.opencv.opencv_core.Mat;
import static org.bytedeco.opencv.global.opencv_imgcodecs.*;


import javax.print.Doc;

@Component
public class OcrController {

    private final OcrResultService ocrResultService;
    private final DocumentAlignmentService documentAlignmentService;
    private final FormFieldDetectionService formFieldDetectionService;

    @Autowired
    public OcrController(OcrResultService ocrResultService,
                         DocumentAlignmentService documentAlignmentService,
                         FormFieldDetectionService formFieldDetectionService) {
        this.ocrResultService = ocrResultService;
        this.documentAlignmentService = documentAlignmentService;
        this.formFieldDetectionService = formFieldDetectionService;
    }

    @FXML
    private Button ocrButton;

    @FXML
    private TextArea resultTextArea;

    // Der Service, der den OCR-Aufruf abwickelt
    private OcrService ocrService = new OcrService();



    // Diese Methode wird beim Klick auf den Button aufgerufen
    @FXML
    private void onOcrButtonClicked(ActionEvent event) {
        // Öffnet einen Datei-Chooser, um das Bild auszuwählen
        OpenCvLoader.init();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wählen sie eine Bilddatei mit Formulardaten aus");
        File selectedFile = fileChooser.showOpenDialog(ocrButton.getScene().getWindow());

        if (selectedFile != null) {
            try {

                // 1) Pfade vorbereiten (z.B. temporär)
                String alignedPath = "aligned_temp.png";

                // 2) Dokument ausrichten
                boolean success = documentAlignmentService.alignDocument(
                        selectedFile.getAbsolutePath(),
                        alignedPath
                );

                if (!success) {
                    resultTextArea.setText("Fehler bei der Dokument-Ausrichtung.");
                    return;
                }

                // Eingelesenes (ausgerichtetes) Bild in OpenCV laden
                Mat alignedMat = imread(alignedPath);

                // Formularfelder markieren
                Mat withFieldsMarked = formFieldDetectionService.detectAndDrawFields(alignedMat);

                // Debug-/Vorschau-Anzeige (falls gewünscht)
                // showPreview(withFieldsMarked);

                // Das markierte Bild ggf. erneut speichern (zur Kontrolle)
                imwrite("debug_fields_marked.png", withFieldsMarked);

                // OCR-Service aufrufen (hier ggf. das markierte Bild nehmen oder
                // besser das Original/entzerrte Bild ohne Markierungen, je nach Bedarf)
                String recognizedText = ocrService.handleHandwritingOCR(new File(alignedPath));

                // "Skip"-Logik prüfen (wenn das Backend skipped meldet)
                if (recognizedText != null && recognizedText.equals("__SKIPPED__")) {
                    resultTextArea.setText("Seite enthält nur vorgedruckten Text – Übersprungen.");
                    return;
                }

                // In der TextArea anzeigen
                resultTextArea.setText(recognizedText);

                // Als OcrResult speichern
                ocrResultService.saveOcrResult(recognizedText);

            } catch (Exception e) {
                resultTextArea.setText("Fehler bei der OCR: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            resultTextArea.setText("Keine Datei ausgewählt.");
        }
    }
}
