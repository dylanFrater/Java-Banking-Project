package com.bank.banking_app.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainUI extends Application {

    @Override
    public void start(Stage stage) {

        Label title = new Label("Banking App Login");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setMaxWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(250);

        Button loginButton = new Button("Login");
        loginButton.setMaxWidth(250);

        Label message = new Label();

        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();

            if (username.equals("admin") && password.equals("1234")) {
                message.setText("Login successful!");
            } else {
                message.setText("Invalid username or password.");
            }
        });

        VBox layout = new VBox(15);
        layout.getChildren().addAll(title, usernameField, passwordField, loginButton, message);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));

        Scene scene = new Scene(layout, 500, 400);

        stage.setTitle("Banking App");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}