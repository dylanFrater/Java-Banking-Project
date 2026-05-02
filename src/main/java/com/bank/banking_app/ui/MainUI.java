package com.bank.banking_app.ui;

import com.bank.banking_app.service.UserService;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

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

        primaryStage.setTitle("Banking App");
        primaryStage.setScene(new Scene(layout, 500, 400));
        primaryStage.show();
    }

    private void showSignupScreen() {
        VBox layout = SignupView.create(this::showLoginScreen);
        primaryStage.setScene(new Scene(layout, 600, 600));
    }

    private void showDashboardScreen() {
        UserService service = new UserService();

        Map<String, String> balances = service.getBalances(currentUsername);

        String checking = balances.getOrDefault("checking", "0.00");
        String savings = balances.getOrDefault("savings", "0.00");
        String fullName = service.getFullName(currentUsername);

        VBox layout = DashboardView.create(
                fullName,
                checking,
                savings,
                this::showTransactionsScreen,
                this::showTransferScreen,
                this::showRequestsScreen,
                this::showLoginScreen
        );

        primaryStage.setScene(new Scene(layout, 650, 550));
    }

    private void showTransactionsScreen() {
        VBox layout = TransactionsView.create(currentUsername, this::showDashboardScreen);
        primaryStage.setScene(new Scene(layout, 850, 600));
    }

    private void showTransferScreen() {
        VBox layout = TransferView.create(currentUsername, this::showDashboardScreen);
        primaryStage.setScene(new Scene(layout, 700, 650));
    }

    private void showRequestsScreen() {
        VBox layout = RequestsView.create(currentUsername, this::showDashboardScreen);
        primaryStage.setScene(new Scene(layout, 700, 550));
    }

    public static void main(String[] args) {
        launch();
    }
}