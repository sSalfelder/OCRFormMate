package com.github.ssalfelder.ocrformmate.controller;

import java.io.File;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import com.github.ssalfelder.ocrformmate.service.OcrService;
import org.springframework.stereotype.Component;

@Component
public class OcrController {

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
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wählen sie eine Bilddatei mit Formulardaten aus");
        File selectedFile = fileChooser.showOpenDialog(ocrButton.getScene().getWindow());

        if (selectedFile != null) {
            try {
                // OCR-Service aufrufen und erkannten Text erhalten
                String recognizedText = ocrService.handleHandwritingOCR(selectedFile);
                resultTextArea.setText(recognizedText);
            } catch (Exception e) {
                resultTextArea.setText("Fehler bei der OCR: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            resultTextArea.setText("Keine Datei ausgewählt.");
        }
    }
}
