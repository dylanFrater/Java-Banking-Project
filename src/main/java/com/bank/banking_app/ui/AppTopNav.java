package com.bank.banking_app.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/*
 This builds the top navigation
 used on the main screens.
*/
public class AppTopNav {

    public static final String DASHBOARD = "dashboard";
    public static final String TRANSACTIONS = "transactions";
    public static final String GOALS = "goals";
    public static final String TRANSFER = "transfer";
    public static final String CARD_SIM = "card_sim";

    /*
     This makes the tab row
     and highlights the active page.
    */
    public static HBox create(String activeTab,
                              Runnable onDashboard,
                              Runnable onTransactions,
                              Runnable onGoals,
                              Runnable onTransfer,
                              Runnable onProfile,
                              Runnable onLogout) {
        Button dashboardButton = createTabButton("Dashboard", DASHBOARD.equals(activeTab));
        Button transactionsButton = createTabButton("View History & Transactions", TRANSACTIONS.equals(activeTab));
        Button goalsButton = createTabButton("Savings Goals", GOALS.equals(activeTab));
        Button transferButton = createTabButton("Pay & Transfer", TRANSFER.equals(activeTab));
        Button cardSimButton = createTabButton("Card Sim", CARD_SIM.equals(activeTab));

        dashboardButton.setOnAction(e -> onDashboard.run());
        transactionsButton.setOnAction(e -> onTransactions.run());
        goalsButton.setOnAction(e -> onGoals.run());
        transferButton.setOnAction(e -> onTransfer.run());

        Button profileButton = createUtilityButton("Profile");
        profileButton.setOnAction(e -> onProfile.run());

        Button logoutButton = createUtilityButton("Logout");
        logoutButton.setOnAction(e -> onLogout.run());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox nav = new HBox(10);
        nav.getChildren().addAll(
                dashboardButton,
                transactionsButton,
                goalsButton,
                transferButton,
                cardSimButton,
                spacer,
                profileButton,
                logoutButton
        );
        nav.setAlignment(Pos.CENTER_LEFT);
        nav.setPadding(new Insets(16, 24, 16, 24));
        nav.setStyle(
                "-fx-background-color: #f7f5f1;" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-color: #ffb854;" +
                        "-fx-border-radius: 18;"
        );
        return nav;
    }

    private static Button createTabButton(String text, boolean active) {
        Button button = new Button(text);
        button.setStyle(
                active
                        ? "-fx-background-color: #ed8b00; -fx-text-fill: #2b1700; -fx-font-weight: bold; -fx-background-radius: 10;"
                        : "-fx-background-color: #ffe1b0; -fx-text-fill: #7a4700; -fx-font-weight: bold; -fx-background-radius: 10;"
        );
        return button;
    }

    private static Button createUtilityButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: #ffcd87; -fx-text-fill: #543100; -fx-font-weight: bold; -fx-background-radius: 10;");
        return button;
    }
}
