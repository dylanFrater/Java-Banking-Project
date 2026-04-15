package com.bank.banking_app.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class TransferView {

    public static VBox create(Runnable onBack) {
        Label title = new Label("Transfer Money");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        ComboBox<String> fromAccountBox = new ComboBox<>();
        fromAccountBox.getItems().addAll("Checking", "Savings");
        fromAccountBox.setPromptText("From Account");
        fromAccountBox.setMaxWidth(250);

        ComboBox<String> toAccountBox = new ComboBox<>();
        toAccountBox.getItems().addAll("Checking", "Savings");
        toAccountBox.setPromptText("To Account");
        toAccountBox.setMaxWidth(250);

        TextField amountField = new TextField();
        amountField.setPromptText("Amount");
        amountField.setMaxWidth(250);

        Button submitButton = new Button("Submit Transfer");
        submitButton.setPrefWidth(180);

        Label messageLabel = new Label();

        submitButton.setOnAction(e -> {
            String fromAccount = fromAccountBox.getValue();
            String toAccount = toAccountBox.getValue();
            String amount = amountField.getText();

            if (fromAccount == null || toAccount == null || amount.isBlank()) {
                messageLabel.setText("Please fill in all fields.");
            } else if (fromAccount.equals(toAccount)) {
                messageLabel.setText("Accounts must be different.");
            } else {
                messageLabel.setText("Transfer submitted successfully.");
            }
        });

        Button backButton = new Button("Back to Dashboard");
        backButton.setPrefWidth(180);
        backButton.setOnAction(e -> onBack.run());

        VBox layout = new VBox(15);
        layout.getChildren().addAll(
                title,
                fromAccountBox,
                toAccountBox,
                amountField,
                submitButton,
                messageLabel,
                backButton
        );
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));

        return layout;
    }
}