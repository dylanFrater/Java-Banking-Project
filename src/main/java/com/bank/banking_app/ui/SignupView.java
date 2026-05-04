package com.bank.banking_app.ui;

import com.bank.banking_app.service.UserService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

/*
 This is the signup screen.
 It creates a new account
 after basic validation.
*/
public class SignupView {

    public static VBox create(Runnable onBackToLogin) {

        Label title = new Label("Create Account");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #543100;");

        Label subtitle = new Label("Enter your information below");
        subtitle.setStyle("-fx-font-size: 15px; -fx-text-fill: #874f00;");

        TextField fullNameField = new TextField();
        fullNameField.setPromptText("Full Name");
        fullNameField.setMaxWidth(300);
        fullNameField.setStyle("-fx-padding: 10; -fx-background-radius: 8;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setMaxWidth(300);
        usernameField.setStyle("-fx-padding: 10; -fx-background-radius: 8;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(300);
        passwordField.setStyle("-fx-padding: 10; -fx-background-radius: 8;");

        Button createButton = makeBlueButton("Create Account");
        Button backButton = makeLightButton("Back to Login");

        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-font-weight: bold;");

        createButton.setOnAction(e -> {
            String fullName = fullNameField.getText().trim();
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();

            UserService userService = new UserService();
            String validationMessage = userService.validateRegistrationInput(fullName, username, password);

            if (validationMessage != null) {
                messageLabel.setText(validationMessage);
                return;
            }

            boolean success = userService.registerUser(fullName, username, password);

            if (success) {
                messageLabel.setText("Account created. You can log in now.");
                fullNameField.clear();
                usernameField.clear();
                passwordField.clear();
            } else {
                messageLabel.setText("Could not create the account.");
            }
        });

        backButton.setOnAction(e -> onBackToLogin.run());

        VBox card = new VBox(15);
        card.getChildren().addAll(
                title,
                subtitle,
                fullNameField,
                usernameField,
                passwordField,
                createButton,
                backButton,
                messageLabel
        );

        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(35));
        card.setMaxWidth(420);
        card.setStyle("-fx-background-color: #f7f5f1; -fx-background-radius: 16; -fx-border-color: #ffb854; -fx-border-radius: 16;");

        VBox layout = new VBox(card);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(40));
        layout.setStyle("-fx-background-color: #fff1d6;");

        return layout;
    }

    private static Button makeBlueButton(String text) {
        Button button = new Button(text);
        button.setPrefWidth(230);
        button.setStyle("-fx-background-color: #ed8b00; -fx-text-fill: #2b1700; -fx-font-weight: bold; -fx-background-radius: 8;");
        return button;
    }

    private static Button makeLightButton(String text) {
        Button button = new Button(text);
        button.setPrefWidth(230);
        button.setStyle("-fx-background-color: #ffcd87; -fx-text-fill: #543100; -fx-font-weight: bold; -fx-background-radius: 8;");
        return button;
    }
}
