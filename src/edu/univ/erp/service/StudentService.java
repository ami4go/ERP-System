package edu.univ.erp.service;

import edu.univ.erp.access.AccessControl;
import edu.univ.erp.auth.AuthRepository;
import edu.univ.erp.auth.CurrentUserSession;
import edu.univ.erp.data.GradeRepository;
import edu.univ.erp.data.SectionRepository;
import edu.univ.erp.data.SystemSettingsRepository;
import edu.univ.erp.data.UserRepository;
import edu.univ.erp.domain.*;
import edu.univ.erp.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service layer for student-related logic.
 * REFACTORED: Uses Username for all student identifications.
 */
public class StudentService {
    private SectionRepository sectionRepository;
    private AuthRepository authRepository;
    private AccessControl accessControl;
    private GradeRepository gradeRepository;
    private UserRepository userRepository;
    private SystemSettingsRepository settingsRepository;

    public StudentService() {
        this.sectionRepository = new SectionRepository();
        this.authRepository = new AuthRepository();
        this.accessControl = new AccessControl();
        this.gradeRepository = new GradeRepository();
        this.userRepository = new UserRepository();
        this.settingsRepository = new SystemSettingsRepository();
    }

    // --- HELPER: Get Fresh Student Username ---
    // REFACTORED: No more ID lookup. The username IS the ID.
    private String getCurrentUsername() {
        return CurrentUserSession.getInstance().getUsername();
    }

    // ============================================================
    // 1. COURSE CATALOG
    // ============================================================
    public List<CatalogSection> getCourseCatalog() {
        String studentUsername = getCurrentUsername();
        if (studentUsername == null) return new ArrayList<>();

        // Fix: Repository now accepts username
        int studentCurrentSem = userRepository.getStudentCurrentSemester(studentUsername);

        String currentTerm = settingsRepository.getSetting("current_term");
        String currentYearStr = settingsRepository.getSetting("current_year");

        if (currentTerm == null || currentYearStr == null) return new ArrayList<>();
        int currentYear = Integer.parseInt(currentYearStr);

        List<CatalogSection> allSections = sectionRepository.getCatalogDetails("All");
        List<CatalogSection> filteredSections = new ArrayList<>();

        // 1. Collect instructor usernames to fetch names in batch
        Set<String> instructorUsernames = new HashSet<>();

        for (CatalogSection sec : allSections) {
            if (sec.getInstructorUsername() != null) {
                instructorUsernames.add(sec.getInstructorUsername());
            }
        }

        // 2. Fetch Instructor Names
        Map<String, String> nameMap = authRepository.getFullNamesByUsernames(instructorUsernames);

        // 3. Filter and Enrich
        for (CatalogSection sec : allSections) {
            if (!sec.getSemester().equalsIgnoreCase(currentTerm) || sec.getYear() != currentYear) {
                continue;
            }

            String allowedStr = sec.getAllowedSemesters();
            boolean semMatch = false;
            if (allowedStr != null) {
                String[] allowedList = allowedStr.split("[,\\s]+");
                for (String s : allowedList) {
                    if (s.equals(String.valueOf(studentCurrentSem))) {
                        semMatch = true;
                        break;
                    }
                }
            }
            if (!semMatch) continue;

            // Set resolved instructor name
            if (sec.getInstructorUsername() != null) {
                sec.setInstructorName(nameMap.getOrDefault(sec.getInstructorUsername(), "N/A"));
            } else {
                sec.setInstructorName("Unassigned");
            }

            filteredSections.add(sec);
        }
        return filteredSections;
    }

    // ============================================================
    // 2. REGISTRATION & TIMETABLE
    // ============================================================
    public String registerStudentForSection(int sectionId) {
        java.sql.Date deadline = sectionRepository.getSectionDeadline(sectionId);
        if (deadline != null && new java.util.Date().after(deadline)) {
            return "Error: The deadline (" + deadline + ") for this course has passed.";
        }

        String accessError = accessControl.checkWriteAccess("Student");
        if (accessError != null) return accessError;

        String studentUsername = getCurrentUsername();
        if (studentUsername == null) return "Error: Could not identify student.";

        if (sectionRepository.isEnrolled(studentUsername, sectionId)) return "You are already enrolled in this section.";
        if (sectionRepository.isEnrolledInCourse(studentUsername, sectionId)) return "You are already enrolled in another section of this course.";

        int[] capacity = sectionRepository.getSectionCapacity(sectionId);
        if (capacity == null) return "Error: This section does not exist.";
        if (capacity[0] >= capacity[1]) return "Section is full. Cannot register.";

        // Pass username to repository
        return sectionRepository.registerStudent(studentUsername, sectionId) ? "Successfully registered for the section!"
                : "An unexpected database error occurred.";
    }

    public List<TimetableEntry> getStudentTimetable() {
        String studentUsername = getCurrentUsername();
        if (studentUsername == null) return new ArrayList<>();
        return sectionRepository.getEnrolledSections(studentUsername);
    }

    public String dropStudentFromSection(int enrollmentId) {
        String accessError = accessControl.checkWriteAccess("Student");
        if (accessError != null) return accessError;

        int sectionId = sectionRepository.getSectionIdByEnrollmentId(enrollmentId);
        if (sectionId == 0) return "Error: Enrollment not found.";

        java.sql.Date deadline = sectionRepository.getSectionDeadline(sectionId);
        if (deadline != null && new java.util.Date().after(deadline)) {
            return "Error: The deadline (" + deadline + ") for this course has passed.";
        }

        return sectionRepository.dropStudent(enrollmentId) ? "Successfully dropped the section."
                : "An unexpected database error occurred.";
    }

    // ============================================================
    // 3. GRADES & ACADEMIC RECORD
    // ============================================================
    public List<GradeEntry> getStudentGradesForDisplay() {
        return new ArrayList<>();
    }

    public AcademicRecord getAcademicRecord() {
        String studentUsername = getCurrentUsername();
        if (studentUsername == null) return new AcademicRecord(new ArrayList<>(), new HashMap<>(), 0.0);

        List<CourseGradeSummary> allSummaries = new ArrayList<>();

        // --- PART A: FETCH ARCHIVED HISTORY ---
        List<CourseGradeSummary> historyList = getArchivedHistory(studentUsername);
        allSummaries.addAll(historyList);

        // --- PART B: FETCH LIVE DATA ---
        List<GradeEntry> liveGrades = gradeRepository.getGradesForStudent(studentUsername);
        Map<String, String[]> finalGradeMap = getFinalGradesFromEnrollments(studentUsername);

        // Group live grades by Course
        Map<String, List<GradeEntry>> liveByCourse = liveGrades.stream()
                .collect(Collectors.groupingBy(GradeEntry::getCourseCode, LinkedHashMap::new, Collectors.toList()));

        for (Map.Entry<String, List<GradeEntry>> entry : liveByCourse.entrySet()) {
            String courseCode = entry.getKey();
            List<GradeEntry> rawList = entry.getValue();
            if (rawList.isEmpty()) continue;

            GradeEntry first = rawList.get(0);

            // Deduplicate components
            Map<String, GradeEntry> uniqueComps = new LinkedHashMap<>();
            for (GradeEntry g : rawList) uniqueComps.put(g.getComponent(), g);

            // Build Text Summary
            StringBuilder sb = new StringBuilder();
            for (GradeEntry c : uniqueComps.values()) {
                if (sb.length() > 0) sb.append("\n");
                sb.append(c.getComponent()).append(": ");
                sb.append(c.getScore() != null ? c.getScore() : "0");
                sb.append("/").append(c.getTotalMarks() != null ? c.getTotalMarks() : "0");
            }

            // Get Final Data
            String[] finals = finalGradeMap.getOrDefault(courseCode, new String[]{"0.0", "IP"});
            double finalScore = Double.parseDouble(finals[0]);
            String finalGrade = finals[1];

            CourseGradeSummary summary = new CourseGradeSummary();
            summary.setCourseCode(courseCode);
            summary.setCourseTitle(first.getCourseTitle());
            summary.setSemester(first.getSemester());
            summary.setYear(first.getYear());
            summary.setCredits(first.getCredits());
            summary.setAssessmentsSummary(sb.toString());
            summary.setFinalScore(finalScore);
            summary.setFinalGrade(finalGrade);

            allSummaries.add(summary);
        }

        // --- PART C: CALCULATE GPA ---
        double totalPoints = 0;
        int totalCredits = 0;
        Map<String, Double> semesterPoints = new HashMap<>();
        Map<String, Integer> semesterCredits = new HashMap<>();

        for (CourseGradeSummary s : allSummaries) {
            if (!"IP".equalsIgnoreCase(s.getFinalGrade())) {
                if (s.getGradePoints() == 0.0) {
                    s.setGradePoints(convertGradeToPoints(s.getFinalGrade()));
                }
                String semKey = s.getSemester() + " " + s.getYear();
                double points = s.getGradePoints() * s.getCredits();

                semesterPoints.merge(semKey, points, Double::sum);
                semesterCredits.merge(semKey, s.getCredits(), Integer::sum);

                totalPoints += points;
                totalCredits += s.getCredits();
            }
        }

        Map<String, Double> sgpaMap = new HashMap<>();
        for (String sem : semesterCredits.keySet()) {
            if (semesterCredits.get(sem) > 0) {
                sgpaMap.put(sem, semesterPoints.get(sem) / semesterCredits.get(sem));
            }
        }

        double cgpa = (totalCredits > 0) ? totalPoints / totalCredits : 0.0;

        allSummaries.sort((a, b) -> {
            int y = Integer.compare(b.getYear(), a.getYear());
            if (y != 0) return y;
            return b.getSemester().compareTo(a.getSemester());
        });

        return new AcademicRecord(allSummaries, sgpaMap, cgpa);
    }

    private List<CourseGradeSummary> getArchivedHistory(String studentUsername) {
        List<CourseGradeSummary> list = new ArrayList<>();
        // REFACTORED SQL: Join on course_code and query by student_username
        String sql = "SELECT h.course_code, h.course_title, h.semester, h.year, h.final_score, h.letter_grade, c.credits " +
                "FROM academic_history h " +
                "LEFT JOIN courses c ON h.course_code = c.code " +
                "WHERE h.student_username = ?";

        try (Connection conn = DBConnection.getConnection("erp_db");
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, studentUsername);
            ResultSet rs = p.executeQuery();
            while(rs.next()) {
                CourseGradeSummary s = new CourseGradeSummary();
                s.setCourseCode(rs.getString("course_code"));
                s.setCourseTitle(rs.getString("course_title"));
                s.setSemester(rs.getString("semester"));
                s.setYear(rs.getInt("year"));
                s.setFinalScore(rs.getDouble("final_score"));
                s.setFinalGrade(rs.getString("letter_grade"));
                s.setAssessmentsSummary("Archived Record");

                int cr = rs.getInt("credits");
                s.setCredits(cr > 0 ? cr : 4);
                list.add(s);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    private Map<String, String[]> getFinalGradesFromEnrollments(String studentUsername) {
        Map<String, String[]> map = new HashMap<>();
        // REFACTORED SQL: Join on course_code and query by e.student_username
        String sql = "SELECT c.code, e.final_score, e.course_grade FROM enrollments e " +
                "JOIN sections s ON e.section_id = s.section_id " +
                "JOIN courses c ON s.course_code = c.code " +
                "WHERE e.student_username = ?";

        try (Connection conn = DBConnection.getConnection("erp_db");
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, studentUsername);
            ResultSet rs = p.executeQuery();
            while(rs.next()) {
                map.put(rs.getString("code"), new String[]{
                        String.valueOf(rs.getDouble("final_score")),
                        rs.getString("course_grade")
                });
            }
        } catch(Exception e) { e.printStackTrace(); }
        return map;
    }

    private double convertGradeToPoints(String grade) {
        if(grade == null) return 0.0;
        switch(grade.toUpperCase()) {
            case "A+": return 10.0;
            case "A": return 10.0;
            case "A-": return 9.0;
            case "B": return 8.0;
            case "B-": return 7.0;
            case "C": return 6.0;
            case "D": return 4.0;
            default: return 0.0;
        }
    }
}