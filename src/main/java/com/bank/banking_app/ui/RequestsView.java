package com.bank.banking_app.ui;

import com.bank.banking_app.service.UserService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.util.List;

/*
 This was the older requests screen.
 Requests are on the dashboard now,
 but I kept this class for reference.
*/
public class RequestsView {

    public static VBox create(String currentUsername, Runnable onBack) {
        Label title = new Label("Money Requests");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #1f3c88;");

        Label subtitle = new Label("Accept or deny incoming money requests");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #555555;");

        VBox requestList = new VBox(12);
        requestList.setAlignment(Pos.CENTER_LEFT);
        requestList.setPadding(new Insets(10));

        UserService userService = new UserService();
        List<String> requests = userService.getPendingRequests(currentUsername);

        if (requests.isEmpty()) {
            Label emptyLabel = new Label("No pending requests.");
            emptyLabel.setStyle("-fx-text-fill: #555555;");
            requestList.getChildren().add(emptyLabel);
        } else {
            for (String request : requests) {
                Label requestLabel = new Label(request);
                requestLabel.setWrapText(true);
                requestLabel.setStyle("-fx-font-weight: bold;");

                Button acceptButton = makeBlueButton("Accept Request");
                Button denyButton = makeLightButton("Deny Request");

                acceptButton.setOnAction(e -> {
                    int requestId = getRequestId(request);
                    boolean success = userService.acceptRequest(requestId);

                    if (success) {
                        requestLabel.setText(request + " | ACCEPTED");
                        acceptButton.setDisable(true);
                        denyButton.setDisable(true);
                    } else {
                        requestLabel.setText(request + " | ACCEPT FAILED");
                    }
                });

                denyButton.setOnAction(e -> {
                    int requestId = getRequestId(request);
                    boolean success = userService.denyRequest(requestId);

                    if (success) {
                        requestLabel.setText(request + " | DENIED");
                        acceptButton.setDisable(true);
                        denyButton.setDisable(true);
                    } else {
                        requestLabel.setText(request + " | DENY FAILED");
                    }
                });

                VBox requestBox = new VBox(10);
                requestBox.getChildren().addAll(requestLabel, acceptButton, denyButton);
                requestBox.setPadding(new Insets(14));
                requestBox.setStyle(
                        "-fx-background-color: #f8f9fc;" +
                                "-fx-border-color: #d9d9d9;" +
                                "-fx-border-radius: 10;" +
                                "-fx-background-radius: 10;"
                );

                requestList.getChildren().add(requestBox);
            }
        }

        ScrollPane scrollPane = new ScrollPane(requestList);
        scrollPane.setFitToWidth(true);
        scrollPane.setMaxWidth(650);
        scrollPane.setMaxHeight(340);

        Button backButton = makeLightButton("Back to Dashboard");
        backButton.setOnAction(e -> onBack.run());

        VBox card = new VBox(15);
        card.getChildren().addAll(title, subtitle, scrollPane, backButton);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(30));
        card.setMaxWidth(740);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-border-color: #d9d9d9; -fx-border-radius: 16;");

        VBox layout = new VBox(card);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));
        layout.setStyle("-fx-background-color: #f3f6fb;");

        return layout;
    }

    private static int getRequestId(String requestText) {
        String[] parts = requestText.split(" ");
        return Integer.parseInt(parts[2]);
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
