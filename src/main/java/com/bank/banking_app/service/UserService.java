package com.bank.banking_app.service;

import com.bank.banking_app.config.databaseConnection;
import com.bank.banking_app.model.User;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class UserService {

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
            stmt.setBigDecimal(9, new BigDecimal("0.00"));
            stmt.setBigDecimal(10, new BigDecimal("0.00"));

            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

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

            BigDecimal currentBalance = getCurrentBalance(conn, username, fromColumn);

            if (currentBalance.compareTo(amount) < 0) {
                addTransaction(username, "Transfer Failed", fromAccount, toAccount, amount,
                        "Failed transfer due to insufficient funds");
                return false;
            }

            String updateSql = "UPDATE users SET " + fromColumn + " = " + fromColumn + " - ?, " +
                    toColumn + " = " + toColumn + " + ? WHERE username = ?";

            try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                stmt.setBigDecimal(1, amount);
                stmt.setBigDecimal(2, amount);
                stmt.setString(3, username);
                stmt.executeUpdate();
            }

            addTransaction(username, "Transfer", fromAccount, toAccount, amount,
                    "Transfer from " + fromAccount + " to " + toAccount);

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean sendMoney(String senderUsername, String receiverUsername, BigDecimal amount) {
        if (senderUsername.equals(receiverUsername) || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        if (!userExists(receiverUsername)) {
            return false;
        }

        try (Connection conn = databaseConnection.getConnection()) {

            BigDecimal senderBalance = getCurrentBalance(conn, senderUsername, "checking_balance");

            if (senderBalance.compareTo(amount) < 0) {
                addTransaction(senderUsername, "Send Failed", "Checking", null, amount,
                        "Failed to send money to " + receiverUsername + " due to insufficient funds");
                return false;
            }

            String removeFromSender = "UPDATE users SET checking_balance = checking_balance - ? WHERE username = ?";
            String addToReceiver = "UPDATE users SET checking_balance = checking_balance + ? WHERE username = ?";

            try (PreparedStatement stmt = conn.prepareStatement(removeFromSender)) {
                stmt.setBigDecimal(1, amount);
                stmt.setString(2, senderUsername);
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = conn.prepareStatement(addToReceiver)) {
                stmt.setBigDecimal(1, amount);
                stmt.setString(2, receiverUsername);
                stmt.executeUpdate();
            }

            addTransaction(senderUsername, "Sent Money", "Checking", null, amount,
                    "Sent money to " + receiverUsername);

            addTransaction(receiverUsername, "Received Money", null, "Checking", amount,
                    "Received money from " + senderUsername);

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean requestMoney(String requesterUsername, String receiverUsername, BigDecimal amount) {
        if (requesterUsername.equals(receiverUsername) || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        if (!userExists(receiverUsername)) {
            return false;
        }

        String sql = "INSERT INTO requests (requester_username, receiver_username, amount, status) " +
                "VALUES (?, ?, ?, 'PENDING')";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, requesterUsername);
            stmt.setString(2, receiverUsername);
            stmt.setBigDecimal(3, amount);

            stmt.executeUpdate();

            addTransaction(requesterUsername, "Money Requested", null, null, amount,
                    "Requested money from " + receiverUsername);

            addTransaction(receiverUsername, "Request Received", null, null, amount,
                    requesterUsername + " requested money from you");

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<String> getPendingRequests(String username) {
        List<String> requests = new ArrayList<>();

        String sql = "SELECT id, requester_username, amount, created_at FROM requests " +
                "WHERE receiver_username = ? AND status = 'PENDING' ORDER BY created_at DESC";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String request = "Request ID: " + rs.getInt("id")
                        + " | From: " + rs.getString("requester_username")
                        + " | Amount: $" + rs.getString("amount")
                        + " | Date: " + rs.getString("created_at");

                requests.add(request);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return requests;
    }

    public boolean acceptRequest(int requestId) {
        String getRequestSql = "SELECT requester_username, receiver_username, amount, status FROM requests WHERE id = ?";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(getRequestSql)) {

            stmt.setInt(1, requestId);

            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                return false;
            }

            String requester = rs.getString("requester_username");
            String receiver = rs.getString("receiver_username");
            BigDecimal amount = rs.getBigDecimal("amount");
            String status = rs.getString("status");

            if (!status.equals("PENDING")) {
                return false;
            }

            boolean sent = sendMoney(receiver, requester, amount);

            if (!sent) {
                return false;
            }

            String updateSql = "UPDATE requests SET status = 'ACCEPTED' WHERE id = ?";

            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setInt(1, requestId);
                updateStmt.executeUpdate();
            }

            addTransaction(requester, "Request Accepted", null, "Checking", amount,
                    receiver + " accepted your money request");

            addTransaction(receiver, "Accepted Request", "Checking", null, amount,
                    "You accepted money request from " + requester);

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

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

    public List<String> getTransactionsByMonthAndYear(String username, String monthName, String year) {
        List<String> history = new ArrayList<>();

        int monthNumber = getMonthNumber(monthName);

        String sql = "SELECT transaction_type, amount, description, created_at " +
                "FROM transactions WHERE username = ?";

        if (monthNumber > 0) {
            sql += " AND MONTH(created_at) = ?";
        }

        if (!year.isBlank()) {
            sql += " AND YEAR(created_at) = ?";
        }

        sql += " ORDER BY created_at DESC";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int index = 1;

            stmt.setString(index, username);
            index++;

            if (monthNumber > 0) {
                stmt.setInt(index, monthNumber);
                index++;
            }

            if (!year.isBlank()) {
                stmt.setString(index, year);
            }

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

    private boolean userExists(String username) {
        String sql = "SELECT username FROM users WHERE username = ?";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

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

    private String getBalanceColumn(String accountType) {
        if (accountType.equalsIgnoreCase("Checking")) {
            return "checking_balance";
        }

        if (accountType.equalsIgnoreCase("Savings")) {
            return "savings_balance";
        }

        return null;
    }

    private int getMonthNumber(String monthName) {
        if (monthName == null || monthName.equals("All Months")) {
            return 0;
        }

        switch (monthName) {
            case "January":
                return 1;
            case "February":
                return 2;
            case "March":
                return 3;
            case "April":
                return 4;
            case "May":
                return 5;
            case "June":
                return 6;
            case "July":
                return 7;
            case "August":
                return 8;
            case "September":
                return 9;
            case "October":
                return 10;
            case "November":
                return 11;
            case "December":
                return 12;
            default:
                return 0;
        }
    }

    private String generatePin() {
        Random random = new Random();
        return String.valueOf(1000 + random.nextInt(9000));
    }

    private String generateCardNumber() {
        Random random = new Random();

        String cardNumber = "4";

        for (int i = 0; i < 15; i++) {
            cardNumber += random.nextInt(10);
        }

        return cardNumber;
    }

    private String generateCvc() {
        Random random = new Random();
        return String.valueOf(100 + random.nextInt(900));
    }

    private String generateExpirationDate() {
        LocalDate futureDate = LocalDate.now().plusYears(4);

        int month = futureDate.getMonthValue();
        int year = futureDate.getYear() % 100;

        return String.format("%02d/%02d", month, year);
    }
}