package com.bank.banking_app.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class databaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3307/banking_app";
    private static final String USER = "root";
    private static final String PASSWORD = "Itzsal$2$2$2";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
