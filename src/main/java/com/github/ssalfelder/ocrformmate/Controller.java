package com.github.ssalfelder.ocrformmate;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Controller {
    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    protected void citizenClick(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("citizen.fxml"));
        switchScene(event);
    }
    @FXML
    protected void clerkClick(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("admin.fxml"));
        switchScene(event);
    }
    @FXML
    protected void goBack(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("main.fxml"));
        switchScene(event);
    }

    @FXML
    protected void adminLoginClick() {

    }

    @FXML
    protected void adminResetClick() {

    }

    private void switchScene(ActionEvent event) {
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}