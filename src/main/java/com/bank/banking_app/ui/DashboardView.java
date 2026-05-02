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
        welcomeLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1f3c88;");

        Label checkingTitle = new Label("Checking Account");
        checkingTitle.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        Label checkingBalance = new Label("$" + checkingAmount);
        checkingBalance.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #0a7c3b;");

        VBox checkingBox = new VBox(8);
        checkingBox.getChildren().addAll(checkingTitle, checkingBalance);
        checkingBox.setAlignment(Pos.CENTER);
        checkingBox.setPadding(new Insets(22));
        checkingBox.setMaxWidth(350);
        checkingBox.setStyle("-fx-background-color: white; -fx-background-radius: 14; -fx-border-color: #d9d9d9; -fx-border-radius: 14;");

        Label savingsTitle = new Label("Savings Account");
        savingsTitle.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        Label savingsBalance = new Label("$" + savingsAmount);
        savingsBalance.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #0a7c3b;");

        VBox savingsBox = new VBox(8);
        savingsBox.getChildren().addAll(savingsTitle, savingsBalance);
        savingsBox.setAlignment(Pos.CENTER);
        savingsBox.setPadding(new Insets(22));
        savingsBox.setMaxWidth(350);
        savingsBox.setStyle("-fx-background-color: white; -fx-background-radius: 14; -fx-border-color: #d9d9d9; -fx-border-radius: 14;");

        Button transactionsButton = makeBlueButton("View Transactions");
        transactionsButton.setOnAction(e -> onTransactions.run());

        Button transferButton = makeBlueButton("Money Actions");
        transferButton.setOnAction(e -> onTransfer.run());

        Button requestsButton = makeBlueButton("View Requests");
        requestsButton.setOnAction(e -> onRequests.run());

        Button logoutButton = makeLightButton("Logout");
        logoutButton.setOnAction(e -> onLogout.run());

        VBox layout = new VBox(16);
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