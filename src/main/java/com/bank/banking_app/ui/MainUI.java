package com.bank.banking_app.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainUI extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        showLoginScreen();
    }

    private void showLoginScreen() {
        VBox layout = LoginView.create(this::showDashboardScreen);

        Scene scene = new Scene(layout, 500, 400);

        primaryStage.setTitle("Banking App");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showDashboardScreen() {
        VBox layout = DashboardView.create(
                this::showTransactionsScreen,
                this::showTransferScreen,
                this::showLoginScreen
        );

        Scene scene = new Scene(layout, 650, 500);
        primaryStage.setScene(scene);
    }

    private void showTransactionsScreen() {
        VBox layout = TransactionsView.create(this::showDashboardScreen);

        Scene scene = new Scene(layout, 650, 500);
        primaryStage.setScene(scene);
    }

    private void showTransferScreen() {
        VBox layout = TransferView.create(this::showDashboardScreen);

        Scene scene = new Scene(layout, 650, 500);
        primaryStage.setScene(scene);
    }

    public static void main(String[] args) {
        launch();
    }
}