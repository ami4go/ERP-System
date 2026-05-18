package edu.univ.erp.ui;

import edu.univ.erp.domain.GradingScale;
import edu.univ.erp.service.InstructorService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GradingScaleDialog extends JDialog {

    private InstructorService instructorService;
    private int sectionId;
    private JTable rulesTable;
    private DefaultTableModel tableModel;

    // Constructor adjusted to what GradebookWindow passes (Owner, Service, ID)
    public GradingScaleDialog(Window owner, int sectionId) {
        super(owner, "Define Grading Scale", ModalityType.APPLICATION_MODAL);
        this.instructorService = new InstructorService();
        this.sectionId = sectionId;

        setLayout(new BorderLayout(10, 10));
        setSize(500, 400);
        setLocationRelativeTo(owner);

        // Instructions
        JLabel lbl = new JLabel("<html>Enter Grade, Min %, and Points.<br>Example: A | 90 | 10</html>");
        lbl.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        add(lbl, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(new String[]{"Grade", "Min %", "Points"}, 0);
        rulesTable = new JTable(tableModel);
        add(new JScrollPane(rulesTable), BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel();
        JButton addBtn = new JButton("Add Row");
        JButton resetBtn = new JButton("Reset Default"); // <-- NEW BUTTON
        JButton saveBtn = new JButton("Save Scale");

        addBtn.addActionListener(e -> tableModel.addRow(new Object[]{"", "", ""}));
        resetBtn.addActionListener(e -> resetToDefault()); // <-- ACTION
        saveBtn.addActionListener(e -> saveRules());

        btnPanel.add(addBtn);
        btnPanel.add(resetBtn);
        btnPanel.add(saveBtn);
        add(btnPanel, BorderLayout.SOUTH);

        loadCurrentScale();
    }

    private void loadCurrentScale() {
        List<GradingScale> list = instructorService.getGradingScale(sectionId);
        if (list.isEmpty()) {
            resetToDefault(); // Load default if empty
        } else {
            for (GradingScale s : list) {
                tableModel.addRow(new Object[]{
                        s.getGradeLetter(),
                        s.getMinPercentage(),
                        s.getGradePoints()
                });
            }
        }
    }

    // --- NEW: Reset Logic ---
    private void resetToDefault() {
        tableModel.setRowCount(0);
        tableModel.addRow(new Object[]{"A", 90.0, 10.0});
        tableModel.addRow(new Object[]{"B", 80.0, 8.0});
        tableModel.addRow(new Object[]{"C", 70.0, 6.0});
        tableModel.addRow(new Object[]{"D", 60.0, 4.0});
        tableModel.addRow(new Object[]{"F", 0.0, 0.0});
    }

    private void saveRules() {
        if (rulesTable.isEditing()) rulesTable.getCellEditor().stopCellEditing();

        List<GradingScale> scales = new ArrayList<>();
        try {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                Object letterObj = tableModel.getValueAt(i, 0);
                Object minObj = tableModel.getValueAt(i, 1);
                Object ptsObj = tableModel.getValueAt(i, 2);

                // --- VALIDATION CHECK ---
                if (letterObj == null || letterObj.toString().trim().isEmpty() ||
                        minObj == null || minObj.toString().trim().isEmpty() ||
                        ptsObj == null || ptsObj.toString().trim().isEmpty()) {

                    JOptionPane.showMessageDialog(this,
                            "Row " + (i + 1) + " contains empty fields. Please fill or remove it.",
                            "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return; // Stop saving
                }

                String letter = letterObj.toString().trim();
                double min, pts;

                try {
                    min = Double.parseDouble(minObj.toString());
                    pts = Double.parseDouble(ptsObj.toString());
                } catch (NumberFormatException ne) {
                    JOptionPane.showMessageDialog(this,
                            "Row " + (i + 1) + ": Min % and Points must be valid numbers.",
                            "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return; // Stop saving
                }

                scales.add(new GradingScale(letter, min, pts));
            }

            // CALL SERVICE
            boolean success = instructorService.saveGradingScale(sectionId, scales);
            if (success) {
                JOptionPane.showMessageDialog(this, "Scale Saved!");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Database Error.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (RuntimeException re) {
            // FIX: Catch the specific Maintenance Mode exception
            JOptionPane.showMessageDialog(this, re.getMessage(), "Action Blocked", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "An unexpected error occurred.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}