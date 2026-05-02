package com.bank.banking_app.ui;

import com.bank.banking_app.service.UserService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;

public class TransferView {

    public static VBox create(String username, Runnable onBack) {

        Label title = new Label("Money Actions");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #1f3c88;");

        Label sendRequestLabel = new Label("Send or Request Money");
        sendRequestLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TextField targetUserField = new TextField();
        targetUserField.setPromptText("Other user's username");
        targetUserField.setMaxWidth(300);
        targetUserField.setStyle("-fx-padding: 9; -fx-background-radius: 8;");

        TextField amountField = new TextField();
        amountField.setPromptText("Amount");
        amountField.setMaxWidth(300);
        amountField.setStyle("-fx-padding: 9; -fx-background-radius: 8;");

        Button sendButton = makeBlueButton("Send Money");
        Button requestButton = makeLightButton("Request Money");

        Label internalLabel = new Label("Transfer Between My Accounts");
        internalLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        ComboBox<String> fromAccountBox = new ComboBox<>();
        fromAccountBox.getItems().addAll("Checking", "Savings");
        fromAccountBox.setPromptText("From Account");
        fromAccountBox.setMaxWidth(300);

        ComboBox<String> toAccountBox = new ComboBox<>();
        toAccountBox.getItems().addAll("Checking", "Savings");
        toAccountBox.setPromptText("To Account");
        toAccountBox.setMaxWidth(300);

        TextField internalAmountField = new TextField();
        internalAmountField.setPromptText("Amount");
        internalAmountField.setMaxWidth(300);
        internalAmountField.setStyle("-fx-padding: 9; -fx-background-radius: 8;");

        Button internalTransferButton = makeBlueButton("Transfer Between Accounts");

        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-font-weight: bold;");

        Button backButton = makeLightButton("Back to Dashboard");

        UserService service = new UserService();

        sendButton.setOnAction(e -> {
            try {
                String targetUser = targetUserField.getText().trim();
                BigDecimal amount = new BigDecimal(amountField.getText().trim());

                boolean success = service.sendMoney(username, targetUser, amount);

                if (success) {
                    messageLabel.setText("Money sent successfully.");
                    targetUserField.clear();
                    amountField.clear();
                } else {
                    messageLabel.setText("Send failed. Check username, balance, or amount.");
                }

            } catch (Exception ex) {
                messageLabel.setText("Invalid send amount.");
            }
        });

        requestButton.setOnAction(e -> {
            try {
                String targetUser = targetUserField.getText().trim();
                BigDecimal amount = new BigDecimal(amountField.getText().trim());

                boolean success = service.requestMoney(username, targetUser, amount);

                if (success) {
                    messageLabel.setText("Money request sent successfully.");
                    targetUserField.clear();
                    amountField.clear();
                } else {
                    messageLabel.setText("Request failed. Check username or amount.");
                }

            } catch (Exception ex) {
                messageLabel.setText("Invalid request amount.");
            }
        });

        internalTransferButton.setOnAction(e -> {
            try {
                String fromAccount = fromAccountBox.getValue();
                String toAccount = toAccountBox.getValue();
                BigDecimal amount = new BigDecimal(internalAmountField.getText().trim());

                if (fromAccount == null || toAccount == null) {
                    messageLabel.setText("Choose both accounts.");
                    return;
                }

                boolean success = service.transferBetweenAccounts(username, fromAccount, toAccount, amount);

                if (success) {
                    messageLabel.setText("Internal transfer completed.");
                    fromAccountBox.setValue(null);
                    toAccountBox.setValue(null);
                    internalAmountField.clear();
                } else {
                    messageLabel.setText("Internal transfer failed.");
                }

            } catch (Exception ex) {
                messageLabel.setText("Invalid internal transfer amount.");
            }
        });

        backButton.setOnAction(e -> onBack.run());

        VBox card = new VBox(12);
        card.getChildren().addAll(
                title,
                sendRequestLabel,
                targetUserField,
                amountField,
                sendButton,
                requestButton,
                internalLabel,
                fromAccountBox,
                toAccountBox,
                internalAmountField,
                internalTransferButton,
                messageLabel,
                backButton
        );

        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(30));
        card.setMaxWidth(420);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-border-color: #d9d9d9; -fx-border-radius: 16;");

        VBox layout = new VBox(card);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));
        layout.setStyle("-fx-background-color: #f3f6fb;");

        return layout;
    }

    private static Button makeBlueButton(String text) {
        Button button = new Button(text);
        button.setPrefWidth(230);
        button.setStyle("-fx-background-color: #1f3c88; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");
        return button;
    }

    private static Button makeLightButton(String text) {
        Button button = new Button(text);
        button.setPrefWidth(230);
        button.setStyle("-fx-background-color: #e8ecf7; -fx-text-fill: #1f3c88; -fx-font-weight: bold; -fx-background-radius: 8;");
        return button;
    }
}