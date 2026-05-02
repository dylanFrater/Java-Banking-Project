package com.bank.banking_app.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class DashboardView {

    public static VBox create(String fullName,
                              String checkingAmount,
                              String savingsAmount,
                              Runnable onTransactions,
                              Runnable onTransfer,
                              Runnable onRequests,
                              Runnable onLogout) {

        Label welcomeLabel = new Label("Welcome to your banking dashboard, " + fullName);
        welcomeLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        Label checkingTitle = new Label("Checking Account");
        checkingTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label checkingBalance = new Label("Balance: $" + checkingAmount);

        VBox checkingBox = new VBox(8);
        checkingBox.getChildren().addAll(checkingTitle, checkingBalance);
        checkingBox.setAlignment(Pos.CENTER);
        checkingBox.setPadding(new Insets(20));
        checkingBox.setStyle("-fx-border-color: lightgray; -fx-border-radius: 8;");

        Label savingsTitle = new Label("Savings Account");
        savingsTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label savingsBalance = new Label("Balance: $" + savingsAmount);

        VBox savingsBox = new VBox(8);
        savingsBox.getChildren().addAll(savingsTitle, savingsBalance);
        savingsBox.setAlignment(Pos.CENTER);
        savingsBox.setPadding(new Insets(20));
        savingsBox.setStyle("-fx-border-color: lightgray; -fx-border-radius: 8;");

        Button transactionsButton = new Button("View Transactions");
        transactionsButton.setPrefWidth(220);
        transactionsButton.setOnAction(e -> onTransactions.run());

        Button transferButton = new Button("Money Actions");
        transferButton.setPrefWidth(220);
        transferButton.setOnAction(e -> onTransfer.run());

        Button requestsButton = new Button("View Requests");
        requestsButton.setPrefWidth(220);
        requestsButton.setOnAction(e -> onRequests.run());

        Button logoutButton = new Button("Logout");
        logoutButton.setPrefWidth(220);
        logoutButton.setOnAction(e -> onLogout.run());

        VBox layout = new VBox(18);
        layout.getChildren().addAll(
                welcomeLabel,
                checkingBox,
                savingsBox,
                transactionsButton,
                transferButton,
                requestsButton,
                logoutButton
        );

        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));

        return layout;
    }
}