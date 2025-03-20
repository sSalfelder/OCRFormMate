package com.github.ssalfelder.ocrformmate;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Application extends javafx.application.Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("/com/github/ssalfelder/ocrformmate/fxml/main.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 650, 151);
        stage.setTitle("OCRFormMate");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}