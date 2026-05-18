package edu.univ.erp.data;

import edu.univ.erp.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Fetches user profile data from the erp_db (e.g., students, instructors).
 * REFACTORED: Uses Username directly as the lookup key.
 */
public class UserRepository {

    // ============================================================
    // PROFILE DATA FETCHERS (Now using Username)
    // ============================================================

    /**
     * Fetches the program for a specific student.
     * @param username The student's username
     */
    public String getStudentProgram(String username) {
        String sql = "SELECT program FROM students WHERE username = ?";
        try (Connection conn = DBConnection.getConnection("erp_db");
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getString("program");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getStudentYear(String username) {
        String sql = "SELECT year FROM students WHERE username = ?";
        try (Connection conn = DBConnection.getConnection("erp_db");
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("year");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 2024; // Default fallback
    }

    public int getStudentCurrentSemester(String username) {
        String query = "SELECT current_semester FROM students WHERE username = ?";
        try (Connection conn = DBConnection.getConnection("erp_db");
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("current_semester");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1; // Default fallback
    }
}