package com.github.ssalfelder.ocrformmate.controller;

import com.github.ssalfelder.ocrformmate.service.AdminService;
import com.github.ssalfelder.ocrformmate.ui.DialogHelper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LoginController {
    @FXML
    private TextField adminSecret;
    private final AdminService adminService;

    @Autowired
    public LoginController(AdminService adminService) {
        this.adminService = adminService;
    }

    @FXML
    protected void adminSecretClick(ActionEvent e) {
        String input = adminSecret.getText().trim();
        if (input.isEmpty()) {
            DialogHelper.showWarning("Fehlende Eingabe", "Bitte Secret-Key eingeben.");
            return;
        }
        boolean valid = adminService.isSecretValid(1, input);
        if (valid) DialogHelper.showInfo("Erfolg", "Zugang gewährt!");
        else DialogHelper.showError("Fehler", "Ungültiger Secret-Key");
    }

}

