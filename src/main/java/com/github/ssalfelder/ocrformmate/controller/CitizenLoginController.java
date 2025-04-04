package com.github.ssalfelder.ocrformmate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.ssalfelder.ocrformmate.OcrFormMateApp;
import com.github.ssalfelder.ocrformmate.auth.CitizenSessionHolder;
import com.github.ssalfelder.ocrformmate.model.User;
import com.github.ssalfelder.ocrformmate.ui.DialogHelper;
import com.github.ssalfelder.ocrformmate.ui.StyleHelper;
import javafx.application.Platform;
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
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;


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
        String email = citizenUsername.getText().trim();
        String password = citizenPassword.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            DialogHelper.showWarning("Fehlende Eingaben", "Bitte E-Mail-Adresse und Passwort eingeben.");
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        String json;

        try {
            Map<String, String> payload = Map.of(
                    "email", email,
                    "password", password
            );
            json = mapper.writeValueAsString(payload);
        } catch (Exception e) {
            DialogHelper.showError("Fehler", "Login Daten konnten nicht vorbereitet werden.");
            e.printStackTrace();
            return;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/user/validate"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpClient client = HttpClient.newHttpClient();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            User user = mapper.readValue(response.body(), User.class);

                            CitizenSessionHolder.setUser(user);

                            Platform.runLater(() -> {
                                Stage stage = (Stage) citizenUsername.getScene().getWindow();
                                stage.close();
                            });

                        } catch (Exception e) {
                            e.printStackTrace();
                            Platform.runLater(() ->
                                    DialogHelper.showError("Fehler", "Benutzerdaten konnten nicht gelesen werden.")
                            );
                        }
                    } else {
                        Platform.runLater(() ->
                                DialogHelper.showWarning("Login fehlgeschlagen", "E-Mail oder Passwort falsch.")
                        );
                    }
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    Platform.runLater(() ->
                            DialogHelper.showError("Verbindungsfehler", "Der Server ist nicht erreichbar.")
                    );
                    return null;
                });

    }

    @FXML
    protected void citizenResetClick(ActionEvent event) {
        citizenUsername.setText("");
        citizenPassword.setText("");
    }

}
