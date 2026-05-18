package edu.univ.erp.ui;

import edu.univ.erp.service.AdminService;
import edu.univ.erp.domain.Course;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;

public class CourseManagementPanel extends JPanel {
    private AdminService adminService; // <--- The Backend Connection

    // UI Components
    private JTextField codeField, titleField;
    private JComboBox<Integer> creditsCombo;
    private JComboBox<String> typeCombo;

    // Search Components
    private JTextField searchField;
    private TableRowSorter<DefaultTableModel> rowSorter;

    // Semester Checkboxes
    private List<JCheckBox> semesterChecks;

    private JTable courseTable;
    private DefaultTableModel tableModel;
    private JButton createButton, deleteButton;

    public CourseManagementPanel() {
        this.adminService = new AdminService(); // Initialize Service

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // LEFT: Creation Form
        JPanel formPanel = createFormPanel();
        add(formPanel, BorderLayout.WEST);

        // CENTER: Course List
        JPanel listPanel = createListPanel();
        add(listPanel, BorderLayout.CENTER);

        // Load Data Initially
        refreshList();
    }

    // --- 1. PUBLIC REFRESH METHOD (Called by Dashboard) ---
    public void refreshList() {
        // Clear Table
        tableModel.setRowCount(0);

        // Fetch from DB
        List<Course> courses = adminService.getAllCourses();

        // Populate Table
        for (Course c : courses) {
            // REFACTORED: Removed courseId. Code is now the identifier.
            tableModel.addRow(new Object[]{
                    c.getCode(), // Column 0: Code (Primary Key)
                    c.getTitle(),
                    c.getCredits(),
                    c.getProgramType(),
                    c.getAllowedSemesters()
            });
        }
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Create New Course"));
        panel.setPreferredSize(new Dimension(350, 600));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; int row = 0;

        // Code
        gbc.gridy = row++; panel.add(new JLabel("Course Code:"), gbc);
        codeField = new JTextField();
        panel.add(codeField, setGbc(1, row-1));

        // Title
        gbc.gridy = row++; panel.add(new JLabel("Course Title:"), gbc);
        titleField = new JTextField(); panel.add(titleField, setGbc(1, row-1));

        // Credits
        gbc.gridy = row++;
        panel.add(new JLabel("Credits:"), gbc);
        creditsCombo = new JComboBox<>(new Integer[]{1, 2, 3, 4});
        panel.add(creditsCombo, setGbc(1, row-1));

        // Type
        gbc.gridy = row++; panel.add(new JLabel("Type:"), gbc);
        typeCombo = new JComboBox<>(new String[]{"Core", "Elective"});
        panel.add(typeCombo, setGbc(1, row-1));

        // Allowed Semesters
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        panel.add(new JLabel("Allowed Semesters:"), gbc);
        gbc.gridy = row++;
        JPanel checkPanel = new JPanel(new GridLayout(2, 4));
        semesterChecks = new ArrayList<>();
        for(int i=1; i<=8; i++) {
            JCheckBox cb = new JCheckBox("Sem " + i);
            semesterChecks.add(cb);
            checkPanel.add(cb);
        }
        panel.add(checkPanel, gbc);

        // Create Button
        gbc.gridy = row++; gbc.gridwidth = 2;
        createButton = new JButton("Save Course");
        createButton.setBackground(new Color(60, 179, 113));
        createButton.setForeground(Color.WHITE);
        panel.add(createButton, gbc);

        createButton.addActionListener(e -> handleCreateCourse());

        return panel;
    }

    private GridBagConstraints setGbc(int x, int y) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = x; c.gridy = y; c.fill = GridBagConstraints.HORIZONTAL; c.insets = new Insets(5, 5, 5, 5);
        return c;
    }

    private JPanel createListPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Course Catalog"));

        // --- 1. NEW: Search Bar ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.add(new JLabel("Search:"));
        searchField = new JTextField(20);
        topPanel.add(searchField);
        p.add(topPanel, BorderLayout.NORTH);

        // --- 2. Table Setup ---
        // REFACTORED COLUMNS: Removed "ID". Code is the first column.
        tableModel = new DefaultTableModel(new String[]{"Code", "Title", "Credits", "Type", "Semesters"}, 0);
        courseTable = new JTable(tableModel);

        // NEW: Enable Sorting & Filtering
        rowSorter = new TableRowSorter<>(tableModel);
        courseTable.setRowSorter(rowSorter);

        p.add(new JScrollPane(courseTable), BorderLayout.CENTER);

        // --- 3. NEW: Search Logic ---
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filter(); }
            public void removeUpdate(DocumentEvent e) { filter(); }
            public void changedUpdate(DocumentEvent e) { filter(); }
            private void filter() {
                String text = searchField.getText();
                if (text.trim().length() == 0) {
                    rowSorter.setRowFilter(null);
                } else {
                    // Case-insensitive search
                    rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });

        // Delete Button (Existing)
        deleteButton = new JButton("Delete Selected Course");
        deleteButton.setForeground(Color.RED);
        deleteButton.addActionListener(e -> handleDeleteCourse());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(deleteButton);
        p.add(btnPanel, BorderLayout.SOUTH);

        return p;
    }

    private void handleCreateCourse() {
        String code = codeField.getText().trim();
        String title = titleField.getText().trim();

        // Gather Semesters
        StringBuilder sems = new StringBuilder();
        for(int i=0; i<semesterChecks.size(); i++) {
            if(semesterChecks.get(i).isSelected()) {
                if(sems.length() > 0) sems.append(",");
                sems.append(i+1);
            }
        }

        if(code.isEmpty() || title.isEmpty() || sems.length() == 0) {
            JOptionPane.showMessageDialog(this, "Please fill Code, Title and select at least one Semester.");
            return;
        }

        // Call Backend
        String result = adminService.createCourse(
                code,
                title,
                creditsCombo.getSelectedItem().toString(),
                typeCombo.getSelectedItem().toString(),
                sems.toString()
        );

        JOptionPane.showMessageDialog(this, result);
        if(result.contains("successfully")) {
            // Clear fields
            codeField.setText("");
            titleField.setText("");
            for(JCheckBox cb : semesterChecks) cb.setSelected(false);
            // Reload List
            refreshList();
        }
    }

    private void handleDeleteCourse() {
        int viewRow = courseTable.getSelectedRow(); // Row in visual table
        if(viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a course to delete.");
            return;
        }

        // NEW: Convert View Index -> Model Index
        int modelRow = courseTable.convertRowIndexToModel(viewRow);

        // REFACTORED: Get Code (String) from Column 0 (was previously ID)
        String code = (String) tableModel.getValueAt(modelRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this, "Delete course " + code + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if(confirm == JOptionPane.YES_OPTION) {
            // REFACTORED: Call delete using Code string
            boolean success = adminService.deleteCourse(code);
            if(success) {
                JOptionPane.showMessageDialog(this, "Deleted.");
                refreshList();
            } else {
                JOptionPane.showMessageDialog(this, "Failed. (Maybe sections exist for this course?)");
            }
        }
    }
}