package com.github.ssalfelder.ocrformmate.ui;

import javafx.scene.control.Alert;

public class DialogHelper {

    private DialogHelper() {
    }
    public static void showConfirmation(String title, String message) {
        show(Alert.AlertType.CONFIRMATION, title, message);
    }

    public static void showError(String title, String message) {
        show(Alert.AlertType.ERROR, title, message);
    }

    public static void showInfo(String title, String message) {
        show(Alert.AlertType.INFORMATION, title, message);
    }

    public static void showWarning(String title, String message) {
        show(Alert.AlertType.WARNING, title, message);
    }

    private static void show(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}