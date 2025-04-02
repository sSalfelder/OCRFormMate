package com.github.ssalfelder.ocrformmate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ssalfelder.ocrformmate.OcrFormMateApp;
import com.github.ssalfelder.ocrformmate.ui.DialogHelper;
import com.github.ssalfelder.ocrformmate.ui.StyleHelper;
import jakarta.persistence.criteria.CriteriaBuilder;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextInputControl;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.github.ssalfelder.ocrformmate.validation.*;

import java.io.IOException;
import java.net.http.*;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
public class RegistrationCitizenController {

    @FXML private TextField firstnameField;
    @FXML private TextField lastnameField;
    @FXML private TextField postalCodeField;
    @FXML private TextField streetField;
    @FXML private TextField houseNumberField;
    @FXML private TextField cityField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    @Autowired
    private InputValidatorService inputValidator;

    @Autowired
    private PasswordValidatorService passwordValidator;


    @FXML
    public void handleRegisterClick() throws Exception {
        // Feldprüfung
        List<Pair<TextInputControl, String>> fields = List.of(
                new Pair<>(firstnameField, "Vornamen"),
                new Pair<>(lastnameField, "Nachnamen"),
                new Pair<>(postalCodeField, "Postleitzahl"),
                new Pair<>(streetField, "Straße"),
                new Pair<>(houseNumberField, "Hausnummer"),
                new Pair<>(cityField, "Ort"),
                new Pair<>(emailField, "E-Mail"),
                new Pair<>(passwordField, "Passwort")
        );

        if (!inputValidator.isValidFirstname(firstnameField.getText())) {
            StyleHelper.markError(firstnameField);
            DialogHelper.showWarning("Ungültiges Eingabeformat", "Bitte geben Sie einen gültigen Vornamen ein.");
            return;
        }

        if (!inputValidator.isValidLastname(lastnameField.getText())) {
            StyleHelper.markError(lastnameField);
            DialogHelper.showWarning("Ungültiges Eingabeformat", "Bitte geben Sie einen gültigen Nachnamen ein.");
            return;
        }

        if (!inputValidator.isValidStreet(streetField.getText())) {
            StyleHelper.markError(streetField);
            DialogHelper.showWarning("Ungültiges Eingabeformat", "Bitte geben Sie eine gültige Straße ein.");
            return;
        }

        if (!inputValidator.isValidHouseNumber(houseNumberField.getText())) {
            StyleHelper.markError(houseNumberField);
            DialogHelper.showWarning("Ungültiges Eingabeformat", "Bitte geben Sie eine gültige Hausnummer ein.");
            return;
        }

        if (!inputValidator.isValidPostalCode(postalCodeField.getText())) {
            StyleHelper.markError(postalCodeField);
            DialogHelper.showWarning("Ungültiges Eingabeformat", "Bitte geben Sie eine gültige Postleitzahl ein.");
            return;
        }

        if (!inputValidator.isValidCity(cityField.getText())) {
            StyleHelper.markError(cityField);
            DialogHelper.showWarning("Ungültiges Eingabeformat", "Bitte geben Sie einen gültigen Ortsnamen ein.");
            return;
        }

        if (!phoneField.getText().isEmpty()) {
            if (!inputValidator.isValidPhone(phoneField.getText())) {
                StyleHelper.markError(phoneField);
                DialogHelper.showWarning("Ungültiges Eingabeformat", "Bitte achten sie bei der Telefonnummer " +
                        "auf ein gültiges Eingabeformat.");
                return;
            }
        }

        if (!inputValidator.isValidEmail(emailField.getText())) {
            StyleHelper.markError(emailField);
            DialogHelper.showWarning("Ungültiges Eingabeformat", "Bitte geben Sie eine gültige Email-Adresse ein.");
            return;
        }

        PasswordValidationResult pwResult = passwordValidator.validatePassword(passwordField.getText());

        if (!pwResult.isValid()) {
            StyleHelper.markError(passwordField);
            String combinedErrors = String.join("\n", pwResult.getErrorMessages());
            DialogHelper.showWarning("Unsicheres Passwort", combinedErrors);
            return;
        }


        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            StyleHelper.markError(confirmPasswordField);
            DialogHelper.showError("Fehler", "Die Passwörter stimmen nicht überein.");
            return;
        }


        Map<String, String> payload = new HashMap<>();
        payload.put("firstname", firstnameField.getText());
        payload.put("lastname", lastnameField.getText());
        payload.put("postalCode", postalCodeField.getText());
        payload.put("street", streetField.getText());
        payload.put("houseNumber", houseNumberField.getText());
        payload.put("city", cityField.getText());
        payload.put("phoneNumber", phoneField.getText());
        payload.put("email", emailField.getText());
        payload.put("password", passwordField.getText());


        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/user/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 201) {
                        System.out.println("Registrierung erfolgreich!");

                        // Wechsel zurück zur Login-Seite
                        Platform.runLater(() -> {
                            DialogHelper.showInfo("Erfolg", "Registrierung abgeschlossen. Du wirst zum Login weitergeleitet.");

                            try {
                                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                                        "/com/github/ssalfelder/ocrformmate/fxml/citizen-login.fxml"));
                                loader.setControllerFactory(clazz -> OcrFormMateApp.getContext().getBean(clazz));
                                Parent loginRoot = loader.load();

                                Stage stage = (Stage) firstnameField.getScene().getWindow();
                                Scene scene = new Scene(loginRoot);
                                scene.getStylesheets().add(getClass().getResource("/CSS/style.css").toExternalForm());
                                stage.setScene(scene);
                                stage.setTitle("Login");
                                stage.show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });

                    } else {
                        Platform.runLater(() ->
                                DialogHelper.showError("Registrierung fehlgeschlagen",
                                "Serverantwort: " + response.body())
                        );
                        System.out.println("Serverantwort: " + response.body());

                    }
                });
    }

}
