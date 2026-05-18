package edu.univ.erp.service;

import edu.univ.erp.access.AccessControl;
import edu.univ.erp.auth.CurrentUserSession;
import edu.univ.erp.domain.*;
import edu.univ.erp.util.DBConnection;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.*;

/**
 * Service layer for instructor-related logic.
 * FINAL FIX: UI now becomes Read-Only during Maintenance Mode.
 */
public class InstructorService {
    private AccessControl accessControl;

    public InstructorService() {
        this.accessControl = new AccessControl();
    }

    // ============================================================
    // 1. DASHBOARD DATA
    // ============================================================
    public List<InstructorSection> getMySections() {
        String username = CurrentUserSession.getInstance().getUsername();
        List<InstructorSection> list = new ArrayList<>();
        if (username == null || username.isEmpty()) return list;

        String sql = "SELECT s.section_id, c.code, c.title, s.day_time, s.room, s.current_enrollment " +
                "FROM sections s " +
                "JOIN courses c ON s.course_code = c.code " +
                "WHERE s.instructor_username = ?";

        try (Connection conn = DBConnection.getConnection("erp_db");
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, username);
            ResultSet rs = p.executeQuery();
            while(rs.next()) {
                InstructorSection is = new InstructorSection();
                is.setSectionId(rs.getInt("section_id"));
                is.setCourseCode(rs.getString("code"));
                is.setCourseTitle(rs.getString("title"));
                is.setDayTime(rs.getString("day_time"));
                is.setRoom(rs.getString("room"));
                is.setEnrollmentCount(rs.getInt("current_enrollment"));
                list.add(is);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // ============================================================
    // 2. GRADEBOOK TABLE BUILDER
    // ============================================================
    public GradebookData getGradebookTableModel(int sectionId) {
        List<Assessment> assessments = getAssessments(sectionId);

        // FIX 1: Check Maintenance Mode ONCE when loading the table
        // If this returns a string (error), it means Maintenance is ON -> Read Only.
        boolean isMaintenanceMode = (accessControl.checkWriteAccess("Instructor") != null);

        // FIX 2: Pass this state into the Table Model
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                // A. Global Lock: If Maintenance Mode is ON, NOTHING is editable.
                if (isMaintenanceMode) return false;

                // B. Standard Column Locks
                // Columns 0 (ID), 1 (Roll), 2 (Name) are Read-Only
                if (column < 3) return false;

                // For dynamic columns: 3=Score(Edit), 4=Weight(Lock), 5=Score(Edit)...
                // We only allow editing the 'Raw Score' columns
                if (column < getColumnCount() - 2) {
                    return (column - 3) % 2 == 0;
                }

                // The last two columns (Total %, Final Grade) are also Read-Only
                return false;
            }
        };

        Map<Integer, Assessment> columnMap = new HashMap<>();
        Map<Integer, Integer> rowMap = new HashMap<>();

        // Static Columns
        model.addColumn("ID");
        model.addColumn("Roll No");
        model.addColumn("Name");

        // Dynamic Columns
        int colIdx = 3;
        for (Assessment a : assessments) {
            model.addColumn(a.getName() + " (Max: " + a.getTotalMarks() + ")");
            columnMap.put(colIdx, a);
            colIdx++;
            model.addColumn(a.getName() + " (Wt: " + a.getWeightage() + "%)");
            colIdx++;
        }
        model.addColumn("Total %");
        model.addColumn("Final Grade");

        // Fetch Data
        try (Connection conn = DBConnection.getConnection("erp_db")) {
            Map<Integer, Map<String, Double>> gradeCache = new HashMap<>();
            String gradeSql = "SELECT enrollment_id, component, score FROM grades " +
                    "WHERE enrollment_id IN (SELECT enrollment_id FROM enrollments WHERE section_id = ?)";

            try (PreparedStatement p = conn.prepareStatement(gradeSql)) {
                p.setInt(1, sectionId);
                ResultSet rs = p.executeQuery();
                while(rs.next()) {
                    gradeCache.computeIfAbsent(rs.getInt("enrollment_id"), k->new HashMap<>())
                            .put(rs.getString("component"), rs.getDouble("score"));
                }
            }

            String studSql = "SELECT e.enrollment_id, s.roll_no, s.full_name, e.final_score, e.course_grade " +
                    "FROM enrollments e " +
                    "JOIN students s ON e.student_username = s.username " +
                    "WHERE e.section_id = ? ORDER BY s.roll_no";

            try (PreparedStatement p = conn.prepareStatement(studSql)) {
                p.setInt(1, sectionId);
                ResultSet rs = p.executeQuery();
                int rowIndex = 0;
                while(rs.next()) {
                    int eid = rs.getInt("enrollment_id");
                    rowMap.put(rowIndex++, eid);

                    Vector<Object> row = new Vector<>();
                    row.add(eid);
                    row.add(rs.getString("roll_no"));
                    row.add(rs.getString("full_name"));

                    Map<String, Double> sGrades = gradeCache.getOrDefault(eid, new HashMap<>());
                    for (Assessment a : assessments) {
                        Double rawScore = sGrades.get(a.getName());
                        row.add(rawScore != null ? rawScore : 0.0);

                        double wScore = 0.0;
                        if (rawScore != null && a.getTotalMarks() > 0) {
                            wScore = (rawScore / a.getTotalMarks()) * a.getWeightage();
                        }
                        row.add(String.format("%.2f", wScore));
                    }

                    double dbFinalScore = rs.getDouble("final_score");
                    String dbFinalGrade = rs.getString("course_grade");
                    if (dbFinalGrade == null) dbFinalGrade = "IP";

                    row.add(dbFinalScore);
                    row.add(dbFinalGrade);
                    model.addRow(row);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new GradebookData(model, columnMap, rowMap);
    }

    // ============================================================
    // 3. SAVE LOGIC
    // ============================================================
    public boolean saveGradesFromTable(int sectionId, DefaultTableModel model,
                                       Map<Integer, Assessment> colMap,
                                       Map<Integer, Integer> rowMap) {
        String error = accessControl.checkWriteAccess("Instructor");
        if (error != null) throw new RuntimeException(error);

        String scoreSql = "INSERT INTO grades (enrollment_id, component, score, total_marks, weight) VALUES (?,?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE score=VALUES(score)";
        String finalSql = "UPDATE enrollments SET final_score = ?, course_grade = ? WHERE enrollment_id = ?";

        int totalColIdx = -1;
        int gradeColIdx = -1;
        for (int i = 0; i < model.getColumnCount(); i++) {
            String name = model.getColumnName(i).toLowerCase();
            if (name.contains("total %")) totalColIdx = i;
            if (name.contains("final grade")) gradeColIdx = i;
        }

        try (Connection conn = DBConnection.getConnection("erp_db")) {
            conn.setAutoCommit(false);
            try (PreparedStatement pScore = conn.prepareStatement(scoreSql);
                 PreparedStatement pFinal = conn.prepareStatement(finalSql)) {

                for (int row = 0; row < model.getRowCount(); row++) {
                    int eid = rowMap.get(row);

                    for (Map.Entry<Integer, Assessment> entry : colMap.entrySet()) {
                        int col = entry.getKey();
                        Assessment a = entry.getValue();
                        Object val = model.getValueAt(row, col);
                        double score = (val != null && !val.toString().isEmpty()) ? Double.parseDouble(val.toString()) : 0.0;

                        pScore.setInt(1, eid);
                        pScore.setString(2, a.getName());
                        pScore.setDouble(3, score);
                        pScore.setDouble(4, a.getTotalMarks());
                        pScore.setDouble(5, a.getWeightage());
                        pScore.addBatch();
                    }

                    if (totalColIdx != -1 && gradeColIdx != -1) {
                        Object totalObj = model.getValueAt(row, totalColIdx);
                        Object gradeObj = model.getValueAt(row, gradeColIdx);
                        double finalScore = 0.0;
                        try { finalScore = Double.parseDouble(totalObj.toString()); } catch (Exception e) {}
                        String finalGrade = (gradeObj != null) ? gradeObj.toString() : "IP";

                        pFinal.setDouble(1, finalScore);
                        pFinal.setString(2, finalGrade);
                        pFinal.setInt(3, eid);
                        pFinal.addBatch();
                    }
                }
                pScore.executeBatch();
                pFinal.executeBatch();
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // ============================================================
    // 4. ASSESSMENT & SCALE MANAGEMENT
    // ============================================================
    public boolean addAssessment(int sectionId, String name, double weight, double total) {
        String error = accessControl.checkWriteAccess("Instructor");
        if (error != null) throw new RuntimeException(error);

        if (total <= 0) return false; // Prevent 0 max marks (Division by zero protection)

        try (Connection conn = DBConnection.getConnection("erp_db");
             PreparedStatement p = conn.prepareStatement("INSERT INTO assessments (section_id, name, weightage, total_marks) VALUES (?,?,?,?)")) {
            p.setInt(1, sectionId);
            p.setString(2, name); p.setDouble(3, weight); p.setDouble(4, total);
            return p.executeUpdate() > 0;
        } catch (Exception e) { return false; }
    }

    public boolean deleteAssessment(int sectionId, String name) {
        String error = accessControl.checkWriteAccess("Instructor");
        if (error != null) throw new RuntimeException(error);

        try (Connection conn = DBConnection.getConnection("erp_db")) {
            conn.setAutoCommit(false);
            try (PreparedStatement p1 = conn.prepareStatement("DELETE FROM grades WHERE component = ? AND enrollment_id IN (SELECT enrollment_id FROM enrollments WHERE section_id = ?)")) {
                p1.setString(1, name);
                p1.setInt(2, sectionId);
                p1.executeUpdate();
            }
            try (PreparedStatement p2 = conn.prepareStatement("DELETE FROM assessments WHERE section_id=? AND name=?")) {
                p2.setInt(1, sectionId);
                p2.setString(2, name);
                p2.executeUpdate();
            }
            conn.commit();
            return true;
        } catch (Exception e) { return false; }
    }

    private List<Assessment> getAssessments(int sectionId) {
        List<Assessment> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection("erp_db");
             PreparedStatement p = conn.prepareStatement("SELECT * FROM assessments WHERE section_id=? ORDER BY assessment_id")) {
            p.setInt(1, sectionId);
            ResultSet rs = p.executeQuery();
            while(rs.next()) list.add(new Assessment(rs.getInt("assessment_id"), rs.getString("name"), rs.getDouble("weightage"), rs.getDouble("total_marks")));
        } catch (Exception e) {}
        return list;
    }

    public boolean saveGradingScale(int sectionId, List<GradingScale> scales) {
        String error = accessControl.checkWriteAccess("Instructor");
        if (error != null) throw new RuntimeException(error);

        try (Connection conn = DBConnection.getConnection("erp_db")) {
            conn.createStatement().executeUpdate("DELETE FROM grading_scale WHERE section_id=" + sectionId);
            try (PreparedStatement p = conn.prepareStatement("INSERT INTO grading_scale (section_id, grade_letter, min_percentage, grade_points) VALUES (?,?,?,?)")) {
                for(GradingScale s : scales) {
                    p.setInt(1, sectionId);
                    p.setString(2, s.getGradeLetter()); p.setDouble(3, s.getMinPercentage()); p.setDouble(4, s.getGradePoints());
                    p.addBatch();
                }
                p.executeBatch();
            }
            return true;
        } catch (Exception e) { return false; }
    }

    public List<GradingScale> getGradingScale(int sectionId) {
        List<GradingScale> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection("erp_db");
             PreparedStatement p = conn.prepareStatement("SELECT * FROM grading_scale WHERE section_id=? ORDER BY min_percentage DESC")) {
            p.setInt(1, sectionId);
            ResultSet rs = p.executeQuery();
            while(rs.next()) list.add(new GradingScale(rs.getString("grade_letter"), rs.getDouble("min_percentage"), rs.getDouble("grade_points")));
        } catch (Exception e) {}
        if (list.isEmpty()) {
            list.add(new GradingScale("A", 90, 10));
            list.add(new GradingScale("B", 80, 9));
            list.add(new GradingScale("F", 0, 0));
        }
        return list;
    }

    public List<ClassStats> getStatsForSection(int sectionId) {
        List<ClassStats> statsList = new ArrayList<>();
        List<Assessment> assessments = getAssessments(sectionId);
        try (Connection conn = DBConnection.getConnection("erp_db")) {
            for (Assessment a : assessments) {
                try (PreparedStatement p = conn.prepareStatement("SELECT score FROM grades WHERE component = ? AND enrollment_id IN (SELECT enrollment_id FROM enrollments WHERE section_id = ?)")) {
                    p.setString(1, a.getName());
                    p.setInt(2, sectionId);
                    ResultSet rs = p.executeQuery();
                    List<Double> scores = new ArrayList<>();
                    while (rs.next()) scores.add(rs.getDouble("score"));

                    if (!scores.isEmpty()) {
                        double sum = scores.stream().mapToDouble(d -> d).sum();
                        ClassStats s = new ClassStats();
                        s.setComponentName(a.getName()); s.setTotalMarks(a.getTotalMarks());
                        s.setStudentCount(scores.size()); s.setAverage(sum / scores.size());
                        s.setMaxScore(scores.stream().mapToDouble(d -> d).max().orElse(0));
                        s.setMinScore(scores.stream().mapToDouble(d -> d).min().orElse(0));
                        statsList.add(s);
                    }
                }
            }
        } catch (Exception e) {}
        return statsList;
    }
}