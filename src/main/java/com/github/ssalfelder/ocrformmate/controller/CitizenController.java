package com.github.ssalfelder.ocrformmate.controller;

import com.github.ssalfelder.ocrformmate.OcrFormMateApp;
import com.github.ssalfelder.ocrformmate.auth.CitizenSessionHolder;
import com.github.ssalfelder.ocrformmate.model.User;
import com.github.ssalfelder.ocrformmate.service.CitizenService;
import com.github.ssalfelder.ocrformmate.service.OcrAssignmentService;
import com.github.ssalfelder.ocrformmate.session.OcrSessionHolder;
import com.github.ssalfelder.ocrformmate.ui.DialogHelper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CitizenController {

    //TODO Ausgabefenster mit dem zu beschreibenden Formular
    //TODO eingereicht am Zeitstempel auch im DialogHelper Fenster
    //TODO mehrseitiges PDF (ganzer Bürgergeldantrag)

    //TODO Clerkseite einrichten + zusätzliche Features
    //TODO Neue Generierung zu Vornamen und neue Datensätze
    @Autowired
    private OcrAssignmentService ocrAssignmentService;

    @FXML
    private ComboBox<String> citizenAuthorityChooser;

    @FXML
    private Label loginStatusLabel;

    @FXML
    private Button citizenOCRSubmitButton;

    @Autowired
    private CitizenService citizenService;

    @Autowired
    private OcrController ocrController;

    private final String[] AUTHORITY = {"Jobcenter", "Meldeamt"};

    @FXML
    public void initialize() {
        updateLoginStatusLabel();
        citizenAuthorityChooser.getItems().addAll(AUTHORITY);
        citizenAuthorityChooser.getSelectionModel().selectFirst();

        citizenOCRSubmitButton.setDisable(!OcrSessionHolder.isAvailable());

        Platform.runLater(() -> {
            ocrController.setCitizenController(this);
            ocrController.syncFormTypeWithCitizenController();
        });

    }

    private void updateLoginStatusLabel() {
        if (CitizenSessionHolder.isLoggedIn()) {
            User user = CitizenSessionHolder.getUser();
            loginStatusLabel.setText("🟢 Angemeldet als: " + user.getEmail());
        } else {
            loginStatusLabel.setText("🔴 Nicht angemeldet");
        }
    }

    @FXML
    protected void onCitizenLogin(ActionEvent event) {
        loadLoginMask();
    }

    @FXML
    protected void onCitizenLogout(ActionEvent event) {
        CitizenSessionHolder.clear();
        updateLoginStatusLabel();
        DialogHelper.showInfo("Abgemeldet", "Sie wurden erfolgreich abgemeldet.");
    }

    @FXML
    protected void onCitizenOCRSubmit(ActionEvent event) {
        loadLoginMask();

        String text = OcrSessionHolder.get();
        String authority = citizenAuthorityChooser.getValue();

        if (text == null || text.isBlank()) {
            System.out.println("Kein OCR-Ergebnis vorhanden.");
            return;
        }

        try {
            User user = CitizenSessionHolder.getUser();
            int userId = user.getId();
            ocrAssignmentService.saveForUser(text, userId, authority);
            System.out.println("OCR gespeichert für User " + user.getId() + " mit Behörde: " + authority);
            DialogHelper.showInfo("Erfolg", "Die Formulardaten wurden erfolgreich übermittelt.");
        } catch (Exception e) {
            System.err.println("Fehler beim Speichern: " + e.getMessage());
        }
    }

    public void enableSubmitIfOcrAvailable() {
        citizenOCRSubmitButton.setDisable(!OcrSessionHolder.isAvailable());

        String formType = OcrSessionHolder.getFormType();
        if ("Buergergeld".equals(formType)) {
            citizenAuthorityChooser.getSelectionModel().select("Jobcenter");
            citizenAuthorityChooser.setDisable(true);
        }
    }

    protected void loadLoginMask() {
        if (!CitizenSessionHolder.isLoggedIn()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "/com/github/ssalfelder/ocrformmate/fxml/citizen-login.fxml"));
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

                updateLoginStatusLabel();
                DialogHelper.showInfo("Login erfolgreich", "Sie sind nun angemeldet als:\n" +
                        CitizenSessionHolder.getUser().getEmail());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateAuthorityBasedOnFormType(String type) {
        if ("Buergergeld".equals(type)) {
            citizenAuthorityChooser.getSelectionModel().select("Jobcenter");
            citizenAuthorityChooser.setDisable(true);
        } else if ("Anmeldung".equals(type)) {
            citizenAuthorityChooser.getSelectionModel().select("Meldeamt");
            citizenAuthorityChooser.setDisable(true);
        } else {
            citizenAuthorityChooser.setDisable(false);
        }
    }
}
