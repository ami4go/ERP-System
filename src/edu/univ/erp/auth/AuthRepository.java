package edu.univ.erp.auth;

import edu.univ.erp.data.DatabaseManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handles fetching user-related data from the auth_db.
 * Note: AuthService is for *authenticating*, this is for *fetching data*.
 */
public class AuthRepository {

    /**
     * Fetches full names for a given set of usernames.
     * Refactored to use 'username' instead of 'user_id'.
     *
     * @param usernames A Set of usernames (Strings)
     * @return A Map where key is username and value is Full Name
     */
    public Map<String, String> getFullNamesByUsernames(Set<String> usernames) {
        if (usernames == null || usernames.isEmpty()) return new HashMap<>();

        // 1. Prepare the IN clause safely (Wrapping strings in quotes)
        String inClause = usernames.stream()
                .map(u -> "'" + u.replace("'", "''") + "'") // Basic escape for safety
                .collect(Collectors.joining(","));

        String sql = "SELECT username, full_name FROM users_auth WHERE username IN (" + inClause + ")";

        Map<String, String> map = new HashMap<>();

        try (Connection conn = edu.univ.erp.util.DBConnection.getConnection("auth_db");
             java.sql.Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                map.put(rs.getString("username"), rs.getString("full_name"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }
}