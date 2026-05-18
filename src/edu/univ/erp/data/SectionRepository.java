package edu.univ.erp.data;

import edu.univ.erp.domain.CatalogSection;
import edu.univ.erp.domain.InstructorSection;
import edu.univ.erp.domain.TimetableEntry;
import edu.univ.erp.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * REFACTORED: Handles Section queries using Course Codes and Usernames.
 */
public class SectionRepository {

    /**
     * Fetches sections.
     * Updated to use JOIN on course_code and instructor_username.
     */
    public List<CatalogSection> getCatalogDetails(String programFilter) {
        List<CatalogSection> sections = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT s.section_id, c.code, c.title, c.credits, s.instructor_username, " +
                        "s.day_time, s.room, s.capacity, s.current_enrollment, " +
                        "c.allowed_semesters, s.semester, s.year, s.deadline, " +
                        "i.full_name AS instructor_name " +
                        "FROM sections s " +
                        "JOIN courses c ON s.course_code = c.code " +
                        "LEFT JOIN instructors i ON s.instructor_username = i.username "
        );

        boolean applyFilter = programFilter != null && !programFilter.equalsIgnoreCase("All");
        if (applyFilter) {
            sql.append(" WHERE c.program_type LIKE ? OR c.program_type = 'Core'");
        }

        try (Connection conn = DBConnection.getConnection("erp_db");
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            if (applyFilter) {
                pstmt.setString(1, programFilter + "%");
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                CatalogSection sec = new CatalogSection();
                sec.setSectionId(rs.getInt("section_id"));
                sec.setCourseCode(rs.getString("code"));
                sec.setCourseTitle(rs.getString("title"));
                sec.setCredits(rs.getInt("credits"));
                sec.setInstructorUsername(rs.getString("instructor_username"));
                sec.setDayTime(rs.getString("day_time"));
                sec.setRoom(rs.getString("room"));
                sec.setCapacity(rs.getInt("capacity"));
                sec.setCurrentEnrollment(rs.getInt("current_enrollment"));
                sec.setAllowedSemesters(rs.getString("allowed_semesters"));
                sec.setSemester(rs.getString("semester"));
                sec.setYear(rs.getInt("year"));
                sec.setDeadline(rs.getDate("deadline"));

                String name = rs.getString("instructor_name");
                sec.setInstructorName(name != null ? name : "TBD");
                sections.add(sec);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sections;
    }

    /**
     * Checks if a student is already enrolled in a specific section.
     * Uses student_username.
     */
    public boolean isEnrolled(String studentUsername, int sectionId) {
        String sql = "SELECT 1 FROM enrollments WHERE student_username = ? AND section_id = ?";
        try (Connection conn = DBConnection.getConnection("erp_db");
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentUsername);
            stmt.setInt(2, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int[] getSectionCapacity(int sectionId) {
        String sql = "SELECT current_enrollment, capacity FROM sections WHERE section_id = ?";
        try (Connection conn = DBConnection.getConnection("erp_db");
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new int[]{ rs.getInt("current_enrollment"), rs.getInt("capacity") };
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Registers a student in a section.
     * Uses student_username.
     */
    public boolean registerStudent(String studentUsername, int sectionId) {
        String insertSql = "INSERT INTO enrollments (student_username, section_id, status) VALUES (?, ?, 'Enrolled')";
        String updateSql = "UPDATE sections SET current_enrollment = current_enrollment + 1 WHERE section_id = ?";

        try (Connection conn = DBConnection.getConnection("erp_db")) {
            conn.setAutoCommit(false);
            try (PreparedStatement p1 = conn.prepareStatement(insertSql);
                 PreparedStatement p2 = conn.prepareStatement(updateSql)) {
                p1.setString(1, studentUsername);
                p1.setInt(2, sectionId);
                p1.executeUpdate();

                p2.setInt(1, sectionId);
                p2.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                ex.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Checks if a student is already enrolled in ANY section of the same course.
     */
    public boolean isEnrolledInCourse(String studentUsername, int sectionIdToRegister) {
        // Logic: Find course_code of new section, check if user has any enrollment in a section with same code
        String sql = "SELECT 1 FROM enrollments e " +
                "JOIN sections s ON e.section_id = s.section_id " +
                "WHERE e.student_username = ? " +
                "AND s.course_code = (SELECT course_code FROM sections WHERE section_id = ?)";

        try (Connection conn = DBConnection.getConnection("erp_db");
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentUsername);
            stmt.setInt(2, sectionIdToRegister);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Fetches a list of all sections a student is currently enrolled in.
     */
    public List<TimetableEntry> getEnrolledSections(String studentUsername) {
        List<TimetableEntry> timetable = new ArrayList<>();

        String sql = "SELECT " +
                "    e.enrollment_id, " +
                "    s.section_id, " +
                "    c.code, " +
                "    c.title, " +
                "    s.day_time, " +
                "    s.room, " +
                "    i.full_name AS instructor_name " +
                "FROM enrollments e " +
                "JOIN sections s ON e.section_id = s.section_id " +
                "JOIN courses c ON s.course_code = c.code " +
                "LEFT JOIN instructors i ON s.instructor_username = i.username " +
                "WHERE e.student_username = ?";

        try (Connection conn = DBConnection.getConnection("erp_db");
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, studentUsername);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    TimetableEntry entry = new TimetableEntry();
                    entry.setEnrollmentId(rs.getInt("enrollment_id"));
                    entry.setSectionId(rs.getInt("section_id"));
                    entry.setCourseCode(rs.getString("code"));
                    entry.setCourseTitle(rs.getString("title"));
                    entry.setDayTime(rs.getString("day_time"));
                    entry.setRoom(rs.getString("room"));

                    String name = rs.getString("instructor_name");
                    entry.setInstructorName(name != null ? name : "TBD");
                    timetable.add(entry);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return timetable;
    }

    public boolean dropStudent(int enrollmentId) {
        int sectionId = getSectionIdByEnrollmentId(enrollmentId);
        if(sectionId == 0) return false;

        String deleteSql = "DELETE FROM enrollments WHERE enrollment_id = ?";
        String updateSql = "UPDATE sections SET current_enrollment = current_enrollment - 1 WHERE section_id = ?";

        try (Connection conn = DBConnection.getConnection("erp_db")) {
            conn.setAutoCommit(false);
            try (PreparedStatement p1 = conn.prepareStatement(deleteSql);
                 PreparedStatement p2 = conn.prepareStatement(updateSql)) {
                p1.setInt(1, enrollmentId);
                int rows = p1.executeUpdate();
                if(rows > 0) {
                    p2.setInt(1, sectionId);
                    p2.executeUpdate();
                }
                conn.commit();
                return rows > 0;
            } catch(SQLException ex) {
                conn.rollback();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Fetches all sections taught by a specific instructor.
     * Uses instructor_username.
     */
    public List<InstructorSection> getSectionsForInstructor(String instructorUsername) {
        List<InstructorSection> sections = new ArrayList<>();

        String sql = "SELECT " +
                "    s.section_id, c.code, c.title, s.day_time, s.room, s.current_enrollment " +
                "FROM sections s " +
                "JOIN courses c ON s.course_code = c.code " +
                "WHERE s.instructor_username = ?";

        try (Connection conn = DBConnection.getConnection("erp_db");
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, instructorUsername);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    InstructorSection section = new InstructorSection();
                    section.setSectionId(rs.getInt("section_id"));
                    section.setCourseCode(rs.getString("code"));
                    section.setCourseTitle(rs.getString("title"));
                    section.setDayTime(rs.getString("day_time"));
                    section.setRoom(rs.getString("room"));
                    section.setEnrollmentCount(rs.getInt("current_enrollment"));
                    sections.add(section);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sections;
    }

    public java.sql.Date getSectionDeadline(int sectionId) {
        String sql = "SELECT deadline FROM sections WHERE section_id = ?";
        try (Connection conn = DBConnection.getConnection("erp_db");
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getDate("deadline");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public int getSectionIdByEnrollmentId(int enrollmentId) {
        String sql = "SELECT section_id FROM enrollments WHERE enrollment_id = ?";
        try (Connection conn = DBConnection.getConnection("erp_db");
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, enrollmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("section_id");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }
}