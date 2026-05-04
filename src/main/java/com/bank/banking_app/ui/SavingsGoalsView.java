package com.bank.banking_app.ui;

import com.bank.banking_app.model.SavingsGoal;
import com.bank.banking_app.service.UserService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.util.List;

/*
 This screen is for savings goals.
 Users can create goals,
 add money, or withdraw money.
*/
public class SavingsGoalsView {

    public static VBox create(String username,
                              Runnable onDashboard,
                              Runnable onTransactions,
                              Runnable onSavingsGoals,
                              Runnable onTransfer,
                              Runnable onProfile,
                              Runnable onLogout) {
        UserService userService = new UserService();

        HBox navBar = AppTopNav.create(
                AppTopNav.GOALS,
                onDashboard,
                onTransactions,
                onSavingsGoals,
                onTransfer,
                onProfile,
                onLogout
        );

        Label title = new Label("Savings Goals");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #543100;");

        Label subtitle = new Label("Create a goal, contribute to it, or withdraw from it.");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #874f00;");

        TextField goalNameField = styledField("Goal name");
        TextField targetAmountField = styledField("Target amount");
        Button createGoalButton = makeBlueButton("Create Goal");

        ListView<SavingsGoal> goalsList = new ListView<>();
        goalsList.setPrefHeight(260);
        goalsList.setMaxWidth(820);

        ComboBox<String> contributionAccountBox = new ComboBox<>();
        contributionAccountBox.getItems().addAll("Checking", "Savings");
        contributionAccountBox.setPromptText("Contribute from");
        contributionAccountBox.setMaxWidth(Double.MAX_VALUE);

        TextField contributionAmountField = styledField("Contribution amount");
        Button contributeButton = makeBlueButton("Add to Goal");

        ComboBox<String> withdrawAccountBox = new ComboBox<>();
        withdrawAccountBox.getItems().addAll("Checking", "Savings");
        withdrawAccountBox.setPromptText("Withdraw to");
        withdrawAccountBox.setMaxWidth(Double.MAX_VALUE);

        TextField withdrawAmountField = styledField("Withdrawal amount");
        Button withdrawButton = makeBlueButton("Take Out of Goal");

        Runnable refreshGoals = () -> {
            List<SavingsGoal> goals = userService.getSavingsGoals(username);
            goalsList.getItems().setAll(goals);
        };
        refreshGoals.run();

        createGoalButton.setOnAction(e -> {
            String goalName = goalNameField.getText().trim();
            String targetAmount = targetAmountField.getText().trim();
            String validationMessage = userService.validateSavingsGoalInput(goalName, targetAmount);

            if (validationMessage != null) {
                showError("Could not create goal", validationMessage);
                return;
            }

            boolean success = userService.createSavingsGoal(username, goalName, targetAmount);
            if (!success) {
                showError("Could not create goal", "Try a different goal name or check the amount.");
                return;
            }

            goalNameField.clear();
            targetAmountField.clear();
            refreshGoals.run();
            showInfo("Savings goal created", "Your new goal is now available.");
        });

        contributeButton.setOnAction(e -> {
            SavingsGoal selectedGoal = goalsList.getSelectionModel().getSelectedItem();
            String fromAccount = contributionAccountBox.getValue();

            if (selectedGoal == null) {
                showError("No goal selected", "Choose a goal before adding money.");
                return;
            }
            if (fromAccount == null) {
                showError("No account selected", "Choose which account to contribute from.");
                return;
            }

            try {
                BigDecimal amount = new BigDecimal(contributionAmountField.getText().trim());
                boolean success = userService.contributeToSavingsGoal(username, selectedGoal.getId(), fromAccount, amount);
                if (!success) {
                    showError("Contribution failed", "Check your balance, the amount, or the goal status.");
                    return;
                }

                contributionAmountField.clear();
                refreshGoals.run();
                showInfo("Money added", "Added funds to " + selectedGoal.getGoalName() + ".");
            } catch (Exception ex) {
                showError("Invalid amount", "Enter a valid contribution amount.");
            }
        });

        withdrawButton.setOnAction(e -> {
            SavingsGoal selectedGoal = goalsList.getSelectionModel().getSelectedItem();
            String toAccount = withdrawAccountBox.getValue();

            if (selectedGoal == null) {
                showError("No goal selected", "Choose a goal before withdrawing.");
                return;
            }
            if (toAccount == null) {
                showError("No account selected", "Choose which account should receive the money.");
                return;
            }

            try {
                BigDecimal amount = new BigDecimal(withdrawAmountField.getText().trim());
                boolean success = userService.withdrawFromSavingsGoal(username, selectedGoal.getId(), toAccount, amount);
                if (!success) {
                    showError("Withdrawal failed", "Check the goal balance and the amount entered.");
                    return;
                }

                withdrawAmountField.clear();
                refreshGoals.run();
                showInfo("Money moved", "Transferred money out of " + selectedGoal.getGoalName() + ".");
            } catch (Exception ex) {
                showError("Invalid amount", "Enter a valid withdrawal amount.");
            }
        });

        ScrollPane goalsPane = new ScrollPane(goalsList);
        goalsPane.setFitToWidth(true);
        goalsPane.setMaxWidth(840);
        goalsPane.setMaxHeight(270);
        goalsPane.setStyle("-fx-background-color: transparent;");

        VBox createColumn = createGoalColumn("Create Goal", goalNameField, targetAmountField, createGoalButton);
        VBox contributeColumn = createGoalColumn("Contribute", contributionAccountBox, contributionAmountField, contributeButton);
        VBox withdrawColumn = createGoalColumn("Withdraw", withdrawAccountBox, withdrawAmountField, withdrawButton);

        HBox.setHgrow(createColumn, Priority.ALWAYS);
        HBox.setHgrow(contributeColumn, Priority.ALWAYS);
        HBox.setHgrow(withdrawColumn, Priority.ALWAYS);

        HBox actionColumns = new HBox(18);
        actionColumns.getChildren().addAll(createColumn, contributeColumn, withdrawColumn);
        actionColumns.setAlignment(Pos.TOP_CENTER);

        VBox card = new VBox(18);
        card.getChildren().addAll(title, subtitle, goalsPane, actionColumns);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(30));
        card.setMaxWidth(980);
        card.setStyle("-fx-background-color: #f7f5f1; -fx-background-radius: 16; -fx-border-color: #ffb854; -fx-border-radius: 16;");

        VBox layout = new VBox(18);
        layout.getChildren().addAll(navBar, card);
        layout.setAlignment(Pos.TOP_CENTER);
        layout.setPadding(new Insets(24));
        layout.setStyle("-fx-background-color: #fff1d6;");
        return layout;
    }

    private static VBox createGoalColumn(String heading, javafx.scene.Node first, javafx.scene.Node second, Button button) {
        Label label = new Label(heading);
        label.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #874f00;");

        VBox column = new VBox(12);
        column.getChildren().addAll(label, first, second, button);
        column.setAlignment(Pos.TOP_CENTER);
        column.setPadding(new Insets(18));
        column.setPrefWidth(280);
        column.setStyle("-fx-background-color: #fff3de; -fx-border-color: #ffb854; -fx-border-radius: 14; -fx-background-radius: 14;");
        return column;
    }

    private static TextField styledField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setMaxWidth(Double.MAX_VALUE);
        field.setStyle("-fx-padding: 9; -fx-background-radius: 8;");
        return field;
    }

    private static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText(title);
        alert.showAndWait();
    }

    private static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setHeaderText(title);
        alert.showAndWait();
    }

    private static Button makeBlueButton(String text) {
        Button button = new Button(text);
        button.setPrefWidth(220);
        button.setStyle("-fx-background-color: #ed8b00; -fx-text-fill: #2b1700; -fx-font-weight: bold; -fx-background-radius: 10;");
        return button;
    }
}
