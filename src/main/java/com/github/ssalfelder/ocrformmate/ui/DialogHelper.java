package com.github.ssalfelder.ocrformmate.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.util.Pair;

import java.util.List;
import java.util.Map;

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

    public static boolean validateRequiredFields(List<Pair<TextInputControl, String>> fields) {
        int counter = 0;
        String value = "";

        for (Pair<TextInputControl, String> pair : fields) {
            TextInputControl control = pair.getKey();
            String fieldName = pair.getValue();

            //Vorherige Fehler entfernen
            control.getStyleClass().remove("error-border");

            if (control.getText().isBlank()) {
                counter++;
                value = fieldName;

                StyleHelper.markError(control);
            }

        }
        if (counter == 1) {
            showWarning("Fehlende Eingabe", "Bitte " + value + " eingeben.");
            return false;
        } else if (counter > 0) {
            showWarning("Fehlende Eingaben", "Bitte die nicht optionalen Felder ausfüllen.");
            return false;
        }
        return true;
    }
}