package com.github.ssalfelder.ocrformmate.controller;

import com.github.ssalfelder.ocrformmate.OcrFormMateApp;
import com.github.ssalfelder.ocrformmate.auth.ClerkSessionHolder;
import com.github.ssalfelder.ocrformmate.model.Clerk;
import com.github.ssalfelder.ocrformmate.service.ClerkService;
import com.github.ssalfelder.ocrformmate.ui.DialogHelper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ClerkLoginController {

    @FXML
    private TextField secretField;

    private final ClerkService clerkService;

    @Autowired
    public ClerkLoginController(ClerkService clerkService) {
        this.clerkService = clerkService;
    }

    @FXML
    protected void handleLogin(ActionEvent event) {
        String input = secretField.getText().trim();

        try {
            Clerk clerk = clerkService.findBySecret(input);
            ClerkSessionHolder.setLoggedInClerk(clerk); // speichern
            DialogHelper.showInfo("Erfolg", "Willkommen, " + clerk.getFirstname());

            // Scene-Wechsel nach login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/.../clerk.fxml"));
            loader.setControllerFactory(clazz -> OcrFormMateApp.getContext().getBean(clazz));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            DialogHelper.showError("Fehler", "Ungültiges Secret.");
        }
    }
}
