package com.bank.banking_app.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainUI extends Application {


    //makes window size dynamic, not changing sizes when swapping scenes
    private void dynamicWindowSize(){
        dynamicWidth = primaryStage.getWidth();
        dynamicHeight = primaryStage.getHeight();
    }








    //initial window size on start
    private Stage primaryStage;
    private double dynamicWidth = 1000;
    private double dynamicHeight = 800;

    @Override
    public void start(Stage stage) {

        primaryStage = stage;
        primaryStage.setHeight(dynamicHeight);
        primaryStage.setWidth(dynamicWidth);

        showLoginScreen();
    }

    private void showLoginScreen() {
        VBox layout = LoginView.create(this::showDashboardScreen);

        dynamicWindowSize();

        Scene scene = new Scene(layout, dynamicWidth, dynamicHeight);
        //dark mode
        scene.getStylesheets().add(getClass().getResource("/darkTheme.css").toExternalForm());

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

        dynamicWindowSize();

        Scene scene = new Scene(layout, dynamicWidth, dynamicHeight);
        scene.getStylesheets().add(getClass().getResource("/darkTheme.css").toExternalForm());
        primaryStage.setScene(scene);
    }

    private void showTransactionsScreen() {
        VBox layout = TransactionsView.create(this::showDashboardScreen);

        dynamicWindowSize();

        Scene scene = new Scene(layout, dynamicWidth, dynamicHeight);
        scene.getStylesheets().add(getClass().getResource("/darkTheme.css").toExternalForm());

        primaryStage.setScene(scene);
    }

    private void showTransferScreen() {
        VBox layout = TransferView.create(this::showDashboardScreen);

        dynamicWindowSize();

        Scene scene = new Scene(layout, dynamicWidth, dynamicHeight);
        scene.getStylesheets().add(getClass().getResource("/darkTheme.css").toExternalForm());

        primaryStage.setScene(scene);
    }

    public static void main(String[] args) {
        launch();
    }
}