package com.github.ssalfelder.ocrformmate.controller;

import com.github.ssalfelder.ocrformmate.OcrFormMateApp;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
public class CitizenLoginController {

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
        Parent loginRoot = loader.load();

        Stage stage = new Stage();
        stage.setScene(new Scene(loginRoot));
        stage.setTitle("Registrierung");
        stage.initModality(Modality.APPLICATION_MODAL); // blockiert andere Fenster
        stage.showAndWait();
    }

    @FXML
    protected void citizenLoginClick(ActionEvent event) {

    }

    @FXML
    protected void citizenResetClick(ActionEvent event) {

    }

}
