package edu.univ.erp.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // Database Credentials
    private static final String URL_PREFIX = "jdbc:mysql://localhost:3306/";
    private static final String USER = "root";      // Default MySQL username
    private static final String PASS = "India@123";  // <--- CHANGE THIS to your MySQL password (often "root" or empty "")

    /**
     * Connects to the specified database.
     * @param dbName The name of the database (e.g., "auth_db" or "erp_db")
     * @return A Connection object
     */
    public static Connection getConnection(String dbName) throws SQLException {
        try {
            // 1. Load the Driver (Optional in newer Java/JDBC versions but good for safety)
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 2. Establish Connection
            return DriverManager.getConnection(URL_PREFIX + dbName + "?useSSL=false&allowPublicKeyRetrieval=true", USER, PASS);

        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL Driver not found. Add the connector JAR to libraries.", e);
        }
    }
}