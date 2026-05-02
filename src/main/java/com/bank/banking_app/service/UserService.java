package com.bank.banking_app.service;

import com.bank.banking_app.config.databaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class UserService {

    // ================= CREATE USER =================
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

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ================= LOGIN =================
    public boolean loginUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password_hash = ?";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ================= GET BALANCES =================
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

    // ================= GET FULL NAME =================
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

    // ================= SEND MONEY =================
    public boolean sendMoney(String sender, String receiver, BigDecimal amount) {
        if (sender.equals(receiver) || amount.compareTo(BigDecimal.ZERO) <= 0) return false;

        if (!userExists(receiver)) return false;

        try (Connection conn = databaseConnection.getConnection()) {

            BigDecimal senderBalance = getBalance(conn, sender, "checking_balance");

            if (senderBalance.compareTo(amount) < 0) {
                addTransaction(sender, "Send Failed", "Checking", null, amount,
                        "Failed to send to " + receiver + " due to insufficient funds");
                return false;
            }

            updateBalance(conn, sender, "checking_balance", amount.negate());
            updateBalance(conn, receiver, "checking_balance", amount);

            addTransaction(sender, "Sent Money", "Checking", null, amount, "Sent to " + receiver);
            addTransaction(receiver, "Received Money", null, "Checking", amount, "From " + sender);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ================= REQUEST MONEY =================
    public boolean requestMoney(String requester, String receiver, BigDecimal amount) {
        if (requester.equals(receiver) || amount.compareTo(BigDecimal.ZERO) <= 0) return false;

        if (!userExists(receiver)) return false;

        String sql = "INSERT INTO requests (requester_username, receiver_username, amount, status) VALUES (?, ?, ?, 'PENDING')";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, requester);
            stmt.setString(2, receiver);
            stmt.setBigDecimal(3, amount);
            stmt.executeUpdate();

            addTransaction(requester, "Money Requested", null, null, amount,
                    "Requested money from " + receiver);
            addTransaction(receiver, "Request Received", null, null, amount,
                    requester + " requested money from you");

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ================= INTERNAL TRANSFER =================
    public boolean transferBetweenAccounts(String username, String from, String to, BigDecimal amount) {
        if (from == null || to == null || amount == null) return false;
        if (from.equals(to) || amount.compareTo(BigDecimal.ZERO) <= 0) return false;

        String fromCol = from.equals("Checking") ? "checking_balance" : "savings_balance";
        String toCol = to.equals("Checking") ? "checking_balance" : "savings_balance";

        try (Connection conn = databaseConnection.getConnection()) {

            BigDecimal balance = getBalance(conn, username, fromCol);
            if (balance.compareTo(amount) < 0) {
                addTransaction(username, "Internal Transfer Failed", from, to, amount,
                        "Failed internal transfer due to insufficient funds");
                return false;
            }

            updateBalance(conn, username, fromCol, amount.negate());
            updateBalance(conn, username, toCol, amount);

            addTransaction(username, "Internal Transfer", from, to, amount,
                    "Moved from " + from + " to " + to);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ================= ACCEPT REQUEST =================
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

            boolean sent = sendMoney(receiver, requester, amount);
            if (!sent) return false;

            PreparedStatement update = conn.prepareStatement("UPDATE requests SET status='ACCEPTED' WHERE id=?");
            update.setInt(1, id);
            update.executeUpdate();

            addTransaction(requester, "Request Accepted", null, "Checking", amount,
                    receiver + " accepted your money request");
            addTransaction(receiver, "Accepted Request", "Checking", null, amount,
                    "You accepted money request from " + requester);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ================= GET REQUESTS =================
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

    // ================= TRANSACTION HISTORY =================
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

        } catch (Exception e) {
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

        if (year != null && !year.isBlank()) {
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

            if (year != null && !year.isBlank()) {
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

        } catch (Exception e) {
            e.printStackTrace();
        }

        return history;
    }

    // ================= HELPER METHODS =================
    private boolean userExists(String username) {
        String sql = "SELECT username FROM users WHERE username=?";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
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

    // ================= GENERATORS =================
    private String generatePin() {
        return String.valueOf(1000 + new Random().nextInt(9000));
    }

    private String generateCardNumber() {
        StringBuilder card = new StringBuilder("4");
        for (int i = 0; i < 15; i++) card.append(new Random().nextInt(10));
        return card.toString();
    }

    private String generateCvc() {
        return String.valueOf(100 + new Random().nextInt(900));
    }

    private String generateExpirationDate() {
        LocalDate date = LocalDate.now().plusYears(4);
        return String.format("%02d/%02d", date.getMonthValue(), date.getYear() % 100);
    }
}