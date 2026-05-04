package com.bank.banking_app.ui;

import com.bank.banking_app.service.UserService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;

/*
 This creates the dashboard screen.
 The balances are meant to be
 the main focus of the page.
*/
public class DashboardView {

    public static VBox create(String username,
                              String fullName,
                              String checkingAmount,
                              String savingsAmount,
                              List<String> recentTransactions,
                              Runnable onDashboard,
                              Runnable onTransactions,
                              Runnable onSavingsGoals,
                              Runnable onTransfer,
                              Runnable onProfile,
                              Runnable onLogout) {

        /*
         Build the top nav first
         so the user can move around.
        */
        HBox navBar = AppTopNav.create(
                AppTopNav.DASHBOARD,
                onDashboard,
                onTransactions,
                onSavingsGoals,
                onTransfer,
                onProfile,
                onLogout
        );

        Label eyebrowLabel = new Label("BANK OF JAVA");
        eyebrowLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #874f00; -fx-letter-spacing: 1.5px;");

        HBox brandRow = new HBox(8);
        brandRow.getChildren().addAll(createJavaLogo(), eyebrowLabel);
        brandRow.setAlignment(Pos.CENTER_LEFT);

        Label welcomeLabel = new Label("Welcome back, " + fullName);
        welcomeLabel.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #543100;");

        Label summaryLabel = new Label("Your balances and latest account activity are all in one place.");
        summaryLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #874f00;");

        VBox heroText = new VBox(6);
        heroText.getChildren().addAll(brandRow, welcomeLabel, summaryLabel);
        heroText.setAlignment(Pos.TOP_LEFT);

        HBox heroRow = new HBox(heroText);
        heroRow.setAlignment(Pos.TOP_LEFT);
        heroRow.setPadding(new Insets(24));
        heroRow.setStyle(
                "-fx-background-color: #ffcd87;" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-color: #ed8b00;" +
                        "-fx-border-radius: 20;"
        );

        Label accountSnapshotLabel = new Label("Account Snapshot");
        accountSnapshotLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #874f00;");

        VBox checkingBox = createBalanceCard("Checking Account", "$" + checkingAmount, "Available for daily use");
        VBox savingsBox = createBalanceCard("Savings Account", "$" + savingsAmount, "Reserved for goals");

        HBox balanceRow = new HBox(18);
        balanceRow.getChildren().addAll(checkingBox, savingsBox);
        balanceRow.setAlignment(Pos.CENTER);
        balanceRow.setPrefWidth(920);

        VBox recentActivityBox = createRecentActivityBox(recentTransactions);
        VBox requestsBox = createRequestsBox(username);

        HBox lowerRow = new HBox(18);
        lowerRow.getChildren().addAll(recentActivityBox, requestsBox);
        lowerRow.setAlignment(Pos.TOP_CENTER);

        VBox layout = new VBox(20);
        layout.getChildren().addAll(navBar, heroRow, accountSnapshotLabel, balanceRow, lowerRow);
        layout.setAlignment(Pos.TOP_CENTER);
        layout.setPadding(new Insets(24));
        layout.setStyle("-fx-background-color: #fff1d6;");

        return layout;
    }

    /*
     This shows the most recent
     account activity for the user.
    */
    private static VBox createRecentActivityBox(List<String> recentTransactions) {
        Label title = new Label("Recent Activity");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #543100;");

        Label subtitle = new Label("Latest movement across your accounts");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #874f00;");

        VBox list = new VBox(12);
        list.setAlignment(Pos.CENTER_LEFT);
        list.setPadding(new Insets(6));

        if (recentTransactions == null || recentTransactions.isEmpty()) {
            Label emptyLabel = new Label("No recent transactions yet.");
            emptyLabel.setStyle("-fx-text-fill: #555555;");
            list.getChildren().add(emptyLabel);
        } else {
            for (String transaction : recentTransactions) {
                Label item = new Label(transaction);
                item.setWrapText(true);
                item.setStyle(
                        "-fx-background-color: #fff3de;" +
                                "-fx-border-color: #ffb854;" +
                                "-fx-border-radius: 10;" +
                                "-fx-background-radius: 10;" +
                                "-fx-padding: 12;" +
                                "-fx-text-fill: #543100;"
                );
                list.getChildren().add(item);
            }
        }

        ScrollPane pane = new ScrollPane(list);
        pane.setFitToWidth(true);
        pane.setMaxWidth(580);
        pane.setMaxHeight(260);
        pane.setStyle("-fx-background-color: transparent;");

        VBox box = new VBox(10);
        box.getChildren().addAll(title, subtitle, pane);
        box.setAlignment(Pos.TOP_LEFT);
        box.setPadding(new Insets(22));
        box.setPrefWidth(620);
        box.setStyle(
                "-fx-background-color: #f7f5f1;" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-color: #ffb854;" +
                        "-fx-border-radius: 18;"
        );
        return box;
    }

    /*
     This shows pending requests
     with accept and deny buttons.
    */
    private static VBox createRequestsBox(String username) {
        UserService userService = new UserService();

        Label title = new Label("Requests");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #543100;");

        Label subtitle = new Label("Accept or deny incoming requests");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #874f00;");

        VBox requestsList = new VBox(10);
        requestsList.setAlignment(Pos.TOP_LEFT);
        requestsList.setPadding(new Insets(6));

        Runnable refreshRequests = () -> {
            requestsList.getChildren().clear();
            List<String> requests = userService.getPendingRequests(username);

            if (requests.isEmpty()) {
                Label emptyLabel = new Label("No pending requests.");
                emptyLabel.setStyle("-fx-text-fill: #555555;");
                requestsList.getChildren().add(emptyLabel);
                return;
            }

            for (String request : requests) {
                Label requestLabel = new Label(request);
                requestLabel.setWrapText(true);
                requestLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #543100;");

                Button acceptButton = makeBlueButton("Accept");
                Button denyButton = makeLightButton("Deny");

                acceptButton.setOnAction(e -> {
                    int requestId = getRequestId(request);
                    boolean success = userService.acceptRequest(requestId);
                    requestLabel.setText(success ? request + " | ACCEPTED" : request + " | ACCEPT FAILED");
                    acceptButton.setDisable(true);
                    denyButton.setDisable(true);
                });

                denyButton.setOnAction(e -> {
                    int requestId = getRequestId(request);
                    boolean success = userService.denyRequest(requestId);
                    requestLabel.setText(success ? request + " | DENIED" : request + " | DENY FAILED");
                    acceptButton.setDisable(true);
                    denyButton.setDisable(true);
                });

                HBox buttons = new HBox(8);
                buttons.getChildren().addAll(acceptButton, denyButton);
                buttons.setAlignment(Pos.CENTER_LEFT);

                VBox card = new VBox(8);
                card.getChildren().addAll(requestLabel, buttons);
                card.setPadding(new Insets(12));
                card.setStyle(
                        "-fx-background-color: #fff3de;" +
                                "-fx-border-color: #ffb854;" +
                                "-fx-border-radius: 10;" +
                                "-fx-background-radius: 10;"
                );
                requestsList.getChildren().add(card);
            }
        };

        refreshRequests.run();

        ScrollPane pane = new ScrollPane(requestsList);
        pane.setFitToWidth(true);
        pane.setMaxWidth(320);
        pane.setMaxHeight(260);
        pane.setStyle("-fx-background-color: transparent;");

        VBox box = new VBox(10);
        box.getChildren().addAll(title, subtitle, pane);
        box.setAlignment(Pos.TOP_LEFT);
        box.setPadding(new Insets(22));
        box.setPrefWidth(340);
        box.setStyle(
                "-fx-background-color: #f7f5f1;" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-color: #ffb854;" +
                        "-fx-border-radius: 18;"
        );
        return box;
    }

    private static int getRequestId(String requestText) {
        String[] parts = requestText.split(" ");
        return Integer.parseInt(parts[2]);
    }

    /*
     These are the balance cards
     for checking and savings.
    */
    private static VBox createBalanceCard(String titleText, String balanceText, String detailText) {
        Label title = new Label(titleText);
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #874f00;");

        Label balance = new Label(balanceText);
        balance.setStyle("-fx-font-size: 40px; -fx-font-weight: bold; -fx-text-fill: #0c7c59;");

        Label detail = new Label(detailText);
        detail.setStyle("-fx-font-size: 14px; -fx-text-fill: #ba6d00;");

        VBox card = new VBox(10);
        card.getChildren().addAll(title, balance, detail);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(26));
        card.setPrefWidth(450);
        card.setPrefHeight(170);
        card.setStyle(
                "-fx-background-color: #f8f6f2;" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-color: #ffb854;" +
                        "-fx-border-radius: 18;"
        );
        return card;
    }

    private static StackPane createJavaLogo() {
        Image image = new Image(DashboardView.class.getResourceAsStream("/com/bank/banking_app/javaLogo.png"));
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(28);
        imageView.setFitHeight(28);

        StackPane logo = new StackPane(imageView);
        logo.setAlignment(Pos.CENTER_LEFT);
        logo.setPrefSize(30, 30);
        logo.setMinSize(30, 30);
        logo.setMaxSize(30, 30);
        return logo;
    }

    private static Button makeBlueButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: #ed8b00; -fx-text-fill: #2b1700; -fx-font-weight: bold; -fx-background-radius: 10;");
        return button;
    }

    private static Button makeLightButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: #ffcd87; -fx-text-fill: #543100; -fx-font-weight: bold; -fx-background-radius: 10;");
        return button;
    }
}
