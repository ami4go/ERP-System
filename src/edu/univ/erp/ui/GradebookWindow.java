package edu.univ.erp.ui;

import edu.univ.erp.domain.Assessment;
import edu.univ.erp.domain.GradingScale;
import edu.univ.erp.service.InstructorService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class GradebookWindow extends JFrame {

    private int sectionId;
    private String courseTitle;
    private InstructorService instructorService;

    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;

    // UI Components
    private JButton addAssessmentBtn, deleteAssessmentButton;
    private JButton scaleBtn, computeBtn, saveBtn;

    // NEW: Search
    private JTextField searchField;
    private TableRowSorter<DefaultTableModel> rowSorter;

    // Maps: Raw Column Index -> Assessment Object
    private Map<Integer, Assessment> assessmentMap;
    private Map<Integer, Integer> rowEnrollmentMap;

    public GradebookWindow(int sectionId, String courseTitle) {
        this.sectionId = sectionId;
        this.courseTitle = courseTitle;
        this.instructorService = new InstructorService();

        setTitle("Gradebook: " + courseTitle);
        setSize(1300, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // --- TOP TOOLBAR ---
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // 1. Add Search Bar
        toolbar.add(new JLabel("Search Student:"));
        searchField = new JTextField(15);
        toolbar.add(searchField);

        toolbar.add(Box.createHorizontalStrut(20)); // Spacer

        addAssessmentBtn = new JButton("Add Assessment");
        deleteAssessmentButton = new JButton("Delete Assessment");
        deleteAssessmentButton.setForeground(Color.RED);

        toolbar.add(addAssessmentBtn);
        toolbar.add(deleteAssessmentButton);
        add(toolbar, BorderLayout.NORTH);

        // --- CENTER TABLE ---
        tableModel = new DefaultTableModel();
        table = new JTable(tableModel);
        table.setRowHeight(25);
        table.setFillsViewportHeight(true);

        // 2. Enable Sorting & Filtering
        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // --- BOTTOM PANEL ---
        JPanel bottomPanel = new JPanel(new BorderLayout());
        statusLabel = new JLabel("Gradebook loaded.", SwingConstants.LEFT);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        scaleBtn = new JButton("Set Grading Scale");
        computeBtn = new JButton("Compute & View Finals");
        saveBtn = new JButton("Save All Grades");

        actionPanel.add(scaleBtn);
        actionPanel.add(computeBtn);
        actionPanel.add(saveBtn);

        bottomPanel.add(statusLabel, BorderLayout.WEST);
        bottomPanel.add(actionPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        // --- LISTENERS ---
        addAssessmentBtn.addActionListener(e -> handleAddAssessment());
        deleteAssessmentButton.addActionListener(e -> handleDeleteAssessment());
        scaleBtn.addActionListener(e -> new GradingScaleDialog(this, sectionId).setVisible(true));
        computeBtn.addActionListener(e -> computeFinalGrades());
        saveBtn.addActionListener(e -> saveAllGrades());

        // 3. Search Listener
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterTable(); }
            public void removeUpdate(DocumentEvent e) { filterTable(); }
            public void changedUpdate(DocumentEvent e) { filterTable(); }
        });

        loadGradebookData();
    }

    // Helper for Search Logic
    private void filterTable() {
        String text = searchField.getText();
        if (text.trim().length() == 0) {
            rowSorter.setRowFilter(null);
        } else {
            // Regex filter on Name/Roll No (case-insensitive)
            rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        }
    }

    private void loadGradebookData() {
        var result = instructorService.getGradebookTableModel(sectionId);
        this.tableModel = result.model;
        this.assessmentMap = result.assessmentMap;
        this.rowEnrollmentMap = result.rowMap;

        // Re-attach model to sorter (Important when model is replaced)
        table.setModel(tableModel);
        rowSorter.setModel(tableModel);

        if (this.assessmentMap != null) {
            table.setDefaultEditor(Object.class, new GradebookCellEditor(this.assessmentMap));
        }

        if (table.getColumnCount() > 0) {
            table.getColumnModel().getColumn(0).setMinWidth(0);
            table.getColumnModel().getColumn(0).setMaxWidth(0);
            table.getColumnModel().getColumn(0).setWidth(0);
        }

        // Re-apply search if text exists
        filterTable();
    }

    // --- 1. ADD ASSESSMENT (With Error Handling) ---
    private void handleAddAssessment() {
        JTextField nameField = new JTextField();
        JTextField weightField = new JTextField();
        JTextField totalField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(0, 2));
        panel.add(new JLabel("Name (e.g. Quiz 1):")); panel.add(nameField);
        panel.add(new JLabel("Weightage (%):")); panel.add(weightField);
        panel.add(new JLabel("Total Marks:")); panel.add(totalField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add Assessment", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText();
                double w = Double.parseDouble(weightField.getText());
                double t = Double.parseDouble(totalField.getText());

                if(instructorService.addAssessment(sectionId, name, w, t)) {
                    loadGradebookData();
                    statusLabel.setText("Added " + name);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add.");
                }
            } catch (RuntimeException re) {
                JOptionPane.showMessageDialog(this, re.getMessage(), "Action Blocked", JOptionPane.WARNING_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Invalid Numbers.");
            }
        }
    }

    // --- 2. DELETE ASSESSMENT (With Error Handling) ---
    private void handleDeleteAssessment() {
        if(assessmentMap == null || assessmentMap.isEmpty()) return;
        HashSet<String> names = new HashSet<>();
        for(Assessment a : assessmentMap.values()) names.add(a.getName());

        String[] opts = names.toArray(new String[0]);
        String sel = (String) JOptionPane.showInputDialog(this, "Delete:", "Delete", JOptionPane.WARNING_MESSAGE, null, opts, opts[0]);

        if(sel != null) {
            try {
                if(instructorService.deleteAssessment(sectionId, sel)) {
                    loadGradebookData();
                    statusLabel.setText("Deleted " + sel);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete.");
                }
            } catch (RuntimeException re) {
                JOptionPane.showMessageDialog(this, re.getMessage(), "Action Blocked", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    // --- 3. COMPUTE LOGIC (Live Math) ---
    private void computeFinalGrades() {
        List<GradingScale> scales = instructorService.getGradingScale(sectionId);
        if (scales.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please define a Grading Scale first.");
            return;
        }
        scales.sort(Comparator.comparingDouble(GradingScale::getMinPercentage).reversed());

        int totalColIdx = -1, gradeColIdx = -1;
        for (int i = 0; i < table.getColumnCount(); i++) {
            String name = table.getColumnName(i).toLowerCase();
            if (name.contains("total %")) totalColIdx = i;
            if (name.contains("final grade")) gradeColIdx = i;
        }

        if (totalColIdx == -1) return;

        // Iterate Rows (MODEL rows, not VIEW rows, to ensure calculation works even if filtered)
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            double finalPercentage = 0.0;
            for (Map.Entry<Integer, Assessment> entry : assessmentMap.entrySet()) {
                int rawColIdx = entry.getKey();
                Assessment a = entry.getValue();

                Object val = tableModel.getValueAt(row, rawColIdx);
                double score = 0.0;
                try {
                    score = (val != null && !val.toString().isEmpty()) ? Double.parseDouble(val.toString()) : 0.0;
                } catch (NumberFormatException e) { score = 0.0; }

                if (a.getTotalMarks() > 0) {
                    double weighted = (score / a.getTotalMarks()) * a.getWeightage();
                    finalPercentage += weighted;
                    tableModel.setValueAt(String.format("%.2f", weighted), row, rawColIdx + 1);
                }
            }

            tableModel.setValueAt(String.format("%.2f", finalPercentage), row, totalColIdx);

            String letter = "F";
            for (GradingScale s : scales) {
                if (finalPercentage >= (s.getMinPercentage() - 0.01)) {
                    letter = s.getGradeLetter();
                    break;
                }
            }
            tableModel.setValueAt(letter, row, gradeColIdx);
        }
        statusLabel.setText("Calculated. Click 'Save All Grades' to publish.");
    }

    // --- 4. SAVE GRADES (With Error Handling & Filter Support) ---
    private void saveAllGrades() {
        if (table.isEditing()) table.getCellEditor().stopCellEditing();

        // Auto-compute before saving
        computeFinalGrades();

        try {
            // We pass the MODEL directly, so sorting/filtering doesn't affect saving.
            // It saves ALL rows, hidden or visible.
            boolean ok = instructorService.saveGradesFromTable(sectionId, tableModel, assessmentMap, rowEnrollmentMap);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Grades Saved Successfully!");
                // Optional: Reload to sync DB state, but keep search filter active
                loadGradebookData();
            } else {
                JOptionPane.showMessageDialog(this, "Save Failed (Database Error).");
            }
        } catch (RuntimeException re) {
            JOptionPane.showMessageDialog(this, re.getMessage(), "Action Blocked", JOptionPane.WARNING_MESSAGE);
        }
    }
}