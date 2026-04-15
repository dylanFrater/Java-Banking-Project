package com.bank.banking_app.service;

import com.bank.banking_app.config.databaseConnection;
import com.bank.banking_app.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
}
