package com.bank.banking_app.ui;

import com.bank.banking_app.service.UserService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

/*
 This is the login screen.
 It checks the account and then
 sends the user into the app.
*/
public class LoginView {

    public static VBox create(Consumer<String> onSuccess, Runnable onSignup) {

        Label title = new Label("Bank Of Java");
        title.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #543100;");

        Label subtitle = new Label("Login to your account");
        subtitle.setStyle("-fx-font-size: 15px; -fx-text-fill: #874f00;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setMaxWidth(280);
        usernameField.setStyle("-fx-padding: 10; -fx-background-radius: 8;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(280);
        passwordField.setStyle("-fx-padding: 10; -fx-background-radius: 8;");

        Button loginButton = new Button("Login");
        loginButton.setPrefWidth(220);
        loginButton.setStyle("-fx-background-color: #ed8b00; -fx-text-fill: #2b1700; -fx-font-weight: bold; -fx-background-radius: 8;");

        Button signupButton = new Button("Create Account");
        signupButton.setPrefWidth(220);
        signupButton.setStyle("-fx-background-color: #ffcd87; -fx-text-fill: #543100; -fx-font-weight: bold; -fx-background-radius: 8;");

        Label message = new Label();
        message.setStyle("-fx-text-fill: #9a3412; -fx-font-weight: bold;");

        loginButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();

            UserService userService = new UserService();
            String validationMessage = userService.validateLoginInput(username, password);

            if (validationMessage != null) {
                message.setText(validationMessage);
                return;
            }

            if (userService.loginUser(username, password)) {
                onSuccess.accept(username);
            } else {
                message.setText("Invalid username or password.");
            }
        });

        signupButton.setOnAction(e -> onSignup.run());

        VBox card = new VBox(15);
        card.getChildren().addAll(title, subtitle, usernameField, passwordField, loginButton, signupButton, message);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(35));
        card.setMaxWidth(380);
        card.setStyle("-fx-background-color: #f7f5f1; -fx-background-radius: 16; -fx-border-color: #ffb854; -fx-border-radius: 16;");

        VBox layout = new VBox(card);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(40));
        layout.setStyle("-fx-background-color: #fff1d6;");

        return layout;
    }
}
