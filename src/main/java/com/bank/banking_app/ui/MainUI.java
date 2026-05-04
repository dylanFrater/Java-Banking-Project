package com.bank.banking_app.ui;

import com.bank.banking_app.service.UserService;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;

/*
 This is the main JavaFX class.
 It switches between the screens
 in the project.
*/
public class MainUI extends Application {

    private Stage primaryStage;
    private String currentUsername;
    private final UserService userService = new UserService();

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        showLoginScreen();
    }

    private void changeScene(VBox layout, double defaultWidth, double defaultHeight) {
        double width = defaultWidth;
        double height = defaultHeight;

        if (primaryStage.getScene() != null) {
            width = primaryStage.getWidth();
            height = primaryStage.getHeight();
        }

        primaryStage.setScene(new Scene(layout, width, height));
        primaryStage.show();
    }

    /*
     These methods change scenes
     based on where the user goes.
    */
    private void showLoginScreen() {
        VBox layout = LoginView.create(
                username -> {
                    currentUsername = username;
                    showDashboardScreen();
                },
                this::showSignupScreen
        );

        primaryStage.setTitle("Bank Of Java");
        changeScene(layout, 500, 400);
    }

    private void showSignupScreen() {
        VBox layout = SignupView.create(this::showLoginScreen);
        changeScene(layout, 600, 600);
    }

    /*
     The dashboard needs balances,
     the user's name, and history.
    */
    private void showDashboardScreen() {
        Map<String, String> balances = userService.getBalances(currentUsername);

        String checking = balances.getOrDefault("checking", "0.00");
        String savings = balances.getOrDefault("savings", "0.00");
        String fullName = userService.getFullName(currentUsername);
        List<String> recentTransactions = userService.getRecentTransactions(currentUsername, 5);

        VBox layout = DashboardView.create(
                currentUsername,
                fullName,
                checking,
                savings,
                recentTransactions,
                this::showDashboardScreen,
                this::showTransactionsScreen,
                this::showSavingsGoalsScreen,
                this::showTransferScreen,
                this::showProfileScreen,
                this::showLoginScreen
        );

        changeScene(layout, 980, 760);
    }

    private void showTransactionsScreen() {
        VBox layout = TransactionsView.create(
                currentUsername,
                this::showDashboardScreen,
                this::showTransactionsScreen,
                this::showSavingsGoalsScreen,
                this::showTransferScreen,
                this::showProfileScreen,
                this::showLoginScreen
        );
        changeScene(layout, 980, 760);
    }

    private void showProfileScreen() {
        VBox layout = ProfileView.create(
                currentUsername,
                this::showDashboardScreen,
                this::showTransactionsScreen,
                this::showSavingsGoalsScreen,
                this::showTransferScreen,
                this::showProfileScreen,
                this::showLoginScreen
        );
        changeScene(layout, 980, 760);
    }

    private void showSavingsGoalsScreen() {
        VBox layout = SavingsGoalsView.create(
                currentUsername,
                this::showDashboardScreen,
                this::showTransactionsScreen,
                this::showSavingsGoalsScreen,
                this::showTransferScreen,
                this::showProfileScreen,
                this::showLoginScreen
        );
        changeScene(layout, 980, 760);
    }

    private void showTransferScreen() {
        VBox layout = TransferView.create(
                currentUsername,
                this::showDashboardScreen,
                this::showTransactionsScreen,
                this::showSavingsGoalsScreen,
                this::showTransferScreen,
                this::showProfileScreen,
                this::showLoginScreen
        );
        changeScene(layout, 980, 760);
    }

    public static void main(String[] args) {
        launch();
    }
}
