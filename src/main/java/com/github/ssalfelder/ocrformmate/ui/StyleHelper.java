package com.github.ssalfelder.ocrformmate.ui;

import javafx.scene.control.TextInputControl;

public class StyleHelper {

    public static void markError(TextInputControl control) {
        if (!control.getStyleClass().contains("error-border")) {
            control.getStyleClass().add("error-border");
        }

        control.textProperty().addListener((obs, oldVal, newVal) ->
                control.getStyleClass().remove("error-border"));
    }
}
