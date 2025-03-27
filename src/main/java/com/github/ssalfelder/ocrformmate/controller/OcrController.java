package com.github.ssalfelder.ocrformmate.controller;

import java.io.File;
import java.io.FileInputStream;
import java.text.Normalizer;

import com.github.ssalfelder.ocrformmate.init.OpenCvLoader;
import com.github.ssalfelder.ocrformmate.service.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.bytedeco.opencv.opencv_core.Mat;
import static org.bytedeco.opencv.global.opencv_imgcodecs.*;


import javax.print.Doc;

@Component
public class OcrController {

    private final OcrResultService ocrResultService;
    private final DocumentAlignmentService documentAlignmentService;
    private final FormFieldService formFieldDetectionService;

    @Autowired
    public OcrController(OcrResultService ocrResultService,
                         DocumentAlignmentService documentAlignmentService,
                         FormFieldService formFieldDetectionService) {
        this.ocrResultService = ocrResultService;
        this.documentAlignmentService = documentAlignmentService;
        this.formFieldDetectionService = formFieldDetectionService;
    }
    @FXML
    private ComboBox<String> formTypeComboBox;

    @FXML
    private Button ocrButton;

    @FXML
    private TextArea resultTextArea;

    @FXML
    private ImageView imageView;

    private final String[] FORMTYPE = {"Auto", "Buergergeld", "Anmeldung"};

    // Der Service, der den OCR-Aufruf abwickelt
    private OcrService ocrService = new OcrService();

    @Autowired
    private AlignmentAnalyzer alignmentAnalyzer;

    @Autowired
    private PdfConverterService pdfConverterService;

    @Autowired
    private ImageScalerService imageScalerService;

    @FXML
    private void initialize(){
        formTypeComboBox.getItems().addAll(FORMTYPE);
        formTypeComboBox.getSelectionModel().selectFirst();
    }

    // Diese Methode wird beim Klick auf den Button aufgerufen
    @FXML
    private void onOcrButtonClicked(ActionEvent event) {
        OpenCvLoader.init();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wählen Sie eine PDF- oder Bilddatei mit Formulardaten aus");
        File selectedFile = fileChooser.showOpenDialog(ocrButton.getScene().getWindow());
        String formType = formTypeComboBox.getValue();

        if (selectedFile != null) {
            try {
                String inputPath = selectedFile.getAbsolutePath();
                String tempPngPath = "converted_from_pdf.png";
                String normalizedInputPath = "normalized_input.png";

                // 1) Falls PDF, vorher konvertieren
                if (inputPath.toLowerCase().endsWith(".pdf")) {
                    inputPath = pdfConverterService.convertPdfToPng(inputPath, tempPngPath, 300);
                }

                inputPath = imageScalerService.scaleToTargetSize(inputPath, normalizedInputPath);

                // 2) Dokument ausrichten
                String alignedPath = "aligned_temp.png";
                boolean success = documentAlignmentService.alignDocument(inputPath, alignedPath);

                if (!success) {
                    resultTextArea.setText("Fehler bei der Dokument-Ausrichtung.");
                    return;
                }

                // 3) Analyse & Felder überlagern
                Mat alignedMat = imread(alignedPath);
                alignmentAnalyzer.analyzeLines(imread(inputPath), "Original");
                alignmentAnalyzer.analyzeLines(alignedMat, "Nach Ausrichtung");

                Mat withFieldsMarked = formFieldDetectionService.overlayTemplateFields(alignedMat, formType);
                imwrite("form_preview.png", withFieldsMarked);

                // 4) Vorschau im ImageView
                Image image = new Image(new FileInputStream("form_preview.png"));
                imageView.setImage(image);
                printImageResolution(image);

                // 5) OCR auf ausgerichtetes Bild ohne Markierungen anwenden
                String recognizedText = ocrService.handleHandwritingOCR(new File(alignedPath));

                if (recognizedText != null && recognizedText.equals("__SKIPPED__")) {
                    resultTextArea.setText("Seite enthält nur vorgedruckten Text – Übersprungen.");
                    return;
                }

                resultTextArea.setText(recognizedText);
                ocrResultService.saveOcrResult(recognizedText);

            } catch (Exception e) {
                resultTextArea.setText("Fehler bei der OCR: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            resultTextArea.setText("Keine Datei ausgewählt.");
        }
    }

    public void printImageResolution(Image image) {
        System.out.println("🖼️ Bildgröße:");
        System.out.println("  ➤ Breite:  " + image.getWidth());
        System.out.println("  ➤ Höhe:    " + image.getHeight());
    }
}
