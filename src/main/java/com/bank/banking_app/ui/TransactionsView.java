package com.bank.banking_app.ui;

import com.bank.banking_app.service.UserService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

/*
 This screen shows all transactions.
 The user can also filter them
 by month and year.
*/
public class TransactionsView {

    public static VBox create(String username,
                              Runnable onDashboard,
                              Runnable onTransactions,
                              Runnable onSavingsGoals,
                              Runnable onTransfer,
                              Runnable onProfile,
                              Runnable onLogout) {
        HBox navBar = AppTopNav.create(
                AppTopNav.TRANSACTIONS,
                onDashboard,
                onTransactions,
                onSavingsGoals,
                onTransfer,
                onProfile,
                onLogout
        );

        Label title = new Label("History & Transactions");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #543100;");

        Label filterLabel = new Label("Filter transactions by month and year");
        filterLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #874f00;");

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
        scrollPane.setMaxWidth(760);
        scrollPane.setMaxHeight(380);
        scrollPane.setStyle("-fx-background-color: transparent;");

        UserService userService = new UserService();
        loadTransactions(transactionList, userService.getTransactionHistory(username));

        searchButton.setOnAction(e -> {
            String selectedMonth = monthBox.getValue();
            String year = yearField.getText().trim();
            loadTransactions(transactionList, userService.getTransactionsByMonthAndYear(username, selectedMonth, year));
        });

        clearButton.setOnAction(e -> {
            monthBox.setValue("All Months");
            yearField.clear();
            loadTransactions(transactionList, userService.getTransactionHistory(username));
        });

        VBox card = new VBox(14);
        card.getChildren().addAll(
                title,
                filterLabel,
                monthBox,
                yearField,
                searchButton,
                clearButton,
                scrollPane
        );
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
                    "-fx-background-color: #fff3de;" +
                            "-fx-border-color: #ffb854;" +
                            "-fx-border-radius: 8;" +
                            "-fx-background-radius: 8;" +
                            "-fx-padding: 10;" +
                            "-fx-text-fill: #543100;"
            );
            transactionList.getChildren().add(transactionLabel);
        }
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
}
