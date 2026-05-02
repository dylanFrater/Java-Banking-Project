package com.bank.banking_app.ui;

import com.bank.banking_app.service.UserService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;

public class TransferView {

    public static VBox create(String currentUsername, Runnable onBack) {
        Label title = new Label("Send or Request Money");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        TextField targetUserField = new TextField();
        targetUserField.setPromptText("Other user's username");
        targetUserField.setMaxWidth(300);

        TextField amountField = new TextField();
        amountField.setPromptText("Amount");
        amountField.setMaxWidth(300);

        Button sendButton = new Button("Send Money");
        sendButton.setPrefWidth(200);

        Button requestButton = new Button("Request Money");
        requestButton.setPrefWidth(200);

        Button backButton = new Button("Back to Dashboard");
        backButton.setPrefWidth(200);

        Label messageLabel = new Label();

        UserService userService = new UserService();

        sendButton.setOnAction(e -> {
            try {
                BigDecimal amount = new BigDecimal(amountField.getText().trim());
                boolean success = userService.sendMoney(currentUsername, targetUserField.getText().trim(), amount);

                messageLabel.setText(success ? "Money sent successfully." : "Send failed.");
            } catch (Exception ex) {
                messageLabel.setText("Invalid input.");
            }
        });

        requestButton.setOnAction(e -> {
            try {
                BigDecimal amount = new BigDecimal(amountField.getText().trim());
                boolean success = userService.requestMoney(currentUsername, targetUserField.getText().trim(), amount);

                messageLabel.setText(success ? "Request sent successfully." : "Request failed.");
            } catch (Exception ex) {
                messageLabel.setText("Invalid input.");
            }
        });

        backButton.setOnAction(e -> onBack.run());

        VBox layout = new VBox(15);
        layout.getChildren().addAll(
                title,
                targetUserField,
                amountField,
                sendButton,
                requestButton,
                messageLabel,
                backButton
        );

        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));

        return layout;
    }
}