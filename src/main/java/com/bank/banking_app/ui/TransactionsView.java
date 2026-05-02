package com.bank.banking_app.ui;

import com.bank.banking_app.service.UserService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.List;

public class TransactionsView {

    public static VBox create(String username, Runnable onBack) {
        Label title = new Label("Transaction History");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #1f3c88;");

        Label filterLabel = new Label("Filter transactions by month and year");
        filterLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555555;");

        ComboBox<String> monthBox = new ComboBox<>();
        monthBox.getItems().addAll(
                "All Months",
                "January", "February", "March", "April",
                "May", "June", "July", "August",
                "September", "October", "November", "December"
        );
        monthBox.setValue("All Months");
        monthBox.setMaxWidth(260);

        TextField yearField = new TextField();
        yearField.setPromptText("Year, example: 2026");
        yearField.setMaxWidth(260);
        yearField.setStyle("-fx-padding: 9; -fx-background-radius: 8;");

        Button searchButton = makeBlueButton("Search Transactions");
        Button clearButton = makeLightButton("Clear Filter");

        VBox transactionList = new VBox(10);
        transactionList.setAlignment(Pos.CENTER_LEFT);
        transactionList.setPadding(new Insets(10));

        ScrollPane scrollPane = new ScrollPane(transactionList);
        scrollPane.setFitToWidth(true);
        scrollPane.setMaxWidth(700);
        scrollPane.setMaxHeight(320);
        scrollPane.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        UserService userService = new UserService();

        loadTransactions(transactionList, userService.getTransactionHistory(username));

        searchButton.setOnAction(e -> {
            String selectedMonth = monthBox.getValue();
            String year = yearField.getText().trim();

            List<String> filteredTransactions =
                    userService.getTransactionsByMonthAndYear(username, selectedMonth, year);

            loadTransactions(transactionList, filteredTransactions);
        });

        clearButton.setOnAction(e -> {
            monthBox.setValue("All Months");
            yearField.clear();
            loadTransactions(transactionList, userService.getTransactionHistory(username));
        });

        Button backButton = makeLightButton("Back to Dashboard");
        backButton.setOnAction(e -> onBack.run());

        VBox card = new VBox(14);
        card.getChildren().addAll(
                title,
                filterLabel,
                monthBox,
                yearField,
                searchButton,
                clearButton,
                scrollPane,
                backButton
        );

        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(30));
        card.setMaxWidth(780);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-border-color: #d9d9d9; -fx-border-radius: 16;");

        VBox layout = new VBox(card);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));
        layout.setStyle("-fx-background-color: #f3f6fb;");

        return layout;
    }

    private static void loadTransactions(VBox transactionList, List<String> transactions) {
        transactionList.getChildren().clear();

        if (transactions.isEmpty()) {
            Label emptyLabel = new Label("No transactions found.");
            emptyLabel.setStyle("-fx-text-fill: #555555;");
            transactionList.getChildren().add(emptyLabel);
            return;
        }

        for (String transaction : transactions) {
            Label transactionLabel = new Label(transaction);
            transactionLabel.setWrapText(true);
            transactionLabel.setStyle(
                    "-fx-background-color: #f8f9fc;" +
                            "-fx-border-color: #d9d9d9;" +
                            "-fx-border-radius: 8;" +
                            "-fx-background-radius: 8;" +
                            "-fx-padding: 10;"
            );
            transactionList.getChildren().add(transactionLabel);
        }
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