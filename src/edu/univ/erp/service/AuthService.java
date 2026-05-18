package edu.univ.erp.service;

import edu.univ.erp.auth.CurrentUserSession;
import edu.univ.erp.auth.PasswordUtil;
import edu.univ.erp.data.DatabaseManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Service layer for handling authentication logic.
 * REFACTORED: Uses Username for authentication and session management.
 */
public class AuthService {

    // Removed UserRepository dependency as Profile IDs are no longer needed.

    public AuthService() {
    }

    /**
     * Attempts to log in a user.
     */
    public String login(String username, String password) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return "Username and password cannot be empty.";
        }

        // REFACTORED SQL: No user_id selected
        String sql = "SELECT role, password_hash FROM users_auth WHERE username = ? AND status = 'Active'";

        try (Connection conn = DatabaseManager.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // --- 1. User was found ---
                    String hashedPasswordFromDB = rs.getString("password_hash");

                    // --- 2. Check the password ---
                    if (PasswordUtil.checkPassword(password, hashedPasswordFromDB)) {
                        // --- 3. Password is CORRECT ---
                        String role = rs.getString("role");

                        // --- 4. Create the session (Username only) ---
                        CurrentUserSession.getInstance().createSession(username, role);

                        // --- 5. Update last_login ---
                        updateLastLogin(username);

                        return "SUCCESS";
                    } else {
                        return "Incorrect username or password.";
                    }
                } else {
                    return "Incorrect username or password.";
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error. Please try again later.";
        }
    }

    /**
     * Helper method to update the user's last_login timestamp.
     * REFACTORED: Uses username.
     */
    private void updateLastLogin(String username) {
        String sql = "UPDATE users_auth SET last_login = CURRENT_TIMESTAMP WHERE username = ?";
        try (Connection conn = DatabaseManager.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to update last_login: " + e.getMessage());
        }
    }

    /**
     * Logs out the current user.
     */
    public void logout() {
        CurrentUserSession.getInstance().destroySession();
    }

    /**
     * Changes the password for the currently logged-in user.
     */
    public String changePassword(String oldPassword, String newPassword) {
        // REFACTORED: Get username instead of userId
        String username = CurrentUserSession.getInstance().getUsername();

        if (username == null) return "Error: No user logged in.";
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return "Error: New password cannot be empty.";
        }

        // 1. Verify Old Password
        String verifySql = "SELECT password_hash FROM users_auth WHERE username = ?";
        String updateSql = "UPDATE users_auth SET password_hash = ? WHERE username = ?";

        try (Connection conn = DatabaseManager.getAuthConnection()) {
            // Step A: Fetch current hash
            String currentHash = null;
            try (PreparedStatement stmt = conn.prepareStatement(verifySql)) {
                stmt.setString(1, username);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        currentHash = rs.getString("password_hash");
                    }
                }
            }

            if (currentHash == null) return "Error: User not found.";

            // Step B: Check if old password matches
            if (!PasswordUtil.checkPassword(oldPassword, currentHash)) {
                return "Error: Current password is incorrect.";
            }

            // Step C: Update to new hash
            String newHash = PasswordUtil.hashPassword(newPassword);
            try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                stmt.setString(1, newHash);
                stmt.setString(2, username);
                stmt.executeUpdate();
            }

            return "Password changed successfully!";

        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error: " + e.getMessage();
        }
    }
}