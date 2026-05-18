package edu.univ.erp.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Manages database connections for both Auth and ERP databases.
 * Uses HikariCP for efficient connection pooling.
 */
public class DatabaseManager {

    private static HikariDataSource authDataSource;
    private static HikariDataSource erpDataSource;

    // --- Database Configuration ---
    // !!! IMPORTANT: Change these values if your setup is different !!!
    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "3306";
    private static final String DB_USER = "root"; // Or your MySQL username
    private static final String DB_PASS = "India@123"; // Or your MySQL password

    private static final String AUTH_DB_NAME = "auth_db";
    private static final String ERP_DB_NAME = "erp_db";
    // -------------------------------

    static {
        // Initialize the Auth DB connection pool
        HikariConfig authConfig = new HikariConfig();
        authConfig.setJdbcUrl("jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + AUTH_DB_NAME);
        authConfig.setUsername(DB_USER);
        authConfig.setPassword(DB_PASS);
        authConfig.addDataSourceProperty("cachePrepStmts", "true");
        authConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        authConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        authDataSource = new HikariDataSource(authConfig);

        // Initialize the ERP DB connection pool
        HikariConfig erpConfig = new HikariConfig();
        erpConfig.setJdbcUrl("jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + ERP_DB_NAME);
        erpConfig.setUsername(DB_USER);
        erpConfig.setPassword(DB_PASS);
        erpConfig.addDataSourceProperty("cachePrepStmts", "true");
        erpConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        erpConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        erpDataSource = new HikariDataSource(erpConfig);
    }

    /**
     * Gets a connection from the Auth DB pool.
     * @return A database connection
     * @throws SQLException
     */
    public static Connection getAuthConnection() throws SQLException {
        return authDataSource.getConnection();
    }

    /**
     * Gets a connection from the ERP DB pool.
     * @return A database connection
     * @throws SQLException
     */
    public static Connection getErpConnection() throws SQLException {
        return erpDataSource.getConnection();
    }

    // Call this when your application shuts down to clean up.
    public static void closeDataSources() {
        if (authDataSource != null) {
            authDataSource.close();
        }
        if (erpDataSource != null) {
            erpDataSource.close();
        }
    }
}