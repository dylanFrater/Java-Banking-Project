package com.bank.banking_app.ui;

import com.bank.banking_app.service.UserService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;

/*
 This screen handles sending money,
 requesting money, and transfers
 between checking and savings.
*/
public class TransferView {

    public static VBox create(String username,
                              Runnable onDashboard,
                              Runnable onTransactions,
                              Runnable onSavingsGoals,
                              Runnable onTransfer,
                              Runnable onProfile,
                              Runnable onLogout) {

        HBox navBar = AppTopNav.create(
                AppTopNav.TRANSFER,
                onDashboard,
                onTransactions,
                onSavingsGoals,
                onTransfer,
                onProfile,
                onLogout
        );

        Label title = new Label("Pay & Transfer");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #543100;");

        Label sendRequestLabel = new Label("Send or request money");
        sendRequestLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #874f00;");

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

        Label internalLabel = new Label("Transfer between my accounts");
        internalLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #874f00;");

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

        UserService service = new UserService();

        sendButton.setOnAction(e -> {
            try {
                String targetUser = targetUserField.getText().trim();
                BigDecimal amount = new BigDecimal(amountField.getText().trim());

                if (targetUser.isBlank()) {
                    showError("Missing username", "Enter the username of the person you want to pay.");
                    return;
                }

                boolean success = service.sendMoney(username, targetUser, amount);

                if (!success) {
                    showError("Payment could not be sent", "Check the username, balance, and amount, then try again.");
                    return;
                }

                targetUserField.clear();
                amountField.clear();
                showInfo("Payment sent", "Your money was sent successfully.");
            } catch (Exception ex) {
                showError("Invalid amount", "Enter a valid amount before sending money.");
            }
        });

        requestButton.setOnAction(e -> {
            try {
                String targetUser = targetUserField.getText().trim();
                BigDecimal amount = new BigDecimal(amountField.getText().trim());

                if (targetUser.isBlank()) {
                    showError("Missing username", "Enter the username of the person you want to request from.");
                    return;
                }

                boolean success = service.requestMoney(username, targetUser, amount);

                if (!success) {
                    showError("Request could not be sent", "Check the username and amount, then try again.");
                    return;
                }

                targetUserField.clear();
                amountField.clear();
                showInfo("Request sent", "Your money request was sent successfully.");
            } catch (Exception ex) {
                showError("Invalid amount", "Enter a valid amount before sending the request.");
            }
        });

        internalTransferButton.setOnAction(e -> {
            try {
                String fromAccount = fromAccountBox.getValue();
                String toAccount = toAccountBox.getValue();
                BigDecimal amount = new BigDecimal(internalAmountField.getText().trim());

                if (fromAccount == null || toAccount == null) {
                    showError("Missing account selection", "Choose both the source and destination accounts.");
                    return;
                }

                if (fromAccount.equals(toAccount)) {
                    showError("Same account selected", "Choose two different accounts for the transfer.");
                    return;
                }

                boolean success = service.transferBetweenAccounts(username, fromAccount, toAccount, amount);
                if (!success) {
                    showError("Transfer could not be completed", "Check the amount and available balance, then try again.");
                    return;
                }

                fromAccountBox.setValue(null);
                toAccountBox.setValue(null);
                internalAmountField.clear();
                showInfo("Transfer completed", "Your money was moved between accounts.");
            } catch (Exception ex) {
                showError("Invalid amount", "Enter a valid amount before transferring money.");
            }
        });

        VBox sendRequestColumn = new VBox(12);
        sendRequestColumn.getChildren().addAll(
                sendRequestLabel,
                targetUserField,
                amountField,
                sendButton,
                requestButton
        );
        sendRequestColumn.setAlignment(Pos.TOP_CENTER);
        sendRequestColumn.setPadding(new Insets(20));
        sendRequestColumn.setPrefWidth(340);
        sendRequestColumn.setStyle("-fx-background-color: #fff3de; -fx-background-radius: 14; -fx-border-color: #ffb854; -fx-border-radius: 14;");

        VBox internalColumn = new VBox(12);
        internalColumn.getChildren().addAll(
                internalLabel,
                fromAccountBox,
                toAccountBox,
                internalAmountField,
                internalTransferButton
        );
        internalColumn.setAlignment(Pos.TOP_CENTER);
        internalColumn.setPadding(new Insets(20));
        internalColumn.setPrefWidth(340);
        internalColumn.setStyle("-fx-background-color: #fff3de; -fx-background-radius: 14; -fx-border-color: #ffb854; -fx-border-radius: 14;");

        HBox actionRow = new HBox(18);
        actionRow.getChildren().addAll(sendRequestColumn, internalColumn);
        actionRow.setAlignment(Pos.TOP_CENTER);

        VBox card = new VBox(18);
        card.getChildren().addAll(title, actionRow);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(30));
        card.setMaxWidth(840);
        card.setStyle("-fx-background-color: #f7f5f1; -fx-background-radius: 16; -fx-border-color: #ffb854; -fx-border-radius: 16;");

        VBox layout = new VBox(18);
        layout.getChildren().addAll(navBar, card);
        layout.setAlignment(Pos.TOP_CENTER);
        layout.setPadding(new Insets(24));
        layout.setStyle("-fx-background-color: #fff1d6;");
        return layout;
    }

    private static Button makeBlueButton(String text) {
        Button button = new Button(text);
        button.setPrefWidth(230);
        button.setStyle("-fx-background-color: #ed8b00; -fx-text-fill: #2b1700; -fx-font-weight: bold; -fx-background-radius: 10;");
        return button;
    }

    private static Button makeLightButton(String text) {
        Button button = new Button(text);
        button.setPrefWidth(230);
        button.setStyle("-fx-background-color: #ffcd87; -fx-text-fill: #543100; -fx-font-weight: bold; -fx-background-radius: 10;");
        return button;
    }

    private static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText(title);
        alert.showAndWait();
    }

    private static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setHeaderText(title);
        alert.showAndWait();
    }
}
