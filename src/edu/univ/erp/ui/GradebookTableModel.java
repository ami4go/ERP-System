package edu.univ.erp.ui;

import javax.swing.table.DefaultTableModel;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import edu.univ.erp.domain.GradebookStudent; // Make sure this import is here
import edu.univ.erp.domain.GradebookEntry; // Make sure this import is here

/**
 * Custom TableModel for the gradebook. (FINAL, REBUILT VERSION)
 * Handles dual-column layout and live-calculates weighted scores.
 */
public class GradebookTableModel extends DefaultTableModel {

    // These store the *raw* data
    private List<String> assessmentComponents = new ArrayList<>();
    private Map<String, BigDecimal> componentTotals = new HashMap<>();
    private Map<String, BigDecimal> componentWeights = new HashMap<>();

    // Column structure
    // 0 = Roll No, 1 = Student Name, ... (dynamic pairs) ..., Total, Final Grade
    private static final int STATIC_LEFT_COLS = 2; // Roll No, Student Name
    private static final int STATIC_RIGHT_COLS = 2; // Total, Final Grade

    // Stores <EnrollmentID, Data>
    private Map<Integer, Object[]> studentData = new HashMap<>();

    public GradebookTableModel() {}

    /**
     * Builds the table columns based on the loaded data.
     */
    public void buildColumns(List<String> components, Map<String, BigDecimal> totals, Map<String, BigDecimal> weights) {
        this.assessmentComponents = components;
        this.componentTotals = totals;
        this.componentWeights = weights;

        List<String> columnNames = new ArrayList<>();
        columnNames.add("Roll No");
        columnNames.add("Student Name");

        // --- THIS IS YOUR DUAL-COLUMN REQUIREMENT ---
        for (String comp : components) {
            String total = totals.get(comp).toPlainString();
            String weight = weights.get(comp).toPlainString();
            columnNames.add(comp + " (Out of " + total + ")"); // e.g., Quiz 1 (Out of 25)
            columnNames.add(comp + " (Weight " + weight + "%)"); // e.g., Quiz 1 (Weight 10%)
        }

        columnNames.add("Total (Out of 100)");
        columnNames.add("Final Grade");

        setColumnIdentifiers(columnNames.toArray());
    }

    /**
     * THIS IS THE METHOD THAT IS MISSING IN YOUR FILE.
     * Loads the raw data for all students into the model.
     */
    public void setStudentData(List<GradebookStudent> students, List<GradebookEntry> allGrades) {
        studentData.clear();
        setRowCount(0); // Clear the visual table

        // Map<EnrollmentID, Map<Component, Score>>
        Map<Integer, Map<String, BigDecimal>> scoreMap = new HashMap<>();
        Map<Integer, String> finalGradeMap = new HashMap<>();
        for (GradebookEntry grade : allGrades) {
            scoreMap.computeIfAbsent(grade.getEnrollmentId(), k -> new HashMap<>())
                    .put(grade.getComponent(), grade.getScore());
            if (grade.getFinalGrade() != null) {
                finalGradeMap.put(grade.getEnrollmentId(), grade.getFinalGrade());
            }
        }

        // Build the data rows
        for (GradebookStudent student : students) {
            int enrollId = student.getEnrollmentId();
            Object[] row = new Object[getColumnCount()];
            row[0] = student.getRollNo();
            row[1] = student.getStudentName();

            double totalWeightedScore = 0.0;

            // Loop through assessment components
            for (int i = 0; i < assessmentComponents.size(); i++) {
                String comp = assessmentComponents.get(i);
                int scoreCol = STATIC_LEFT_COLS + (i * 2);
                int weightCol = scoreCol + 1;

                BigDecimal score = null;
                if (scoreMap.containsKey(enrollId) && scoreMap.get(enrollId).containsKey(comp)) {
                    score = scoreMap.get(enrollId).get(comp);
                }

                row[scoreCol] = score; // Set the raw score, e.g., 20

                // Calculate weighted score
                if (score != null) {
                    double s = score.doubleValue();
                    double t = componentTotals.get(comp).doubleValue();
                    double w = componentWeights.get(comp).doubleValue();
                    if (t > 0) {
                        double weightedScore = (s / t) * w;
                        row[weightCol] = weightedScore; // Set calculated score, e.g., 8.0
                        totalWeightedScore += weightedScore;
                    }
                }
            }

            // Set the final static columns
            row[getColumnCount() - 2] = totalWeightedScore; // Total (Out of 100)
            row[getColumnCount() - 1] = finalGradeMap.getOrDefault(enrollId, "IP"); // Final Grade

            // Add to model and internal map
            addRow(row);
            studentData.put(enrollId, row);
        }
    }

    /**
     * THIS IS THE LIVE CALCULATION LOGIC
     */
    @Override
    public void setValueAt(Object aValue, int row, int col) {
        // 1. Set the raw score in the model
        super.setValueAt(aValue, row, col);

        String compName = getAssessmentNameAtCol(col);
        BigDecimal total = componentTotals.get(compName);
        BigDecimal weight = componentWeights.get(compName);

        BigDecimal score = null;
        if (aValue != null) {
            score = (BigDecimal) aValue;
        }

        // 2. Calculate and set the weighted score
        double weightedScore = 0.0;
        if (score != null && total.doubleValue() > 0) {
            weightedScore = (score.doubleValue() / total.doubleValue()) * weight.doubleValue();
        }
        // Set the next column (weighted score)
        super.setValueAt(weightedScore, row, col + 1);

        // 3. Recalculate and set the "Total (Out of 100)"
        double totalWeightedScore = 0.0;
        for (int i = 0; i < assessmentComponents.size(); i++) {
            int weightCol = STATIC_LEFT_COLS + (i * 2) + 1;
            Object val = getValueAt(row, weightCol);
            if (val != null) {
                totalWeightedScore += (Double) val;
            }
        }
        // Set the "Total" column
        super.setValueAt(totalWeightedScore, row, getColumnCount() - 2);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        // Is it a static column?
        if (column < STATIC_LEFT_COLS || column >= getColumnCount() - STATIC_RIGHT_COLS) {
            return false; // Not editable
        }
        // Is it a Score column or a Weight column?
        // 0=Score, 1=Weight
        return (column - STATIC_LEFT_COLS) % 2 == 0; // Only Score columns are editable
    }

    /**
     * THIS IS THE OTHER METHOD THAT IS MISSING IN YOUR FILE.
     * Gets the hidden enrollment ID from the specified row.
     */
    public int getEnrollmentIdAtRow(int row) {
        // We know the enroll ID is in the "Total" column's data, let's find it.
        // This is complex. Let's re-think.
        // Ah, the studentData map.

        // Find the Roll No, which is unique
        String rollNo = (String) getValueAt(row, 0);
        for(Map.Entry<Integer, Object[]> entry : studentData.entrySet()) {
            if (entry.getValue()[0].equals(rollNo)) {
                return entry.getKey();
            }
        }
        return 0; // Should not happen
    }

    // --- Helper methods for the Cell Editor ---
    public String getAssessmentNameAtCol(int col) {
        int assessmentIndex = (col - STATIC_LEFT_COLS) / 2;
        return assessmentComponents.get(assessmentIndex);
    }

    public BigDecimal getTotalMarksForColumn(int col) {
        return componentTotals.get(getAssessmentNameAtCol(col));
    }
}