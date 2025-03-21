package com.github.ssalfelder.ocrformmate;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;

@SpringBootApplication
public class OcrFormMateApp extends Application {

    private ConfigurableApplicationContext springContext;

    @Override
    public void init() throws Exception {

        // Spring Boot-Kontext starten
        springContext = SpringApplication.run(OcrFormMateApp.class);

    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                OcrFormMateApp.class.getResource("/com/github/ssalfelder/ocrformmate/fxml/main.fxml"));

        Scene scene = new Scene(fxmlLoader.load(), 650, 151);
        stage.setTitle("OCRFormMate");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        // Beim Beenden JavaFX und Spring Boot sauber herunterfahren
        springContext.close();
        Platform.exit();
    }

    public static void main(String[] args) {
        // JavaFX-Start (ruft später init() und start() auf)
        launch();
    }
}