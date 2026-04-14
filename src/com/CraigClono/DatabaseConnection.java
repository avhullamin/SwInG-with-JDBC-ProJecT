package com.CraigClono;

import java.sql.*;

public class DatabaseConnection {
    // Connection details - CHANGE THESE!
    private static final String URL = "jdbc:mysql://localhost:3306/craigslist_simple";
    private static final String USER = "root";
    private static final String PASSWORD = "pass123"; // CHANGE THIS TO YOUR PASSWORD!
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver"; // Correct for 9.6.0

    // Load driver
    static {
        try {
            Class.forName(DRIVER);
            System.out.println("✅ MySQL Driver Loaded Successfully!");
        } catch (ClassNotFoundException e) {
            System.out.println("❌ Error: MySQL Driver not found!");
            e.printStackTrace();
        }
    }

    // Get connection
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}