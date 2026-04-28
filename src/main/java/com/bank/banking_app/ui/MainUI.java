package com.bank.banking_app.ui;

import com.bank.banking_app.service.UserService;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;

public class MainUI extends Application {

    private Stage primaryStage;
    private String currentUsername;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        showLoginScreen();
    }

    private void showLoginScreen() {
        VBox layout = LoginView.create(
                username -> {
                    currentUsername = username;
                    showDashboardScreen();
                },
                this::showSignupScreen
        );

        Scene scene = new Scene(layout, 500, 400);

        primaryStage.setTitle("Banking App");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showSignupScreen() {
        VBox layout = SignupView.create(this::showLoginScreen);

        Scene scene = new Scene(layout, 600, 600);
        primaryStage.setScene(scene);
    }

    private void showDashboardScreen() {
        UserService userService = new UserService();

        Map<String, String> balances = userService.getBalances(currentUsername);

        String checking = balances.getOrDefault("checking", "0.00");
        String savings = balances.getOrDefault("savings", "0.00");

        VBox layout = DashboardView.create(
                checking,
                savings,
                this::showTransactionsScreen,
                this::showTransferScreen,
                this::showLoginScreen
        );

        Scene scene = new Scene(layout, 650, 500);
        primaryStage.setScene(scene);
    }

    private void showTransactionsScreen() {
        UserService userService = new UserService();
        List<String> transactions = userService.getTransactionHistory(currentUsername);

        VBox layout = TransactionsView.create(transactions, this::showDashboardScreen);

        Scene scene = new Scene(layout, 750, 550);
        primaryStage.setScene(scene);
    }

    private void showTransferScreen() {
        VBox layout = TransferView.create(currentUsername, this::showDashboardScreen);

        Scene scene = new Scene(layout, 650, 500);
        primaryStage.setScene(scene);
    }

    public static void main(String[] args) {
        launch();
    }
}