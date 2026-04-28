package com.bank.banking_app.ui;

import com.bank.banking_app.service.UserService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class SignupView {

    public static VBox create(Runnable onBackToLogin) {

        Label title = new Label("Create New Account");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        TextField fullNameField = new TextField();
        fullNameField.setPromptText("Full Name");
        fullNameField.setMaxWidth(300);

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setMaxWidth(300);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(300);

        Button createButton = new Button("Create Account");
        createButton.setPrefWidth(200);

        Button backButton = new Button("Back to Login");
        backButton.setPrefWidth(200);

        Label messageLabel = new Label();

        createButton.setOnAction(e -> {
            String fullName = fullNameField.getText();
            String username = usernameField.getText();
            String password = passwordField.getText();

            if (fullName.isBlank() || username.isBlank() || password.isBlank()) {
                messageLabel.setText("Please fill in all fields.");
                return;
            }

            UserService userService = new UserService();

            boolean success = userService.registerUser(fullName, username, password);

            if (success) {
                messageLabel.setText("Account created. Go back and log in.");
            } else {
                messageLabel.setText("Could not create account. Username may already exist.");
            }
        });

        backButton.setOnAction(e -> onBackToLogin.run());

        VBox layout = new VBox(15);
        layout.getChildren().addAll(
                title,
                fullNameField,
                usernameField,
                passwordField,
                createButton,
                backButton,
                messageLabel
        );

        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));

        return layout;
    }
}