package com.bank.banking_app.ui;

import com.bank.banking_app.service.UserService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.util.List;

public class RequestsView {

    public static VBox create(String currentUsername, Runnable onBack) {
        Label title = new Label("Incoming Money Requests");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        VBox requestList = new VBox(12);
        requestList.setAlignment(Pos.CENTER_LEFT);

        UserService userService = new UserService();

        List<String> requests = userService.getPendingRequests(currentUsername);

        if (requests.isEmpty()) {
            requestList.getChildren().add(new Label("No pending requests."));
        } else {
            for (String request : requests) {
                Label requestLabel = new Label(request);

                Button acceptButton = new Button("Accept Request");

                acceptButton.setOnAction(e -> {
                    int requestId = getRequestId(request);
                    boolean success = userService.acceptRequest(requestId);

                    if (success) {
                        requestLabel.setText(request + " | ACCEPTED");
                        acceptButton.setDisable(true);
                    } else {
                        requestLabel.setText(request + " | FAILED");
                    }
                });

                VBox requestBox = new VBox(8);
                requestBox.getChildren().addAll(requestLabel, acceptButton);
                requestBox.setStyle("-fx-border-color: lightgray; -fx-padding: 10;");

                requestList.getChildren().add(requestBox);
            }
        }

        ScrollPane scrollPane = new ScrollPane(requestList);
        scrollPane.setFitToWidth(true);
        scrollPane.setMaxWidth(600);
        scrollPane.setMaxHeight(350);

        Button backButton = new Button("Back to Dashboard");
        backButton.setPrefWidth(200);
        backButton.setOnAction(e -> onBack.run());

        VBox layout = new VBox(15);
        layout.getChildren().addAll(title, scrollPane, backButton);

        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));

        return layout;
    }

    private static int getRequestId(String requestText) {
        String[] parts = requestText.split(" ");
        return Integer.parseInt(parts[2]);
    }
}