package com.bank.banking_app.service;

import com.bank.banking_app.config.databaseConnection;
import com.bank.banking_app.model.User;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class UserService {

    // Creates a new user manually if another part of the project uses User objects
    public void createUser(User user) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, pin_hash, card_number, full_name, cvc, expiration_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getPin());
            stmt.setString(4, user.getCardNumber());
            stmt.setString(5, user.getFullName());
            stmt.setString(6, user.getCvc());
            stmt.setString(7, user.getExpirationDate());

            stmt.executeUpdate();
        }
    }

    // Registers a new user from the signup screen
    public boolean registerUser(String fullName, String username, String password) {
        String sql = "INSERT INTO users " +
                "(username, password_hash, pin_hash, card_number, full_name, cvc, expiration_date, routing_number, savings_balance, checking_balance) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, generatePin());
            stmt.setString(4, generateCardNumber());
            stmt.setString(5, fullName);
            stmt.setString(6, generateCvc());
            stmt.setString(7, generateExpirationDate());
            stmt.setString(8, "000000000");

            // New users start with $0.00 in both accounts
            stmt.setBigDecimal(9, new BigDecimal("0.00"));
            stmt.setBigDecimal(10, new BigDecimal("0.00"));

            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Checks username and password against the database
    public boolean loginUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password_hash = ?";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Gets checking and savings balances for the logged-in user
    public Map<String, String> getBalances(String username) {
        Map<String, String> balances = new HashMap<>();

        String sql = "SELECT checking_balance, savings_balance FROM users WHERE username = ?";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                balances.put("checking", rs.getString("checking_balance"));
                balances.put("savings", rs.getString("savings_balance"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return balances;
    }

    // Transfers money between checking and savings
    public boolean transfer(String username, String fromAccount, String toAccount, BigDecimal amount) {
        if (fromAccount.equals(toAccount) || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        String fromColumn = getBalanceColumn(fromAccount);
        String toColumn = getBalanceColumn(toAccount);

        if (fromColumn == null || toColumn == null) {
            return false;
        }

        try (Connection conn = databaseConnection.getConnection()) {

            // 1. Check current balance first
            BigDecimal currentBalance = getCurrentBalance(conn, username, fromColumn);

            if (currentBalance.compareTo(amount) < 0) {
                addTransaction(username, "Transfer Failed", fromAccount, toAccount, amount,
                        "Failed transfer due to insufficient funds");
                return false;
            }

            // 2. Update balances
            String updateSql = "UPDATE users SET " + fromColumn + " = " + fromColumn + " - ?, " +
                    toColumn + " = " + toColumn + " + ? WHERE username = ?";

            try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                stmt.setBigDecimal(1, amount);
                stmt.setBigDecimal(2, amount);
                stmt.setString(3, username);
                stmt.executeUpdate();
            }

            // 3. Save transaction history
            addTransaction(username, "Transfer", fromAccount, toAccount, amount,
                    "Transfer from " + fromAccount + " to " + toAccount);

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Gets all transactions for the logged-in user
    public List<String> getTransactionHistory(String username) {
        List<String> history = new ArrayList<>();

        String sql = "SELECT transaction_type, amount, description, created_at " +
                "FROM transactions WHERE username = ? ORDER BY created_at DESC";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String transaction = rs.getString("created_at")
                        + " | " + rs.getString("transaction_type")
                        + " | $" + rs.getString("amount")
                        + " | " + rs.getString("description");

                history.add(transaction);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return history;
    }

    // Helper method: gets one balance from database
    private BigDecimal getCurrentBalance(Connection conn, String username, String balanceColumn) throws SQLException {
        String sql = "SELECT " + balanceColumn + " FROM users WHERE username = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getBigDecimal(balanceColumn);
            }
        }

        return BigDecimal.ZERO;
    }

    // Helper method: saves a transaction into the transactions table
    private void addTransaction(String username, String type, String fromAccount, String toAccount,
                                BigDecimal amount, String description) {
        String sql = "INSERT INTO transactions (username, transaction_type, from_account, to_account, amount, description) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, type);
            stmt.setString(3, fromAccount);
            stmt.setString(4, toAccount);
            stmt.setBigDecimal(5, amount);
            stmt.setString(6, description);

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Helper method: converts account name to database column name
    private String getBalanceColumn(String accountType) {
        if (accountType.equalsIgnoreCase("Checking")) {
            return "checking_balance";
        }

        if (accountType.equalsIgnoreCase("Savings")) {
            return "savings_balance";
        }

        return null;
    }

    // Generates a random 4-digit PIN
    private String generatePin() {
        Random random = new Random();
        return String.valueOf(1000 + random.nextInt(9000));
    }

    // Generates a simple 16-digit card number
    private String generateCardNumber() {
        Random random = new Random();
        return "4" + String.valueOf(System.currentTimeMillis()).substring(0, 12)
                + random.nextInt(100);
    }

    // Generates a random 3-digit CVC
    private String generateCvc() {
        Random random = new Random();
        return String.valueOf(100 + random.nextInt(900));
    }

    // Generates an expiration date 4 years from now
    private String generateExpirationDate() {
        LocalDate futureDate = LocalDate.now().plusYears(4);
        int month = futureDate.getMonthValue();
        int year = futureDate.getYear() % 100;

        return String.format("%02d/%02d", month, year);
    }
}