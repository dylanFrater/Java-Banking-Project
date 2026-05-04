package com.bank.banking_app.service;

import com.bank.banking_app.config.databaseConnection;
import com.bank.banking_app.model.ProfileInfo;
import com.bank.banking_app.model.SavingsGoal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/*
 This class handles most of the
 database logic for the app.
 It helps keep UI files cleaner.
*/
public class UserService {
    private static final String CHECKING = "Checking";
    private static final String SAVINGS = "Savings";
    private static final DateTimeFormatter TRANSACTION_TIME_FORMAT =
            DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");

    /*
     These methods check input
     before saving or logging in.
    */
    public String validateRegistrationInput(String fullName, String username, String password) {
        String trimmedFullName = safeTrim(fullName);
        String trimmedUsername = safeTrim(username);
        String trimmedPassword = safeTrim(password);

        if (trimmedFullName.isBlank() || trimmedUsername.isBlank() || trimmedPassword.isBlank()) {
            return "Fill in all fields.";
        }

        if (trimmedFullName.length() < 2) {
            return "Enter a valid full name.";
        }

        if (!trimmedUsername.matches("[A-Za-z0-9_]{4,20}")) {
            return "Username must be 4-20 letters, numbers, or underscores.";
        }

        if (trimmedPassword.length() < 6) {
            return "Password must be at least 6 characters.";
        }

        if (userExists(trimmedUsername)) {
            return "That username is already taken.";
        }

        return null;
    }

    public String validateLoginInput(String username, String password) {
        String trimmedUsername = safeTrim(username);
        String trimmedPassword = safeTrim(password);

        if (trimmedUsername.isBlank() || trimmedPassword.isBlank()) {
            return "Enter your username and password.";
        }

        if (!userExists(trimmedUsername)) {
            return "No account matches that username.";
        }

        return null;
    }

    public boolean registerUser(String fullName, String username, String password) {
        String validationMessage = validateRegistrationInput(fullName, username, password);
        if (validationMessage != null) {
            return false;
        }

        String trimmedFullName = fullName.trim();
        String trimmedUsername = username.trim();
        String trimmedPassword = password.trim();

        String sql = "INSERT INTO users " +
                "(username, password_hash, pin_hash, card_number, full_name, cvc, expiration_date, routing_number, savings_balance, checking_balance) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, trimmedUsername);
            stmt.setString(2, trimmedPassword);
            stmt.setString(3, generatePin());
            stmt.setString(4, generateCardNumber());
            stmt.setString(5, trimmedFullName);
            stmt.setString(6, generateCvc());
            stmt.setString(7, generateExpirationDate());
            stmt.setString(8, "000000000");
            stmt.setBigDecimal(9, new BigDecimal("0.00"));
            stmt.setBigDecimal(10, new BigDecimal("0.00"));
            stmt.executeUpdate();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /*
     This checks if the username
     and password match the database.
    */
    public boolean loginUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username=? AND password_hash=?";
        String trimmedUsername = safeTrim(username);
        String trimmedPassword = safeTrim(password);

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, trimmedUsername);
            stmt.setString(2, trimmedPassword);

            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /*
     These load simple user data
     for the dashboard and profile.
    */
    public Map<String, String> getBalances(String username) {
        Map<String, String> balances = new HashMap<>();

        String sql = "SELECT checking_balance, savings_balance FROM users WHERE username=?";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                balances.put("checking", rs.getString("checking_balance"));
                balances.put("savings", rs.getString("savings_balance"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return balances;
    }

    public String getFullName(String username) {
        String sql = "SELECT full_name FROM users WHERE username=?";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("full_name");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    /*
     These methods move money around
     between users and accounts.
    */
    public boolean sendMoney(String sender, String receiver, BigDecimal amount) {
        sender = safeTrim(sender);
        receiver = safeTrim(receiver);

        if (!isValidTransferPair(sender, receiver, amount)) {
            return false;
        }

        try (Connection conn = databaseConnection.getConnection()) {

            BigDecimal balance = getBalance(conn, sender, "checking_balance");
            if (balance.compareTo(amount) < 0) {
                return false;
            }

            updateBalance(conn, sender, "checking_balance", amount.negate());
            updateBalance(conn, receiver, "checking_balance", amount);

            addTransaction(sender, "Sent Money", CHECKING, null, amount, "Sent to " + receiver);
            addTransaction(receiver, "Received Money", null, CHECKING, amount, "From " + sender);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean requestMoney(String requester, String receiver, BigDecimal amount) {
        requester = safeTrim(requester);
        receiver = safeTrim(receiver);

        if (!isValidTransferPair(requester, receiver, amount)) {
            return false;
        }

        String sql = "INSERT INTO requests (requester_username, receiver_username, amount, status) VALUES (?, ?, ?, 'PENDING')";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, requester);
            stmt.setString(2, receiver);
            stmt.setBigDecimal(3, amount);
            stmt.executeUpdate();

            addTransaction(requester, "Money Requested", null, null, amount, "Requested from " + receiver);
            addTransaction(receiver, "Request Received", null, null, amount, requester + " requested money from you");

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean acceptRequest(int id) {
        try (Connection conn = databaseConnection.getConnection()) {

            String sql = "SELECT requester_username, receiver_username, amount FROM requests WHERE id=? AND status='PENDING'";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);

            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return false;

            String requester = rs.getString("requester_username");
            String receiver = rs.getString("receiver_username");
            BigDecimal amount = rs.getBigDecimal("amount");

            boolean success = sendMoney(receiver, requester, amount);
            if (!success) return false;

            PreparedStatement update = conn.prepareStatement("UPDATE requests SET status='ACCEPTED' WHERE id=?");
            update.setInt(1, id);
            update.executeUpdate();

            addTransaction(requester, "Request Accepted", null, "Checking", amount, receiver + " accepted your request");
            addTransaction(receiver, "Accepted Request", "Checking", null, amount, "You accepted request from " + requester);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean denyRequest(int id) {
        String getRequestSql = "SELECT requester_username, receiver_username, amount FROM requests WHERE id=? AND status='PENDING'";
        String updateSql = "UPDATE requests SET status='DENIED' WHERE id=? AND status='PENDING'";

        try (Connection conn = databaseConnection.getConnection()) {

            PreparedStatement getStmt = conn.prepareStatement(getRequestSql);
            getStmt.setInt(1, id);

            ResultSet rs = getStmt.executeQuery();

            if (!rs.next()) {
                return false;
            }

            String requester = rs.getString("requester_username");
            String receiver = rs.getString("receiver_username");
            BigDecimal amount = rs.getBigDecimal("amount");

            PreparedStatement updateStmt = conn.prepareStatement(updateSql);
            updateStmt.setInt(1, id);

            int rows = updateStmt.executeUpdate();

            if (rows > 0) {
                addTransaction(requester, "Request Denied", null, null, amount,
                        receiver + " denied your money request");

                addTransaction(receiver, "Denied Request", null, null, amount,
                        "You denied money request from " + requester);

                return true;
            }

            return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean transferBetweenAccounts(String username, String from, String to, BigDecimal amount) {
        username = safeTrim(username);
        from = safeTrim(from);
        to = safeTrim(to);

        if (username.isBlank() || from.isBlank() || to.isBlank() || amount == null) {
            return false;
        }

        if (from.equals(to) || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        String fromCol = getBalanceColumn(from);
        String toCol = getBalanceColumn(to);

        if (fromCol == null || toCol == null) {
            return false;
        }

        try (Connection conn = databaseConnection.getConnection()) {

            BigDecimal balance = getBalance(conn, username, fromCol);
            if (balance.compareTo(amount) < 0) {
                return false;
            }

            updateBalance(conn, username, fromCol, amount.negate());
            updateBalance(conn, username, toCol, amount);

            addTransaction(username, "Internal Transfer", from, to, amount, "Moved from " + from + " to " + to);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /*
     These methods load requests
     and transaction history.
    */
    public List<String> getPendingRequests(String username) {
        List<String> list = new ArrayList<>();

        String sql = "SELECT id, requester_username, amount FROM requests WHERE receiver_username=? AND status='PENDING'";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add("Request ID: " + rs.getInt("id") +
                        " From: " + rs.getString("requester_username") +
                        " Amount: $" + rs.getBigDecimal("amount"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<String> getTransactionHistory(String username) {
        List<String> history = new ArrayList<>();

        String sql = "SELECT * FROM transactions WHERE username=? ORDER BY created_at DESC";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                history.add(formatTransactionRecord(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return history;
    }

    public List<String> getRecentTransactions(String username, int limit) {
        List<String> history = new ArrayList<>();

        if (limit <= 0) {
            return history;
        }

        String sql = "SELECT * FROM transactions WHERE username=? ORDER BY created_at DESC LIMIT ?";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setInt(2, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                history.add(formatTransactionRecord(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return history;
    }

    public List<String> getTransactionsByMonthAndYear(String username, String monthName, String year) {
        List<String> history = new ArrayList<>();

        int monthNumber = getMonthNumber(monthName);

        String sql = "SELECT * FROM transactions WHERE username=?";

        if (monthNumber > 0) {
            sql += " AND MONTH(created_at)=?";
        }

        if (year != null && !year.isBlank()) {
            sql += " AND YEAR(created_at)=?";
        }

        sql += " ORDER BY created_at DESC";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int index = 1;
            stmt.setString(index++, username);

            if (monthNumber > 0) {
                stmt.setInt(index++, monthNumber);
            }

            if (year != null && !year.isBlank()) {
                stmt.setString(index, year);
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                history.add(formatTransactionRecord(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return history;
    }

    /*
     These methods handle savings goals
     and profile validation.
    */
    public String validateSavingsGoalInput(String goalName, String targetAmountText) {
        String trimmedGoalName = safeTrim(goalName);
        String trimmedTargetAmount = safeTrim(targetAmountText);

        if (trimmedGoalName.isBlank() || trimmedTargetAmount.isBlank()) {
            return "Enter a goal name and target amount.";
        }

        if (trimmedGoalName.length() < 2 || trimmedGoalName.length() > 50) {
            return "Goal name must be 2-50 characters.";
        }

        try {
            BigDecimal targetAmount = new BigDecimal(trimmedTargetAmount);
            if (targetAmount.compareTo(BigDecimal.ZERO) <= 0) {
                return "Target amount must be greater than 0.";
            }
        } catch (Exception e) {
            return "Enter a valid target amount.";
        }

        return null;
    }

    public String validateFullNameUpdate(String fullName) {
        String trimmedFullName = safeTrim(fullName);

        if (trimmedFullName.length() < 2) {
            return "Enter a valid full name.";
        }

        return null;
    }

    public String validatePasswordUpdate(String password) {
        String trimmedPassword = safeTrim(password);

        if (trimmedPassword.length() < 6) {
            return "Password must be at least 6 characters.";
        }

        return null;
    }

    public String validatePinUpdate(String pin) {
        String trimmedPin = safeTrim(pin);

        if (!trimmedPin.matches("\\d{4}")) {
            return "PIN must be exactly 4 digits.";
        }

        return null;
    }

    public ProfileInfo getProfileInfo(String username) {
        String sql = "SELECT full_name, username, card_number, routing_number, created_at FROM users WHERE username=?";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new ProfileInfo(
                        rs.getString("full_name"),
                        rs.getString("username"),
                        maskCardNumber(rs.getString("card_number")),
                        rs.getString("routing_number"),
                        rs.getString("created_at"),
                        "ACTIVE"
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ProfileInfo("", safeTrim(username), "", "", "", "");
    }

    public List<SavingsGoal> getSavingsGoals(String username) {
        List<SavingsGoal> goals = new ArrayList<>();
        String sql = "SELECT id, username, goal_name, target_amount, current_amount, status, created_at " +
                "FROM savings_goals WHERE username=? ORDER BY created_at DESC";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    goals.add(new SavingsGoal(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("goal_name"),
                            rs.getString("target_amount"),
                            rs.getString("current_amount"),
                            rs.getString("status"),
                            rs.getString("created_at")
                    ));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return goals;
    }

    public boolean createSavingsGoal(String username, String goalName, String targetAmountText) {
        String validationMessage = validateSavingsGoalInput(goalName, targetAmountText);
        if (validationMessage != null) {
            return false;
        }

        String sql = "INSERT INTO savings_goals (username, goal_name, target_amount, current_amount, status) " +
                "VALUES (?, ?, ?, 0.00, 'IN_PROGRESS')";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, safeTrim(username));
            stmt.setString(2, safeTrim(goalName));
            stmt.setBigDecimal(3, new BigDecimal(safeTrim(targetAmountText)));
            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteSavingsGoal(String username, int goalId) {
        String trimmedUsername = safeTrim(username);
        String selectSql = "SELECT goal_name, current_amount FROM savings_goals WHERE id=? AND username=?";
        String deleteSql = "DELETE FROM savings_goals WHERE id=? AND username=?";

        try (Connection conn = databaseConnection.getConnection()) {
            PreparedStatement selectStmt = conn.prepareStatement(selectSql);
            selectStmt.setInt(1, goalId);
            selectStmt.setString(2, trimmedUsername);

            ResultSet rs = selectStmt.executeQuery();
            if (!rs.next()) {
                return false;
            }

            String goalName = rs.getString("goal_name");
            BigDecimal currentAmount = rs.getBigDecimal("current_amount");

            PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
            deleteStmt.setInt(1, goalId);
            deleteStmt.setString(2, trimmedUsername);

            if (deleteStmt.executeUpdate() <= 0) {
                return false;
            }

            if (currentAmount != null && currentAmount.compareTo(BigDecimal.ZERO) > 0) {
                updateBalance(conn, trimmedUsername, "savings_balance", currentAmount);
                addTransaction(
                        trimmedUsername,
                        "Savings Goal Withdrawal",
                        null,
                        SAVINGS,
                        currentAmount,
                        "Removed from goal: " + goalName
                );
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean contributeToSavingsGoal(String username, int goalId, String fromAccount, BigDecimal amount) {
        username = safeTrim(username);
        fromAccount = safeTrim(fromAccount);

        if (username.isBlank() || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        String balanceColumn = getBalanceColumn(fromAccount);
        if (balanceColumn == null) {
            return false;
        }

        try (Connection conn = databaseConnection.getConnection()) {
            String goalSql = "SELECT goal_name, target_amount, current_amount, status FROM savings_goals WHERE id=? AND username=?";
            PreparedStatement goalStmt = conn.prepareStatement(goalSql);
            goalStmt.setInt(1, goalId);
            goalStmt.setString(2, username);

            ResultSet rs = goalStmt.executeQuery();
            if (!rs.next()) {
                return false;
            }

            if ("COMPLETED".equalsIgnoreCase(rs.getString("status"))) {
                return false;
            }

            String goalName = rs.getString("goal_name");
            BigDecimal targetAmount = rs.getBigDecimal("target_amount");
            BigDecimal currentAmount = rs.getBigDecimal("current_amount");
            BigDecimal availableBalance = getBalance(conn, username, balanceColumn);

            if (availableBalance.compareTo(amount) < 0) {
                return false;
            }

            BigDecimal newCurrentAmount = currentAmount.add(amount);
            String newStatus = getGoalStatus(newCurrentAmount, targetAmount);

            updateBalance(conn, username, balanceColumn, amount.negate());

            String updateGoalSql = "UPDATE savings_goals SET current_amount=?, status=? WHERE id=? AND username=?";
            PreparedStatement updateGoalStmt = conn.prepareStatement(updateGoalSql);
            updateGoalStmt.setBigDecimal(1, newCurrentAmount);
            updateGoalStmt.setString(2, newStatus);
            updateGoalStmt.setInt(3, goalId);
            updateGoalStmt.setString(4, username);

            int rows = updateGoalStmt.executeUpdate();
            if (rows <= 0) {
                return false;
            }

            addTransaction(
                    username,
                    "Savings Goal Contribution",
                    fromAccount,
                    null,
                    amount,
                    "Added to goal: " + goalName
            );

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean withdrawFromSavingsGoal(String username, int goalId, String toAccount, BigDecimal amount) {
        username = safeTrim(username);
        toAccount = safeTrim(toAccount);

        if (username.isBlank() || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        String balanceColumn = getBalanceColumn(toAccount);
        if (balanceColumn == null) {
            return false;
        }

        try (Connection conn = databaseConnection.getConnection()) {
            String goalSql = "SELECT goal_name, target_amount, current_amount FROM savings_goals WHERE id=? AND username=?";
            PreparedStatement goalStmt = conn.prepareStatement(goalSql);
            goalStmt.setInt(1, goalId);
            goalStmt.setString(2, username);

            ResultSet rs = goalStmt.executeQuery();
            if (!rs.next()) {
                return false;
            }

            String goalName = rs.getString("goal_name");
            BigDecimal targetAmount = rs.getBigDecimal("target_amount");
            BigDecimal currentAmount = rs.getBigDecimal("current_amount");

            if (currentAmount.compareTo(amount) < 0) {
                return false;
            }

            BigDecimal newCurrentAmount = currentAmount.subtract(amount);
            String newStatus = getGoalStatus(newCurrentAmount, targetAmount);

            String updateGoalSql = "UPDATE savings_goals SET current_amount=?, status=? WHERE id=? AND username=?";
            PreparedStatement updateGoalStmt = conn.prepareStatement(updateGoalSql);
            updateGoalStmt.setBigDecimal(1, newCurrentAmount);
            updateGoalStmt.setString(2, newStatus);
            updateGoalStmt.setInt(3, goalId);
            updateGoalStmt.setString(4, username);

            int rows = updateGoalStmt.executeUpdate();
            if (rows <= 0) {
                return false;
            }

            updateBalance(conn, username, balanceColumn, amount);

            addTransaction(
                    username,
                    "Savings Goal Withdrawal",
                    null,
                    toAccount,
                    amount,
                    "Removed from goal: " + goalName
            );

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /*
     These update profile settings
     and account information.
    */
    public boolean resetPassword(String username, String newPassword) {
        String validationMessage = validatePasswordUpdate(newPassword);
        if (validationMessage != null) {
            return false;
        }

        return updateUserField("password_hash", safeTrim(newPassword), safeTrim(username));
    }

    public boolean updateFullName(String username, String newFullName) {
        String validationMessage = validateFullNameUpdate(newFullName);
        if (validationMessage != null) {
            return false;
        }

        return updateUserField("full_name", safeTrim(newFullName), safeTrim(username));
    }

    public boolean updatePin(String username, String newPin) {
        String validationMessage = validatePinUpdate(newPin);
        if (validationMessage != null) {
            return false;
        }

        return updateUserField("pin_hash", safeTrim(newPin), safeTrim(username));
    }

    public boolean regenerateCardDetails(String username) {
        String sql = "UPDATE users SET card_number=?, cvc=?, expiration_date=? WHERE username=?";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, generateUniqueCardNumber());
            stmt.setString(2, generateCvc());
            stmt.setString(3, generateExpirationDate());
            stmt.setString(4, safeTrim(username));

            boolean success = stmt.executeUpdate() > 0;
            if (success) {
                addTransaction(safeTrim(username), "Card Regenerated", null, null, BigDecimal.ZERO, "Profile settings updated card details");
            }
            return success;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteAccount(String username) {
        String trimmedUsername = safeTrim(username);
        if (trimmedUsername.isBlank()) {
            return false;
        }

        try (Connection conn = databaseConnection.getConnection()) {
            deleteRequestsForUser(conn, trimmedUsername);
            deleteTransactionsForUser(conn, trimmedUsername);
            deleteSavingsGoalsForUser(conn, trimmedUsername);

            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE username=?")) {
                stmt.setString(1, trimmedUsername);
                return stmt.executeUpdate() > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean updateUserField(String columnName, String value, String username) {
        String sql = "UPDATE users SET " + columnName + "=? WHERE username=?";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, value);
            stmt.setString(2, username);
            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean userExists(String username) {
        String trimmedUsername = safeTrim(username);
        if (trimmedUsername.isBlank()) {
            return false;
        }

        String sql = "SELECT username FROM users WHERE username=?";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, trimmedUsername);
            ResultSet rs = stmt.executeQuery();

            return rs.next();

        } catch (Exception e) {
            return false;
        }
    }

    private boolean isValidTransferPair(String firstUser, String secondUser, BigDecimal amount) {
        if (firstUser.isBlank() || secondUser.isBlank() || amount == null) {
            return false;
        }

        if (firstUser.equals(secondUser)) {
            return false;
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        return userExists(secondUser);
    }

    private String getBalanceColumn(String accountName) {
        if (CHECKING.equalsIgnoreCase(accountName)) {
            return "checking_balance";
        }

        if (SAVINGS.equalsIgnoreCase(accountName)) {
            return "savings_balance";
        }

        return null;
    }

    private String getGoalStatus(BigDecimal currentAmount, BigDecimal targetAmount) {
        if (currentAmount.compareTo(targetAmount) >= 0) {
            return "COMPLETED";
        }

        return "IN_PROGRESS";
    }

    private BigDecimal getBalance(Connection conn, String username, String column) throws SQLException {
        String sql = "SELECT " + column + " FROM users WHERE username=?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, username);

        ResultSet rs = stmt.executeQuery();
        if (rs.next()) return rs.getBigDecimal(1);

        return BigDecimal.ZERO;
    }

    private void updateBalance(Connection conn, String username, String column, BigDecimal amount) throws SQLException {
        String sql = "UPDATE users SET " + column + " = " + column + " + ? WHERE username=?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setBigDecimal(1, amount);
        stmt.setString(2, username);
        stmt.executeUpdate();
    }

    private void addTransaction(String username, String type, String from, String to, BigDecimal amount, String desc) {
        String sql = "INSERT INTO transactions (username, transaction_type, from_account, to_account, amount, description) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, type);
            stmt.setString(3, from);
            stmt.setString(4, to);
            stmt.setBigDecimal(5, amount);
            stmt.setString(6, desc);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getMonthNumber(String monthName) {
        if (monthName == null || monthName.equals("All Months")) return 0;

        switch (monthName) {
            case "January": return 1;
            case "February": return 2;
            case "March": return 3;
            case "April": return 4;
            case "May": return 5;
            case "June": return 6;
            case "July": return 7;
            case "August": return 8;
            case "September": return 9;
            case "October": return 10;
            case "November": return 11;
            case "December": return 12;
            default: return 0;
        }
    }

    private String generatePin() {
        return String.valueOf(1000 + new Random().nextInt(9000));
    }

    private String generateCardNumber() {
        StringBuilder card = new StringBuilder("4");

        for (int i = 0; i < 15; i++) {
            card.append(new Random().nextInt(10));
        }

        return card.toString();
    }

    private String generateUniqueCardNumber() {
        String cardNumber;

        do {
            cardNumber = generateCardNumber();
        } while (cardNumberExists(cardNumber));

        return cardNumber;
    }

    private String generateCvc() {
        return String.valueOf(100 + new Random().nextInt(900));
    }

    private String generateExpirationDate() {
        LocalDate date = LocalDate.now().plusYears(4);
        return String.format("%02d/%02d", date.getMonthValue(), date.getYear() % 100);
    }

    private void deleteRequestsForUser(Connection conn, String username) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "DELETE FROM requests WHERE requester_username=? OR receiver_username=?")) {
            stmt.setString(1, username);
            stmt.setString(2, username);
            stmt.executeUpdate();
        }
    }

    private void deleteTransactionsForUser(Connection conn, String username) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM transactions WHERE username=?")) {
            stmt.setString(1, username);
            stmt.executeUpdate();
        }
    }

    private void deleteSavingsGoalsForUser(Connection conn, String username) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM savings_goals WHERE username=?")) {
            stmt.setString(1, username);
            stmt.executeUpdate();
        }
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private String formatTransactionRecord(ResultSet rs) throws SQLException {
        String timestamp = formatTimestamp(rs.getTimestamp("created_at"));
        String type = simplifyTransactionType(rs.getString("transaction_type"));
        String amount = "$" + rs.getBigDecimal("amount").setScale(2, RoundingMode.HALF_UP);
        String description = cleanTransactionDescription(rs.getString("description"));

        if (description.isBlank()) {
            return timestamp + "  •  " + type + "  •  " + amount;
        }

        return timestamp + "  •  " + type + "  •  " + amount + "  •  " + description;
    }

    private String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) {
            return "";
        }

        LocalDateTime dateTime = timestamp.toLocalDateTime();
        return dateTime.format(TRANSACTION_TIME_FORMAT);
    }

    private String simplifyTransactionType(String transactionType) {
        String type = safeTrim(transactionType);

        switch (type) {
            case "Savings Goal Contribution":
                return "Goal deposit";
            case "Savings Goal Withdrawal":
                return "Goal withdrawal";
            case "Card Regenerated":
                return "Card updated";
            case "Internal Transfer":
                return "Transfer";
            case "Money Requested":
                return "Request sent";
            case "Request Received":
                return "Request received";
            case "Request Accepted":
                return "Request accepted";
            case "Accepted Request":
                return "Request paid";
            case "Request Denied":
                return "Request denied";
            case "Denied Request":
                return "Request declined";
            default:
                return type;
        }
    }

    private String cleanTransactionDescription(String description) {
        String text = safeTrim(description);

        text = text.replace("Removed from goal: ", "");
        text = text.replace("Added to goal: ", "");
        text = text.replace("Profile settings updated card details", "Card details refreshed");

        return text;
    }

    private String maskCardNumber(String cardNumber) {
        String trimmedCardNumber = safeTrim(cardNumber);

        if (trimmedCardNumber.length() < 4) {
            return trimmedCardNumber;
        }

        String lastFour = trimmedCardNumber.substring(trimmedCardNumber.length() - 4);
        return "**** **** **** " + lastFour;
    }

    private boolean cardNumberExists(String cardNumber) {
        String sql = "SELECT card_number FROM users WHERE card_number=?";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, safeTrim(cardNumber));
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (Exception e) {
            return false;
        }
    }
}
