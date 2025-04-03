package com.github.ssalfelder.ocrformmate.controller;

import com.github.ssalfelder.ocrformmate.OcrFormMateApp;
import com.github.ssalfelder.ocrformmate.ui.StyleHelper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
public class CitizenLoginController {

    //TODO Login Untätigkeit bei falscher Dateineingabe beheben
    @FXML
    private TextField citizenUsername;
    @FXML
    private TextField citizenPassword;

    @FXML
    protected void goBack(ActionEvent event) {
        // Fenster (Stage) über das Event schließen
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    protected void registrationCitizen(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/github/ssalfelder/ocrformmate/" +
                "fxml/citizen-registration.fxml"));
        loader.setControllerFactory(clazz -> OcrFormMateApp.getContext().getBean(clazz));
        Parent registrationRoot = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        Scene scene = (new Scene(registrationRoot));
        scene.getStylesheets().add(getClass().getResource("/CSS/registration.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Registrierung");
        stage.show();
    }

    @FXML
    protected void citizenLoginClick(ActionEvent event) {

    }

    @FXML
    protected void citizenResetClick(ActionEvent event) {
        citizenUsername.setText("");
        citizenPassword.setText("");
    }

}
