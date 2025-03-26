package com.github.ssalfelder.ocrformmate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;

import java.net.http.*;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

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


    @FXML
    public void handleRegisterClick() throws Exception {
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
                    // ggf. Szenenwechsel oder Nachricht
                } else {
                    System.out.println("Fehler: " + response.body());
                }
            });
}
}
