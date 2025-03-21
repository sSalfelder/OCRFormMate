package com.github.ssalfelder.ocrformmate.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;


public class FXController {
    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private TextField adminUsername;

    @FXML
    private TextField adminPassword;

    @FXML
    private TextField adminSecret;

    @FXML
    protected void citizenClick(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("/com/github/ssalfelder/ocrformmate/fxml/citizen.fxml"));
        switchScene(event);
    }
    @FXML
    protected void clerkClick(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("/com/github/ssalfelder/ocrformmate/fxml/clerk.fxml"));
        switchScene(event);
    }

    @FXML
    protected void goBack(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("/com/github/ssalfelder/ocrformmate/fxml/main.fxml"));
        switchScene(event);
    }

    @FXML
    protected void registrationClerk(ActionEvent event) throws  IOException {
        root = FXMLLoader.load(getClass().getResource("/com/github/ssalfelder/ocrformmate/fxml/admin-password-dialog.fxml"));
        switchScene(event);
    }

    @FXML
    protected void registrationCitizen(ActionEvent event) throws  IOException {
        root = FXMLLoader.load(getClass().getResource("/com/github/ssalfelder/ocrformmate/fxml/registration-citizen.fxml"));
        switchScene(event);
    }

    @FXML
    protected void clerkLoginClick() {

    }

    @FXML
    protected void clerkResetClick() {

    }

    @FXML
    protected void adminSecretClick() {
        if (!adminSecret.getText().isEmpty()) {
            String secretInput = adminSecret.getText();

        }
        else {
            showWarningWindow("Fehlende Eingabe", "Bitte geben Sie den Secret-Key ein.");
        }
    }




    private void switchScene(ActionEvent event) {
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Zeigt eine Meldung in einem Infofenster an
     * @param title Titel des Fensters
     * @param message Nachricht
     */
    private void showInfoWindow(String title, String message){
        Alert window = new Alert(Alert.AlertType.INFORMATION);
        showWindow(title, message, window);
    }

    /**
     * Zeigt eine Meldung in einem Error fenster an
     * @param title Titel des Fensters
     * @param message Nachricht
     */
    private void showErrorWindow(String title, String message){
        Alert window = new Alert(Alert.AlertType.ERROR);
        showWindow(title, message, window);
    }

    private void showWarningWindow(String title, String message){
        Alert window = new Alert(Alert.AlertType.WARNING);
        showWindow(title, message, window);
    }

    private void showWindow(String title, String message, Alert window){
        window.setTitle(title);
        window.setHeaderText(null);
        window.setContentText( message );
        window.showAndWait();
    }
}