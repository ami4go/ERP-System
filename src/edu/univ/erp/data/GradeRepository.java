package edu.univ.erp.data;

import edu.univ.erp.domain.GradeEntry;
import edu.univ.erp.domain.GradebookEntry;
import edu.univ.erp.domain.GradebookStudent;
import edu.univ.erp.domain.TranscriptEntry;
import edu.univ.erp.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REFACTORED: Handles grades using Usernames and Course Codes.
 */
public class GradeRepository {

    // --- STUDENT METHODS ---

    /**
     * Fetches raw assessment scores for a student.
     * Updated to use studentUsername.
     */
    public List<GradeEntry> getGradesForStudent(String studentUsername) {
        List<GradeEntry> grades = new ArrayList<>();

        String sql = "SELECT " +
                "    c.code, c.title, c.credits, " +
                "    s.semester, s.year, " +
                "    g.component, g.score, g.total_marks, g.weight " +
                "FROM enrollments e " +
                "JOIN sections s ON e.section_id = s.section_id " +
                "JOIN courses c ON s.course_code = c.code " +
                "LEFT JOIN grades g ON e.enrollment_id = g.enrollment_id " +
                "WHERE e.student_username = ? " +
                "ORDER BY s.year DESC, s.semester, c.code, g.component";

        try (Connection conn = DBConnection.getConnection("erp_db");
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, studentUsername);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    GradeEntry entry = new GradeEntry();
                    entry.setCourseCode(rs.getString("code"));
                    entry.setCourseTitle(rs.getString("title"));
                    entry.setCredits(rs.getInt("credits"));
                    entry.setSemester(rs.getString("semester"));
                    entry.setYear(rs.getInt("year"));

                    String comp = rs.getString("component");
                    if (comp != null) {
                        entry.setComponent(comp);
                        entry.setScore(rs.getBigDecimal("score"));
                        entry.setTotalMarks(rs.getBigDecimal("total_marks"));
                        entry.setWeight(rs.getBigDecimal("weight"));
                    } else {
                        entry.setComponent("No assessments yet");
                        entry.setScore(java.math.BigDecimal.ZERO);
                        entry.setTotalMarks(java.math.BigDecimal.ZERO);
                    }
                    grades.add(entry);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return grades;
    }

    /**
     * Fetches all completed courses for a transcript.
     * Updated to use studentUsername.
     */
    public List<TranscriptEntry> getTranscriptForStudent(String studentUsername) {
        List<TranscriptEntry> transcript = new ArrayList<>();

        String sql = "SELECT c.code, c.title, c.credits, e.course_grade " +
                "FROM enrollments e " +
                "JOIN sections s ON e.section_id = s.section_id " +
                "JOIN courses c ON s.course_code = c.code " +
                "WHERE e.student_username = ? AND e.course_grade != 'IP' " +
                "ORDER BY c.code";

        try (Connection conn = DBConnection.getConnection("erp_db");
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, studentUsername);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    TranscriptEntry entry = new TranscriptEntry();
                    entry.setCourseCode(rs.getString("code"));
                    entry.setCourseTitle(rs.getString("title"));
                    entry.setCredits(rs.getInt("credits"));
                    entry.setFinalGrade(rs.getString("course_grade"));
                    transcript.add(entry);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transcript;
    }

    // --- INSTRUCTOR METHODS ---

    /**
     * Fetches all students enrolled in a specific section.
     * Updated to fetch 'username' instead of IDs.
     */
    public List<GradebookStudent> getStudentsForGradebook(int sectionId) {
        List<GradebookStudent> students = new ArrayList<>();

        // FIX: Join on username. Removed student_id and user_id selects.
        String sql = "SELECT e.enrollment_id, s.username, s.roll_no, s.full_name " +
                "FROM enrollments e " +
                "JOIN students s ON e.student_username = s.username " +
                "WHERE e.section_id = ?";

        // Use a map to prevent duplicates
        Map<String, GradebookStudent> studentMap = new HashMap<>();

        try (Connection conn = DBConnection.getConnection("erp_db");
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, sectionId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    GradebookStudent student = new GradebookStudent();
                    student.setEnrollmentId(rs.getInt("enrollment_id"));
                    student.setUsername(rs.getString("username"));
                    student.setRollNo(rs.getString("roll_no"));
                    student.setStudentName(rs.getString("full_name"));

                    studentMap.put(rs.getString("username"), student);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>(studentMap.values());
    }

    // --- CUSTOM GRADING SCALE & SAVE METHODS ---
    // These use sectionId and enrollmentId which are preserved.

    public boolean saveGradingRules(int sectionId, java.util.List<edu.univ.erp.domain.GradingRule> rules) {
        String deleteSql = "DELETE FROM grading_rules WHERE section_id = ?";
        String insertSql = "INSERT INTO grading_rules (section_id, grade_letter, min_percentage, grade_points) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getErpConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
                stmt.setInt(1, sectionId);
                stmt.executeUpdate();
            }
            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                for (edu.univ.erp.domain.GradingRule rule : rules) {
                    stmt.setInt(1, sectionId);
                    stmt.setString(2, rule.getGrade());
                    stmt.setDouble(3, rule.getMinPercentage());
                    stmt.setDouble(4, rule.getGradePoints());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public java.util.List<edu.univ.erp.domain.GradingRule> getGradingRules(int sectionId) {
        java.util.List<edu.univ.erp.domain.GradingRule> rules = new ArrayList<>();
        String sql = "SELECT grade_letter, min_percentage, grade_points FROM grading_rules WHERE section_id = ? ORDER BY min_percentage DESC";
        try (Connection conn = DatabaseManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    rules.add(new edu.univ.erp.domain.GradingRule(
                            rs.getString("grade_letter"),
                            rs.getDouble("min_percentage"),
                            rs.getDouble("grade_points")
                    ));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return rules;
    }

    public boolean updateFinalGradeAndPoints(int enrollmentId, String finalGrade, double points) {
        String sql = "UPDATE grades SET final_grade = ?, grade_points = ? WHERE enrollment_id = ?";
        try (Connection conn = DatabaseManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, finalGrade);
            stmt.setDouble(2, points);
            stmt.setInt(3, enrollmentId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<GradebookEntry> getGradesForSection(int sectionId) {
        List<GradebookEntry> grades = new ArrayList<>();
        String sql = "SELECT g.grade_id, g.enrollment_id, g.component, g.score, g.total_marks, g.weight " +
                "FROM grades g " +
                "JOIN enrollments e ON g.enrollment_id = e.enrollment_id " +
                "WHERE e.section_id = ?";
        try (Connection conn = DBConnection.getConnection("erp_db");
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    GradebookEntry entry = new GradebookEntry();
                    entry.setGradeId(rs.getInt("grade_id"));
                    entry.setEnrollmentId(rs.getInt("enrollment_id"));
                    entry.setComponent(rs.getString("component"));
                    entry.setScore(rs.getBigDecimal("score"));
                    entry.setTotalMarks(rs.getBigDecimal("total_marks"));
                    entry.setWeight(rs.getBigDecimal("weight"));
                    grades.add(entry);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return grades;
    }

    public List<String> getAssessmentComponents(int sectionId) {
        List<String> components = new ArrayList<>();
        String sql = "SELECT DISTINCT g.component " +
                "FROM grades g " +
                "JOIN enrollments e ON g.enrollment_id = e.enrollment_id " +
                "WHERE e.section_id = ? " +
                "ORDER BY g.component";
        try (Connection conn = DBConnection.getConnection("erp_db");
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    components.add(rs.getString("component"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return components;
    }

    public boolean saveGrades(List<GradebookEntry> gradesToSave) {
        String sql = "INSERT INTO grades (enrollment_id, component, score, total_marks, weight) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE score = VALUES(score), " +
                "total_marks = VALUES(total_marks), weight = VALUES(weight)";
        try (Connection conn = DatabaseManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            for (GradebookEntry grade : gradesToSave) {
                stmt.setInt(1, grade.getEnrollmentId());
                stmt.setString(2, grade.getComponent());
                stmt.setBigDecimal(3, grade.getScore());
                stmt.setBigDecimal(4, grade.getTotalMarks());
                stmt.setBigDecimal(5, grade.getWeight());
                stmt.addBatch();
            }
            stmt.executeBatch();
            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateFinalGrade(int enrollmentId, String finalGrade) {
        String sql = "UPDATE grades SET final_grade = ? WHERE enrollment_id = ?";
        try (Connection conn = DatabaseManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, finalGrade);
            stmt.setInt(2, enrollmentId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteAssessmentComponent(int sectionId, String componentName) {
        String sql = "DELETE g FROM grades g " +
                "JOIN enrollments e ON g.enrollment_id = e.enrollment_id " +
                "WHERE e.section_id = ? AND g.component = ?";
        try (Connection conn = DatabaseManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sectionId);
            stmt.setString(2, componentName);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}