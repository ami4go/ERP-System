package edu.univ.erp.data;

import edu.univ.erp.domain.AdminUser;
import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Section;
import edu.univ.erp.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * REFACTORED: Handles Admin DB operations using Usernames and Course Codes.
 * FIX: createSection now returns boolean to match AdminService logic.
 */
public class AdminRepository {

    // --- 1. AUTH METHODS ---

    public boolean createUserAuth(String fullName, String username, String role, String passwordHash) {
        String sql = "INSERT INTO users_auth (username, full_name, role, password_hash, status) VALUES (?, ?, ?, ?, 'Active')";
        try (Connection conn = DBConnection.getConnection("auth_db");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, fullName);
            pstmt.setString(3, role);
            pstmt.setString(4, passwordHash);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean resetPassword(String username, String passwordHash) {
        String sql = "UPDATE users_auth SET password_hash = ? WHERE username = ?";
        try (Connection conn = DBConnection.getConnection("auth_db");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, passwordHash);
            pstmt.setString(2, username);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean deleteUser(String username) {
        // 1. FIRST: Clean up the ERP Database
        try (Connection conn = DBConnection.getConnection("erp_db")) {

            // A. Delete Grades linked to this user's enrollments
            String deleteGradesSql = "DELETE g FROM grades g " +
                    "JOIN enrollments e ON g.enrollment_id = e.enrollment_id " +
                    "WHERE e.student_username = ?";
            try (PreparedStatement p = conn.prepareStatement(deleteGradesSql)) {
                p.setString(1, username);
                p.executeUpdate();
            }

            // B. Delete Enrollments
            try (PreparedStatement p = conn.prepareStatement("DELETE FROM enrollments WHERE student_username = ?")) {
                p.setString(1, username);
                p.executeUpdate();
            }

            // C. Delete Profiles
            try (PreparedStatement p = conn.prepareStatement("DELETE FROM students WHERE username = ?")) {
                p.setString(1, username);
                p.executeUpdate();
            }
            try (PreparedStatement p = conn.prepareStatement("DELETE FROM instructors WHERE username = ?")) {
                p.setString(1, username);
                p.executeUpdate();
            }
            try (PreparedStatement p = conn.prepareStatement("DELETE FROM admins WHERE username = ?")) {
                p.setString(1, username);
                p.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 2. SECOND: Delete the Login from Auth DB
        String sql = "DELETE FROM users_auth WHERE username = ?";
        try (Connection conn = DBConnection.getConnection("auth_db");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    // --- 2. PROFILE METHODS ---

    public boolean createStudentProfile(String username, String rollNo, String program, int year, String fullName) {
        int semester = calculateSemester(year);
        String sql = "INSERT INTO students (username, roll_no, program, year, full_name, current_semester) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection("erp_db");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, rollNo);
            pstmt.setString(3, program);
            pstmt.setInt(4, year);
            pstmt.setString(5, fullName);
            pstmt.setInt(6, semester);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean createInstructorProfile(String username, String department, String title, String fullName) {
        String sql = "INSERT INTO instructors (username, department, title, full_name) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection("erp_db");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, department);
            pstmt.setString(3, title);
            pstmt.setString(4, fullName);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean createAdminProfile(String username, String fullName) {
        String sql = "INSERT INTO admins (username, full_name) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection("erp_db");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, fullName);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateStudent(String username, String name, String roll, String prog, int yr) {
        updateAuthName(username, name);
        int newSemester = calculateSemester(yr);
        String sql = "UPDATE students SET full_name=?, roll_no=?, program=?, year=?, current_semester=? WHERE username=?";
        try (Connection conn = DBConnection.getConnection("erp_db");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, roll);
            pstmt.setString(3, prog);
            pstmt.setInt(4, yr);
            pstmt.setInt(5, newSemester);
            pstmt.setString(6, username);
            return pstmt.executeUpdate() > 0;
        } catch(Exception e) { return false; }
    }

    public boolean updateInstructor(String username, String name, String dept, String title) {
        updateAuthName(username, name);
        String sql = "UPDATE instructors SET full_name=?, department=?, title=? WHERE username=?";
        try (Connection conn = DBConnection.getConnection("erp_db");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, dept);
            pstmt.setString(3, title);
            pstmt.setString(4, username);
            return pstmt.executeUpdate() > 0;
        } catch(Exception e) { return false; }
    }

    public boolean updateAdmin(String username, String name) {
        try (Connection conn = DBConnection.getConnection("erp_db");
             PreparedStatement pstmt = conn.prepareStatement("UPDATE admins SET full_name=? WHERE username=?")) {
            pstmt.setString(1, name);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        } catch(Exception e) { }
        return updateAuthName(username, name);
    }

    private boolean updateAuthName(String username, String name) {
        try (Connection conn = DBConnection.getConnection("auth_db");
             PreparedStatement pstmt = conn.prepareStatement("UPDATE users_auth SET full_name=? WHERE username=?")) {
            pstmt.setString(1, name);
            pstmt.setString(2, username);
            return pstmt.executeUpdate() > 0;
        } catch(Exception e) { return false; }
    }

    private int calculateSemester(int admissionYear) {
        int currentSystemYear = 2025;
        String term = "Monsoon";
        try (Connection conn = DBConnection.getConnection("erp_db")) {
            try (PreparedStatement p = conn.prepareStatement("SELECT setting_value FROM system_settings WHERE setting_key='current_year'")) {
                ResultSet rs = p.executeQuery();
                if(rs.next()) currentSystemYear = Integer.parseInt(rs.getString("setting_value"));
            }
            try (PreparedStatement p = conn.prepareStatement("SELECT setting_value FROM system_settings WHERE setting_key='current_term'")) {
                ResultSet rs = p.executeQuery();
                if(rs.next()) term = rs.getString("setting_value");
            }
        } catch(Exception e) {}

        int diff = currentSystemYear - admissionYear;
        int semester;
        if ("Monsoon".equalsIgnoreCase(term)) {
            semester = (diff * 2) + 1;
        } else {
            semester = diff * 2;
        }
        if (semester < 1) semester = 1;
        return semester;
    }

    // --- 3. DATA RETRIEVAL ---

    public List<AdminUser> getUsersByRole(String role) {
        List<AdminUser> users = new ArrayList<>();
        String sql = "SELECT username, full_name FROM users_auth WHERE role = ? ORDER BY username";

        try (Connection conn = DBConnection.getConnection("auth_db");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, role);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                AdminUser u = new AdminUser();
                u.setUsername(rs.getString("username"));
                u.setFullName(rs.getString("full_name"));

                if ("Student".equals(role)) enrichStudentData(u);
                if ("Instructor".equals(role)) enrichInstructorData(u);
                users.add(u);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return users;
    }

    private void enrichStudentData(AdminUser u) {
        String sql = "SELECT roll_no, program, year, current_semester FROM students WHERE username = ?";
        try (Connection conn = DBConnection.getConnection("erp_db");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, u.getUsername());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                u.setRollNo(rs.getString("roll_no"));
                u.setProgram(rs.getString("program"));
                u.setYear(rs.getInt("year"));
                u.setCurrentSemester(rs.getInt("current_semester"));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void enrichInstructorData(AdminUser u) {
        try (Connection conn = DBConnection.getConnection("erp_db");
             PreparedStatement pstmt = conn.prepareStatement("SELECT department, title FROM instructors WHERE username = ?")) {
            pstmt.setString(1, u.getUsername());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                u.setDepartment(rs.getString("department"));
                u.setTitle(rs.getString("title"));
            }
        } catch (Exception e) {}
    }

    // --- 4. DROPDOWN HELPERS ---

    public List<Course> getAllCourses() {
        List<Course> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection("erp_db");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM courses")) {
            while(rs.next()) {
                list.add(new Course(
                        rs.getString("code"),
                        rs.getString("title"),
                        rs.getInt("credits"),
                        rs.getString("program_type"),
                        rs.getString("allowed_semesters")
                ));
            }
        } catch(Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<AdminUser> getInstructorList() {
        List<AdminUser> list = new ArrayList<>();
        String sql = "SELECT username, full_name FROM instructors";
        try (Connection conn = DBConnection.getConnection("erp_db");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                AdminUser u = new AdminUser();
                u.setUsername(rs.getString("username"));
                u.setFullName(rs.getString("full_name"));
                list.add(u);
            }
        } catch(Exception e) { e.printStackTrace(); }
        return list;
    }

    // --- 5. COURSE & SECTION WRITES ---

    public boolean courseCodeExists(String code) {
        try (Connection conn = DBConnection.getConnection("erp_db");
             PreparedStatement p = conn.prepareStatement("SELECT 1 FROM courses WHERE code=?")) {
            p.setString(1, code);
            return p.executeQuery().next();
        } catch(Exception e) { return false; }
    }

    public boolean createCourse(String code, String title, int credits, String type, String sems) {
        try (Connection conn = DBConnection.getConnection("erp_db");
             PreparedStatement p = conn.prepareStatement("INSERT INTO courses (code, title, credits, program_type, allowed_semesters) VALUES (?,?,?,?,?)")) {
            p.setString(1, code);
            p.setString(2, title);
            p.setInt(3, credits);
            p.setString(4, type);
            p.setString(5, sems);
            return p.executeUpdate() > 0;
        } catch(Exception e) { return false; }
    }

    public boolean deleteCourse(String code) {
        try (Connection conn = DBConnection.getConnection("erp_db")) {
            PreparedStatement psSection = conn.prepareStatement("DELETE FROM sections WHERE course_code=?");
            psSection.setString(1, code);
            psSection.executeUpdate();

            PreparedStatement psCourse = conn.prepareStatement("DELETE FROM courses WHERE code=?");
            psCourse.setString(1, code);
            return psCourse.executeUpdate() > 0;
        } catch(Exception e) { return false; }
    }

    public String deleteSection(int sectionId) {
        try (Connection conn = DBConnection.getConnection("erp_db")) {

            // 1. SAFETY CHECK: Are students enrolled?
            String checkSql = "SELECT COUNT(*) FROM enrollments WHERE section_id = ?";
            try (PreparedStatement pCheck = conn.prepareStatement(checkSql)) {
                pCheck.setInt(1, sectionId);
                try (ResultSet rs = pCheck.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        // FAIL FAST: Block the deletion
                        return "Error: Cannot delete section. " + rs.getInt(1) + " students are currently enrolled.";
                    }
                }
            }

            // 2. If Safe (Count == 0), Proceed to Delete
            Statement stmt = conn.createStatement();

            // Clean up Gradebook Structure (Assessments/Scales) only
            stmt.executeUpdate("DELETE FROM grading_scale WHERE section_id=" + sectionId);
            stmt.executeUpdate("DELETE FROM assessments WHERE section_id=" + sectionId);

            // Delete the Section
            int rows = stmt.executeUpdate("DELETE FROM sections WHERE section_id=" + sectionId);

            return rows > 0 ? "Section deleted successfully." : "Failed to delete section.";

        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
    public boolean createSection(String courseCode, String instructorUsername, String dayTime, String room, int capacity, String sem, int year, String deadlineStr) {
        try {
            java.sql.Date.valueOf(deadlineStr);
        } catch (IllegalArgumentException e) {
            return false;
        }

        try {
            if (capacity < 1) return false;

            String sql = "INSERT INTO sections (course_code, instructor_username, day_time, room, capacity, semester, year, deadline) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DBConnection.getConnection("erp_db");
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, courseCode);
                pstmt.setString(2, instructorUsername);
                pstmt.setString(3, dayTime);
                pstmt.setString(4, room);
                pstmt.setInt(5, capacity);
                pstmt.setString(6, sem);
                pstmt.setInt(7, year);
                pstmt.setDate(8, java.sql.Date.valueOf(deadlineStr));
                return pstmt.executeUpdate() > 0;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public List<Section> getAllSections() {
        List<Section> list = new ArrayList<>();
        String sql = "SELECT s.*, c.code, i.full_name, s.semester, s.year " +
                "FROM sections s " +
                "JOIN courses c ON s.course_code = c.code " +
                "LEFT JOIN instructors i ON s.instructor_username = i.username " +
                "WHERE s.semester = (SELECT setting_value FROM system_settings WHERE setting_key='current_term') " +
                "AND s.year = (SELECT setting_value FROM system_settings WHERE setting_key='current_year')";

        try (Connection conn = DBConnection.getConnection("erp_db");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                Section s = new Section();
                s.setSectionId(rs.getInt("section_id"));
                s.setCourseCode(rs.getString("code"));
                s.setInstructorName(rs.getString("full_name"));
                s.setSemester(rs.getString("semester"));
                s.setYear(rs.getInt("year"));
                s.setRoom(rs.getString("room"));
                s.setDayTime(rs.getString("day_time"));
                s.setCapacity(rs.getInt("capacity"));
                list.add(s);
            }
        } catch(Exception e) { e.printStackTrace(); }
        return list;
    }

    public boolean usernameExists(String u) {
        try (Connection c = DBConnection.getConnection("auth_db"); PreparedStatement p = c.prepareStatement("SELECT 1 FROM users_auth WHERE username=?")) {
            p.setString(1, u);
            return p.executeQuery().next();
        } catch(Exception e) { return false; }
    }

    public boolean rollNoExists(String r) {
        try (Connection c = DBConnection.getConnection("erp_db"); PreparedStatement p = c.prepareStatement("SELECT 1 FROM students WHERE roll_no=?")) {
            p.setString(1, r);
            return p.executeQuery().next();
        } catch(Exception e) { return false; }
    }
}