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
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        ComboBox<String> monthBox = new ComboBox<>();
        monthBox.getItems().addAll(
                "All Months",
                "January",
                "February",
                "March",
                "April",
                "May",
                "June",
                "July",
                "August",
                "September",
                "October",
                "November",
                "December"
        );
        monthBox.setValue("All Months");
        monthBox.setMaxWidth(250);

        TextField yearField = new TextField();
        yearField.setPromptText("Year, example: 2026");
        yearField.setMaxWidth(250);

        Button searchButton = new Button("Search Transactions");
        searchButton.setPrefWidth(180);

        Button clearButton = new Button("Clear Filter");
        clearButton.setPrefWidth(180);

        VBox transactionList = new VBox(10);
        transactionList.setAlignment(Pos.CENTER_LEFT);

        ScrollPane scrollPane = new ScrollPane(transactionList);
        scrollPane.setFitToWidth(true);
        scrollPane.setMaxWidth(700);
        scrollPane.setMaxHeight(350);

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

        Button backButton = new Button("Back to Dashboard");
        backButton.setPrefWidth(180);
        backButton.setOnAction(e -> onBack.run());

        VBox layout = new VBox(15);
        layout.getChildren().addAll(
                title,
                monthBox,
                yearField,
                searchButton,
                clearButton,
                scrollPane,
                backButton
        );

        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));

        return layout;
    }

    private static void loadTransactions(VBox transactionList, List<String> transactions) {
        transactionList.getChildren().clear();

        if (transactions.isEmpty()) {
            transactionList.getChildren().add(new Label("No transactions found."));
            return;
        }

        for (String transaction : transactions) {
            Label transactionLabel = new Label(transaction);
            transactionLabel.setStyle("-fx-border-color: lightgray; -fx-padding: 8;");
            transactionList.getChildren().add(transactionLabel);
        }
    }
}