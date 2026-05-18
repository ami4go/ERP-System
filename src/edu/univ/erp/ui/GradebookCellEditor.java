package edu.univ.erp.ui;

import edu.univ.erp.domain.Assessment;
import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.Map;

/**
 * A custom cell editor that validates grade input against the Assessment's total marks.
 * FIX: Restored strict Max Marks check. Scores cannot exceed Total Marks.
 */
public class GradebookCellEditor extends DefaultCellEditor {

    private Map<Integer, Assessment> assessmentMap;
    private BigDecimal totalMarks;
    private JTextField textField;

    // Constructor now accepts the Map to know the limits
    public GradebookCellEditor(Map<Integer, Assessment> assessmentMap) {
        super(new JTextField());
        this.assessmentMap = assessmentMap;
        this.textField = (JTextField) getComponent();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        // 1. Find the Assessment for this column
        Assessment a = assessmentMap.get(column);

        if (a != null) {
            this.totalMarks = BigDecimal.valueOf(a.getTotalMarks());
        } else {
            this.totalMarks = null; // Should not happen for editable columns
        }

        textField.setText((value != null) ? value.toString() : "");
        return textField;
    }

    @Override
    public boolean stopCellEditing() {
        String text = textField.getText();
        if (text == null || text.trim().isEmpty()) return super.stopCellEditing();

        try {
            BigDecimal score = new BigDecimal(text);

            // 1. Negative Check
            if (score.compareTo(BigDecimal.ZERO) < 0) {
                JOptionPane.showMessageDialog(null, "Score cannot be negative.", "Invalid Score", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // 2. Max Limit Check (RESTORED)
            // This ensures 69/60 is rejected.
            if (totalMarks != null && score.compareTo(totalMarks) > 0) {
                JOptionPane.showMessageDialog(null,
                        "Score (" + score + ") cannot exceed Total Marks (" + totalMarks + ").",
                        "Invalid Score", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            return super.stopCellEditing();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Please enter a valid number.", "Invalid Score", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}