package com.bank.banking_app.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.util.List;

public class TransactionsView {

    public static VBox create(List<String> transactions, Runnable onBack) {
        Label title = new Label("Transaction History");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        VBox transactionList = new VBox(10);
        transactionList.setAlignment(Pos.CENTER_LEFT);

        if (transactions.isEmpty()) {
            transactionList.getChildren().add(new Label("No transactions found."));
        } else {
            for (String transaction : transactions) {
                Label transactionLabel = new Label(transaction);
                transactionLabel.setStyle("-fx-border-color: lightgray; -fx-padding: 8;");
                transactionList.getChildren().add(transactionLabel);
            }
        }

        ScrollPane scrollPane = new ScrollPane(transactionList);
        scrollPane.setFitToWidth(true);
        scrollPane.setMaxWidth(650);
        scrollPane.setMaxHeight(350);

        Button backButton = new Button("Back to Dashboard");
        backButton.setPrefWidth(180);
        backButton.setOnAction(e -> onBack.run());

        VBox layout = new VBox(15);
        layout.getChildren().addAll(title, scrollPane, backButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));

        return layout;
    }
}