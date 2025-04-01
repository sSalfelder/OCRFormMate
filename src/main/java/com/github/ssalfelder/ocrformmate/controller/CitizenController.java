package com.github.ssalfelder.ocrformmate.controller;

import com.github.ssalfelder.ocrformmate.OcrFormMateApp;
import com.github.ssalfelder.ocrformmate.auth.CitizenSessionHolder;
import com.github.ssalfelder.ocrformmate.model.User;
import com.github.ssalfelder.ocrformmate.service.CitizenService;
import com.github.ssalfelder.ocrformmate.service.OcrAssignmentService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.event.ActionEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CitizenController {

    @Autowired
    private OcrAssignmentService ocrAssignmentService;

    @FXML
    private ComboBox<String> citizenAuthorityChooser;

    @FXML
    private Button citizenOCRSubmitButton;

    @Autowired
    private CitizenService citizenService;

    // Beispiel: dieses Textfeld bekommst du vermutlich aus OcrController via Getter/Injection
    @FXML
    private TextArea resultTextArea;

    private final String[] AUTHORITY = {"Jobcenter", "Meldeamt"};

    private int currentUserId = 1; // später: dynamisch setzen nach Login

    @FXML
    public void initialize() {
        citizenAuthorityChooser.getItems().addAll(AUTHORITY);
        citizenAuthorityChooser.getSelectionModel().selectFirst();
    }

    @FXML
    protected void onCitizenOCRSubmit(ActionEvent event) {
        if (!CitizenSessionHolder.isLoggedIn()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/github/ssalfelder/ocrformmate/" +
                        "fxml/citizen-login.fxml"));
                loader.setControllerFactory(clazz -> OcrFormMateApp.getContext().getBean(clazz));
                Parent loginRoot = loader.load();

                Stage stage = new Stage();
                stage.setScene(new Scene(loginRoot));
                stage.setTitle("Login erforderlich");
                stage.initModality(Modality.APPLICATION_MODAL); // blockiert andere Fenster
                stage.showAndWait();

                // Prüfen ob Login nach Dialog erfolgt ist
                if (!CitizenSessionHolder.isLoggedIn()) {
                    System.out.println("Login wurde abgebrochen.");
                    return;
                }

            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        String text = resultTextArea.getText();
        String authority = citizenAuthorityChooser.getValue();

        if (text == null || text.isBlank()) {
            System.out.println("Kein OCR-Ergebnis vorhanden.");
            return;
        }

        try {
            User user = CitizenSessionHolder.getUser();
            ocrAssignmentService.saveForUser(text, user.getId(), authority);
            System.out.println("OCR gespeichert für User " + user.getId() + " mit Behörde: " + authority);
        } catch (Exception e) {
            System.err.println("Fehler beim Speichern: " + e.getMessage());
        }
    }
}
