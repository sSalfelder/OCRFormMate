package com.github.ssalfelder.ocrformmate.controller;

import com.github.ssalfelder.ocrformmate.service.OcrAssignmentService;
import com.github.ssalfelder.ocrformmate.ui.DialogHelper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ClerkOcrController {

    @FXML
    private TextArea resultTextArea;

    private final OcrAssignmentService ocrAssignmentService;

    @Autowired
    public ClerkOcrController(OcrAssignmentService ocrAssignmentService) {
        this.ocrAssignmentService = ocrAssignmentService;
    }

    @FXML
    protected void onClerkOCRSubmit(ActionEvent event) {
        String text = resultTextArea.getText();

        if (text == null || text.isBlank()) {
            DialogHelper.showWarning("Fehlende Eingabe", "Keine OCR-Daten gefunden.");
            return;
        }

        ocrAssignmentService.saveForLoggedInClerk(text);
        DialogHelper.showInfo("Gespeichert", "OCR-Daten wurden gespeichert.");
    }
}
