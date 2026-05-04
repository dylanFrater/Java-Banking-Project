package com.bank.banking_app.ui;

import com.bank.banking_app.model.ProfileInfo;
import com.bank.banking_app.service.UserService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/*
 This screen shows profile info
 and profile setting actions.
*/
public class ProfileView {

    public static VBox create(String username,
                              Runnable onDashboard,
                              Runnable onTransactions,
                              Runnable onSavingsGoals,
                              Runnable onTransfer,
                              Runnable onProfile,
                              Runnable onLogout) {
        UserService userService = new UserService();

        HBox navBar = AppTopNav.create(
                "",
                onDashboard,
                onTransactions,
                onSavingsGoals,
                onTransfer,
                onProfile,
                onLogout
        );

        Label title = new Label("Profile & Settings");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #543100;");

        Label subtitle = new Label("Review account details and make careful updates.");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #874f00;");

        Label fullNameValue = valueLabel();
        Label usernameValue = valueLabel();
        Label cardValue = valueLabel();
        Label routingValue = valueLabel();
        Label createdValue = valueLabel();
        Label statusValue = valueLabel();

        VBox profileInfoBox = new VBox(12);
        profileInfoBox.setAlignment(Pos.TOP_LEFT);
        profileInfoBox.setPadding(new Insets(22));
        profileInfoBox.setPrefWidth(420);
        profileInfoBox.setStyle("-fx-background-color: #fff3de; -fx-border-color: #ffb854; -fx-border-radius: 14; -fx-background-radius: 14;");

        TextField fullNameField = styledField("New full name");
        PasswordField passwordField = styledPasswordField("New password");
        TextField pinField = styledField("New 4-digit PIN");

        Button updateNameButton = makeBlueButton("Change Full Name");
        Button updatePasswordButton = makeBlueButton("Change Password");
        Button updatePinButton = makeBlueButton("Update PIN");
        Button regenerateCardButton = makeBlueButton("Regenerate Card Details");
        Button deleteAccountButton = makeDangerButton("Delete Account");

        Runnable refreshProfile = () -> {
            ProfileInfo profileInfo = userService.getProfileInfo(username);
            fullNameValue.setText(profileInfo.getFullName());
            usernameValue.setText(profileInfo.getUsername());
            cardValue.setText(profileInfo.getMaskedCardNumber());
            routingValue.setText(profileInfo.getRoutingNumber());
            createdValue.setText(profileInfo.getCreatedAt());
            statusValue.setText(profileInfo.getAccountStatus());
        };
        refreshProfile.run();

        updateNameButton.setOnAction(e -> {
            String newFullName = fullNameField.getText().trim();
            String validationMessage = userService.validateFullNameUpdate(newFullName);

            if (validationMessage != null) {
                showError("Could not update name", validationMessage);
                return;
            }
            if (!confirm("Change full name?", "Your account will display the new name immediately.")) {
                return;
            }

            boolean success = userService.updateFullName(username, newFullName);
            if (!success) {
                showError("Could not update name", "Try again in a moment.");
                return;
            }

            fullNameField.clear();
            refreshProfile.run();
            showInfo("Full name updated", "Your profile now shows the new name.");
        });

        updatePasswordButton.setOnAction(e -> {
            String newPassword = passwordField.getText().trim();
            String validationMessage = userService.validatePasswordUpdate(newPassword);

            if (validationMessage != null) {
                showError("Could not update password", validationMessage);
                return;
            }
            if (!confirm("Change password?", "Make sure you remember the new password before continuing.")) {
                return;
            }

            boolean success = userService.resetPassword(username, newPassword);
            if (!success) {
                showError("Could not update password", "Try again in a moment.");
                return;
            }

            passwordField.clear();
            showInfo("Password updated", "Your new password is now active.");
        });

        updatePinButton.setOnAction(e -> {
            String newPin = pinField.getText().trim();
            String validationMessage = userService.validatePinUpdate(newPin);

            if (validationMessage != null) {
                showError("Could not update PIN", validationMessage);
                return;
            }
            if (!confirm("Update PIN?", "Your old PIN will stop working immediately.")) {
                return;
            }

            boolean success = userService.updatePin(username, newPin);
            if (!success) {
                showError("Could not update PIN", "Try again in a moment.");
                return;
            }

            pinField.clear();
            showInfo("PIN updated", "Your account now uses the new PIN.");
        });

        regenerateCardButton.setOnAction(e -> {
            if (!confirm("Regenerate card details?", "Your card number, CVC, and expiration date will all change.")) {
                return;
            }

            boolean success = userService.regenerateCardDetails(username);
            if (!success) {
                showError("Could not regenerate card", "Try again in a moment.");
                return;
            }

            refreshProfile.run();
            showInfo("Card details regenerated", "Your profile now shows the updated card details.");
        });

        deleteAccountButton.setOnAction(e -> {
            if (!confirm("Delete account?", "This will permanently remove your account, transactions, requests, and savings goals.")) {
                return;
            }

            boolean success = userService.deleteAccount(username);
            if (!success) {
                showError("Could not delete account", "Try again in a moment.");
                return;
            }

            showInfo("Account deleted", "Your account has been removed.");
            onLogout.run();
        });

        VBox overviewColumn = new VBox(14);
        overviewColumn.getChildren().addAll(
                sectionHeading("Account Overview"),
                infoRow("Full Name", fullNameValue),
                infoRow("Username", usernameValue),
                infoRow("Card Number", cardValue),
                infoRow("Routing Number", routingValue),
                infoRow("Created", createdValue),
                infoRow("Status", statusValue)
        );
        overviewColumn.setAlignment(Pos.TOP_LEFT);

        VBox updateColumn = new VBox(16);
        updateColumn.getChildren().addAll(
                sectionHeading("Update Details"),
                settingBlock("Change account name", fullNameField, updateNameButton),
                settingBlock("Change password", passwordField, updatePasswordButton),
                settingBlock("Update PIN", pinField, updatePinButton),
                settingBlock("Card actions", regenerateCardButton),
                settingBlock("Account removal", deleteAccountButton)
        );
        updateColumn.setAlignment(Pos.TOP_LEFT);
        updateColumn.setPadding(new Insets(22));
        updateColumn.setPrefWidth(420);
        updateColumn.setStyle("-fx-background-color: #fff3de; -fx-border-color: #ffb854; -fx-border-radius: 14; -fx-background-radius: 14;");

        HBox contentRow = new HBox(18);
        contentRow.getChildren().addAll(profileInfoBox, updateColumn);
        contentRow.setAlignment(Pos.TOP_CENTER);

        profileInfoBox.getChildren().setAll(sectionHeading("Profile Details"), overviewColumn);

        VBox card = new VBox(16);
        card.getChildren().addAll(title, subtitle, contentRow);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(30));
        card.setMaxWidth(940);
        card.setStyle("-fx-background-color: #f7f5f1; -fx-background-radius: 16; -fx-border-color: #ffb854; -fx-border-radius: 16;");

        VBox layout = new VBox(18);
        layout.getChildren().addAll(navBar, card);
        layout.setAlignment(Pos.TOP_CENTER);
        layout.setPadding(new Insets(24));
        layout.setStyle("-fx-background-color: #fff1d6;");
        return layout;
    }

    private static VBox settingBlock(String title, javafx.scene.Node... nodes) {
        Label heading = new Label(title);
        heading.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #874f00;");

        VBox block = new VBox(8);
        block.getChildren().add(heading);
        block.getChildren().addAll(nodes);
        return block;
    }

    private static HBox infoRow(String labelText, Label valueLabel) {
        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #874f00;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(12);
        row.getChildren().addAll(label, spacer, valueLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 0, 10, 0));
        row.setStyle("-fx-border-color: transparent transparent #f1cf98 transparent;");
        return row;
    }

    private static Label sectionHeading(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #543100;");
        return label;
    }

    private static Label valueLabel() {
        Label label = new Label();
        label.setStyle("-fx-font-size: 14px; -fx-text-fill: #543100;");
        return label;
    }

    private static TextField styledField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setMaxWidth(Double.MAX_VALUE);
        field.setStyle("-fx-padding: 9; -fx-background-radius: 8;");
        return field;
    }

    private static PasswordField styledPasswordField(String prompt) {
        PasswordField field = new PasswordField();
        field.setPromptText(prompt);
        field.setMaxWidth(Double.MAX_VALUE);
        field.setStyle("-fx-padding: 9; -fx-background-radius: 8;");
        return field;
    }

    private static boolean confirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.OK, ButtonType.CANCEL);
        alert.setHeaderText(title);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
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
        button.setPrefWidth(240);
        button.setStyle("-fx-background-color: #ed8b00; -fx-text-fill: #2b1700; -fx-font-weight: bold; -fx-background-radius: 10;");
        return button;
    }

    private static Button makeDangerButton(String text) {
        Button button = new Button(text);
        button.setPrefWidth(240);
        button.setStyle("-fx-background-color: #d97706; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10;");
        return button;
    }
}
