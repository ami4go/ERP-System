package edu.univ.erp.data;

import edu.univ.erp.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SystemSettingsRepository {

    /**
     * Fetches a setting value. Returns default if not found or error occurs.
     */
    public String getSetting(String key) {
        String query = "SELECT setting_value FROM system_settings WHERE setting_key = ?";
        try (Connection conn = DBConnection.getConnection("erp_db");
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, key);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("setting_value");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Return null if not found
    }

    /**
     * Robust Save: Inserts if new, Updates if exists.
     */
    public boolean updateSetting(String key, String value) {
        // MySQL syntax to handle "Create or Update" in one go
        String query = "INSERT INTO system_settings (setting_key, setting_value) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value)";

        try (Connection conn = DBConnection.getConnection("erp_db");
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, key);
            pstmt.setString(2, value);

            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}