package edu.univ.erp.service;

import edu.univ.erp.auth.PasswordUtil;
import edu.univ.erp.data.AdminRepository;
import edu.univ.erp.data.SystemSettingsRepository;
import edu.univ.erp.domain.AdminUser;
import edu.univ.erp.domain.Course;
import edu.univ.erp.util.CsvExporter;
import edu.univ.erp.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for Admin operations.
 * REFACTORED: Adapted to Natural Key Architecture (Usernames/Codes).
 */
public class AdminService {
    private AdminRepository adminRepository;
    private SystemSettingsRepository settingsRepository;

    public AdminService() {
        this.adminRepository = new AdminRepository();
        this.settingsRepository = new SystemSettingsRepository();
    }

    // ==========================================
    // 1. SETTINGS & MAINTENANCE
    // ==========================================
    public boolean isMaintenanceModeOn() {
        String val = settingsRepository.getSetting("maintenance_mode");
        return "true".equalsIgnoreCase(val);
    }

    public boolean toggleMaintenanceMode(boolean enable) {
        return settingsRepository.updateSetting("maintenance_mode", String.valueOf(enable));
    }

    public String getCurrentSetting(String key) {
        return settingsRepository.getSetting(key);
    }

    // ==========================================
    // 2. ACADEMIC SESSION LOGIC
    // ==========================================
    public String startNewAcademicSession(String newTerm, String academicSession, String startDate, String endDate) {
        int newYear;
        try {
            String[] parts = academicSession.split("-");
            newYear = Integer.parseInt(parts[0]);
        } catch (Exception e) {
            return "Error: Invalid Academic Session format.";
        }

        try (Connection conn = DBConnection.getConnection("erp_db")) {
            conn.setAutoCommit(false);

            // 1. ARCHIVE CURRENT SESSION
            System.out.println("Archiving current session data...");
            // REFACTORED SQL: Joins on text keys
            String archiveSql = "INSERT INTO academic_history (student_username, course_code, course_title, instructor_name, semester, year, final_score, letter_grade) " +
                    "SELECT e.student_username, c.code, c.title, i.full_name, s.semester, s.year, e.final_score, e.course_grade " +
                    "FROM enrollments e " +
                    "JOIN sections s ON e.section_id = s.section_id " +
                    "JOIN courses c ON s.course_code = c.code " +
                    "LEFT JOIN instructors i ON s.instructor_username = i.username " +
                    "WHERE e.course_grade != 'IP'";

            try (PreparedStatement pArchive = conn.prepareStatement(archiveSql)) {
                int rows = pArchive.executeUpdate();
                System.out.println("Archived " + rows + " records to History.");
            }

            // 2. WIPE ACTIVE DATA
            conn.createStatement().executeUpdate("DELETE FROM grades");
            conn.createStatement().executeUpdate("DELETE FROM enrollments");
            conn.createStatement().executeUpdate("UPDATE sections SET current_enrollment = 0");

            // 3. UPDATE SETTINGS
            settingsRepository.updateSetting("current_term", newTerm);
            settingsRepository.updateSetting("current_year", String.valueOf(newYear));
            settingsRepository.updateSetting("session_start_date", startDate);
            settingsRepository.updateSetting("session_end_date", endDate);

            // 4. PROMOTE STUDENTS
            String updateSql = "Monsoon".equalsIgnoreCase(newTerm)
                    ? "UPDATE students SET current_semester = ((? - year) * 2) + 1"
                    : "UPDATE students SET current_semester = (? - year) * 2";

            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setInt(1, newYear);
                pstmt.executeUpdate();
            }
            conn.createStatement().executeUpdate("UPDATE students SET current_semester = 1 WHERE current_semester < 1");

            // 5. HANDLE GRADUATES
            archiveAndRemoveGraduates(conn, "B.Tech", 8);
            archiveAndRemoveGraduates(conn, "M.Tech", 4);

            conn.commit();
            return "Session " + academicSession + " Started! History Archived. Students Promoted.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    public boolean unlockSession() {
        return settingsRepository.updateSetting("session_locked", "false");
    }

    private void archiveAndRemoveGraduates(Connection conn, String programFilter, int maxSemesters) throws Exception {
        // REFACTORED: Fetch by username
        String selectSql = "SELECT username, roll_no, full_name, year, cgpa " +
                "FROM students " +
                "WHERE program LIKE ? AND current_semester > ?";

        List<Map<String, String>> graduates = new ArrayList<>();
        List<String> usernamesToRemove = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(selectSql)) {
            pstmt.setString(1, programFilter + "%");
            pstmt.setInt(2, maxSemesters);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, String> data = new HashMap<>();
                data.put("username", rs.getString("username"));
                data.put("name", rs.getString("full_name"));
                data.put("roll", rs.getString("roll_no"));
                data.put("program", programFilter);
                data.put("year", String.valueOf(rs.getInt("year")));
                data.put("cgpa", String.valueOf(rs.getDouble("cgpa")));
                graduates.add(data);
                usernamesToRemove.add(rs.getString("username"));
            }
        }

        if (!graduates.isEmpty()) {
            String fileName = CsvExporter.exportGraduates(programFilter, graduates);
            System.out.println("Archived " + graduates.size() + " graduates to " + fileName);

            if (!usernamesToRemove.isEmpty()) {
                // Prepare IN clause ('user1', 'user2')
                String inClause = usernamesToRemove.stream()
                        .map(u -> "'" + u + "'")
                        .collect(Collectors.joining(","));

                // REFACTORED DELETE LOGIC
                // Delete Grades via Enrollments via Username
                conn.createStatement().executeUpdate("DELETE FROM grades WHERE enrollment_id IN (SELECT enrollment_id FROM enrollments WHERE student_username IN (" + inClause + "))");
                // Delete Enrollments
                conn.createStatement().executeUpdate("DELETE FROM enrollments WHERE student_username IN (" + inClause + ")");
                // Delete Students (Cascade will handle some, but explicit is safer)
                conn.createStatement().executeUpdate("DELETE FROM students WHERE username IN (" + inClause + ")");

                // Optional: Clean up Auth DB too (This requires a separate connection to auth_db)
                // For now, we leave the login active, or we can delete from auth_db if required.
            }
        }
    }

    // ==========================================
    // 3. USER MANAGEMENT
    // ==========================================
    public List<AdminUser> getUsersByRole(String role) {
        return adminRepository.getUsersByRole(role);
    }

    // REFACTORED: Accepts String username instead of int ID
    public boolean deleteUser(String username) {
        // Note: Database Cascades should handle cleaning up erp_db profiles
        return adminRepository.deleteUser(username);
    }

    public boolean resetPassword(String username, String newPassword) {
        String hash = PasswordUtil.hashPassword(newPassword);
        return adminRepository.resetPassword(username, hash);
    }

    public String createStudent(String fullName, String username, String password, String rollNo, String program, int year) {
        if (username.contains(" ")) return "Error: Username cannot contain spaces.";
        if (adminRepository.usernameExists(username)) return "Error: Username taken.";
        if (adminRepository.rollNoExists(rollNo)) return "Error: Roll Number exists.";

        String hash = PasswordUtil.hashPassword(password);

        // 1. Create Auth Record (Returns boolean now)
        boolean authSuccess = adminRepository.createUserAuth(fullName, username, "Student", hash);
        if (!authSuccess) return "Error: Auth creation failed.";

        // 2. Create Profile using Username
        return adminRepository.createStudentProfile(username, rollNo, program, year, fullName)
                ? "Student created successfully!"
                : "Error: Profile creation failed.";
    }

    public String createInstructor(String fullName, String username, String password, String department, String title) {
        if (username.contains(" ")) return "Error: Username cannot contain spaces.";
        if (adminRepository.usernameExists(username)) return "Error: Username taken.";

        String hash = PasswordUtil.hashPassword(password);
        boolean authSuccess = adminRepository.createUserAuth(fullName, username, "Instructor", hash);
        if (!authSuccess) return "Error: Auth creation failed.";

        return adminRepository.createInstructorProfile(username, department, title, fullName)
                ? "Instructor created successfully!"
                : "Error: Profile creation failed.";
    }

    public String createAdmin(String fullName, String username, String password) {
        if (username.contains(" ")) return "Error: Username cannot contain spaces.";
        if (adminRepository.usernameExists(username)) return "Error: Username taken.";

        String hash = PasswordUtil.hashPassword(password);
        boolean authSuccess = adminRepository.createUserAuth(fullName, username, "Admin", hash);

        if (authSuccess) {
            // Also create the admin profile entry
            adminRepository.createAdminProfile(username, fullName);
            return "Admin created successfully!";
        }
        return "Error creating admin.";
    }

    public String updateStudent(String username, String fullName, String rollNo, String program, int year) {
        if (fullName.isEmpty() || rollNo.isEmpty() || program.isEmpty()) return "Error: Fields cannot be empty.";
        return adminRepository.updateStudent(username, fullName, rollNo, program, year) ? "Student updated!" : "Update failed.";
    }

    public String updateInstructor(String username, String fullName, String department, String title) {
        if (fullName.isEmpty() || department.isEmpty()) return "Error: Fields cannot be empty.";
        return adminRepository.updateInstructor(username, fullName, department, title) ? "Instructor updated!" : "Update failed.";
    }

    public String updateAdmin(String username, String fullName) {
        if (fullName.isEmpty()) return "Error: Name cannot be empty.";
        return adminRepository.updateAdmin(username, fullName) ? "Admin updated!" : "Update failed.";
    }

    // ==========================================
    // 4. COURSE & SECTION OPERATIONS
    // ==========================================
    public java.util.List<edu.univ.erp.domain.Course> getAllCourses() {
        return adminRepository.getAllCourses();
    }

    public java.util.List<edu.univ.erp.domain.Section> getAllSections() {
        return adminRepository.getAllSections();
    }

    public java.util.List<AdminUser> getInstructorsForDropdown() {
        return adminRepository.getInstructorList();
    }

    public String createCourse(String code, String title, String creditsStr, String programType, String allowedSemesters) {
        if (code.isEmpty() || title.isEmpty() || creditsStr.isEmpty() || allowedSemesters.isEmpty()) {
            return "Error: All fields are required.";
        }
        if (adminRepository.courseCodeExists(code)) return "Error: Course code exists.";

        try {
            int credits = Integer.parseInt(creditsStr);
            if (credits < 1 || credits > 10) return "Error: Credits must be 1-10.";
            if (!allowedSemesters.matches("[0-9,]+")) return "Error: Semesters must be comma separated.";

            return adminRepository.createCourse(code, title, credits, programType, allowedSemesters)
                    ? "Course created successfully!" : "Error: DB Insert failed.";
        } catch (NumberFormatException e) {
            return "Error: Credits must be a number.";
        }
    }

    public boolean deleteCourse(String code) {
        return adminRepository.deleteCourse(code);
    }

    public String deleteSection(int sectionId) {
        return adminRepository.deleteSection(sectionId);
    }

    // --- REFACTORED METHOD ---
    public String createSection(Object courseItem, Object instructorItem, String dayTime, String room, String capStr, String sem, String yearStr, String deadlineStr) {
        try {
            java.sql.Date.valueOf(deadlineStr);
        } catch (IllegalArgumentException e) {
            return "Error: Deadline must be YYYY-MM-DD.";
        }

        // FIX: Extract Codes and Usernames (Strings)
        String courseCode = ((Course) courseItem).getCode();
        String instructorUsername = null;
        if (instructorItem != null) {
            instructorUsername = ((AdminUser) instructorItem).getUsername();
        }

        try {
            int capacity = Integer.parseInt(capStr);
            int year = Integer.parseInt(yearStr);
            if (capacity < 1) return "Error: Capacity must be positive.";

            return adminRepository.createSection(courseCode, instructorUsername, dayTime, room, capacity, sem, year, deadlineStr)
                    ? "Section created successfully!" : "Error: Creation failed.";
        } catch (NumberFormatException e) {
            return "Error: Capacity/Year must be numbers.";
        }
    }
}