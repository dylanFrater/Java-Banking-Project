package com.bank.banking_app.ui;

import com.bank.banking_app.service.UserService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public class LoginView {

    public static VBox create(Consumer<String> onSuccess, Runnable onSignup) {

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

        Button signupButton = new Button("Create Account");
        signupButton.setMaxWidth(250);

        Label message = new Label();

        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();

            UserService userService = new UserService();

            if (userService.loginUser(username, password)) {
                onSuccess.accept(username);
            } else {
                message.setText("Invalid username or password.");
            }
        });

        signupButton.setOnAction(e -> onSignup.run());

        VBox layout = new VBox(15);
        layout.getChildren().addAll(title, usernameField, passwordField, loginButton, signupButton, message);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));

        return layout;
    }
}