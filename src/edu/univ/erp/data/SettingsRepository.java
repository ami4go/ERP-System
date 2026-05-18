package edu.univ.erp.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SettingsRepository {

    /**
     * Checks if Maintenance Mode is currently turned ON.
     * @return true if maintenance is ON, false otherwise.
     */
    public boolean isMaintenanceModeOn() {
        String sql = "SELECT setting_value FROM settings WHERE setting_key = 'maintenance_on'";

        try (Connection conn = DatabaseManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return "true".equalsIgnoreCase(rs.getString("setting_value"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Updates the maintenance mode setting.
     * @param isEnabled true to turn ON, false to turn OFF
     * @return true if successful
     */
    public boolean setMaintenanceMode(boolean isEnabled) {
        String sql = "INSERT INTO settings (setting_key, setting_value) VALUES ('maintenance_on', ?) " +
                "ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value)";

        String value = isEnabled ? "true" : "false";

        try (Connection conn = DatabaseManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, value);
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}