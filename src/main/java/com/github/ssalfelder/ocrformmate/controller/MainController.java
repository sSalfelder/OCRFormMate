package com.github.ssalfelder.ocrformmate.controller;

import com.github.ssalfelder.ocrformmate.OcrFormMateApp;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class MainController {
    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    protected void citizenClick(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/github/ssalfelder/ocrformmate/fxml/citizen.fxml"));
        loader.setControllerFactory(clazz -> OcrFormMateApp.getContext().getBean(clazz));
        root = loader.load();
        switchScene(event);
    }

    @FXML
    protected void clerkClick(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/github/ssalfelder/ocrformmate/fxml/clerk-login.fxml"));
        loader.setControllerFactory(clazz -> OcrFormMateApp.getContext().getBean(clazz));
        root = loader.load();
        switchScene(event);
    }

    @FXML
    protected void goBack(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/github/ssalfelder/ocrformmate/fxml/main.fxml"));
        loader.setControllerFactory(clazz -> OcrFormMateApp.getContext().getBean(clazz));
        root = loader.load();
        switchScene(event);
    }

    @FXML
    protected void registrationClerk(ActionEvent event) throws  IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/github/ssalfelder/ocrformmate/fxml/admin-password-dialog.fxml"));
        loader.setControllerFactory(clazz -> OcrFormMateApp.getContext().getBean(clazz));
        root = loader.load();
        switchScene(event);
    }
    @FXML
    protected void registrationCitizen(ActionEvent event) throws  IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/github/ssalfelder/ocrformmate/fxml/registration-citizen-.fxml"));
        loader.setControllerFactory(clazz -> OcrFormMateApp.getContext().getBean(clazz));
        root = loader.load();
        switchScene(event);
    }

    @FXML
    protected void clerkLoginClick() {

    }

    @FXML
    protected void clerkResetClick() {

    }

    private void switchScene(ActionEvent event) {
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}