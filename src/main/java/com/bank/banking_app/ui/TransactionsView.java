package com.bank.banking_app.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class TransactionsView {

    public static VBox create(Runnable onBack) {
        Label title = new Label("Transaction History");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label transaction1 = new Label("Deposit - $500.00");
        Label transaction2 = new Label("Withdraw - $50.00");
        Label transaction3 = new Label("Transfer - $25.00");
        Label transaction4 = new Label("Deposit - $100.00");

        Button backButton = new Button("Back to Dashboard");
        backButton.setPrefWidth(180);
        backButton.setOnAction(e -> onBack.run());

        VBox layout = new VBox(15);
        layout.getChildren().addAll(
                title,
                transaction1,
                transaction2,
                transaction3,
                transaction4,
                backButton
        );
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));

        return layout;
    }
}