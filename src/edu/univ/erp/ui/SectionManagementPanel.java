package edu.univ.erp.ui;

import edu.univ.erp.domain.AdminUser;
import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Section;
import edu.univ.erp.service.AdminService;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SectionManagementPanel extends JPanel {

    private AdminService adminService;

    // Dropdowns
    private JComboBox<Course> courseCombo;
    private JComboBox<AdminUser> instructorCombo;
    private JComboBox<String> semesterCombo;

    // Search
    private JTextField searchField;
    private TableRowSorter<DefaultTableModel> rowSorter;

    // Fields
    private JTextField roomField, timeField, capacityField, yearField, deadlineField;
    private JButton createButton, deleteButton;
    private JTable sectionTable;
    private DefaultTableModel tableModel;

    public SectionManagementPanel() {
        this.adminService = new AdminService();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 1. Left Form
        add(createFormPanel(), BorderLayout.WEST);

        // 2. Right List
        add(createListPanel(), BorderLayout.CENTER);

        // 3. Load Initial Data
        refreshData();
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Assign Section"));

        // FIX: Set Width to 400 as requested
        panel.setPreferredSize(new Dimension(400, 600));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; int row = 0;

        // Course Dropdown
        gbc.gridy = row++; panel.add(new JLabel("Course:"), gbc);
        courseCombo = new JComboBox<>();
        panel.add(courseCombo, setGbc(1, row-1));

        // Instructor Dropdown
        gbc.gridy = row++; panel.add(new JLabel("Instructor:"), gbc);
        instructorCombo = new JComboBox<>();
        // Custom renderer to show Name in dropdown instead of object ID
        instructorCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof AdminUser) {
                    setText(((AdminUser) value).getFullName());
                }
                return this;
            }
        });
        panel.add(instructorCombo, setGbc(1, row-1));

        // Semester & Year
        gbc.gridy = row++; panel.add(new JLabel("Semester:"), gbc);
        semesterCombo = new JComboBox<>(new String[]{"Monsoon", "Winter", "Summer"});
        panel.add(semesterCombo, setGbc(1, row-1));

        gbc.gridy = row++; panel.add(new JLabel("Year:"), gbc);
        yearField = new JTextField("2025");
        panel.add(yearField, setGbc(1, row-1));

        // Details
        gbc.gridy = row++; panel.add(new JLabel("Day/Time:"), gbc);
        timeField = new JTextField("Mon/Wed 10:00");
        panel.add(timeField, setGbc(1, row-1));

        gbc.gridy = row++; panel.add(new JLabel("Room:"), gbc);
        roomField = new JTextField("LHC-101");
        panel.add(roomField, setGbc(1, row-1));

        gbc.gridy = row++; panel.add(new JLabel("Capacity:"), gbc);
        capacityField = new JTextField("60");
        panel.add(capacityField, setGbc(1, row-1));

        gbc.gridy = row++; panel.add(new JLabel("Deadline (YYYY-MM-DD):"), gbc);
        deadlineField = new JTextField("2025-08-10");
        panel.add(deadlineField, setGbc(1, row-1));

        // Button
        gbc.gridy = row++; gbc.gridwidth = 2;
        createButton = new JButton("Assign Instructor");
        createButton.setBackground(new Color(100, 149, 237));
        createButton.setForeground(Color.WHITE);
        createButton.addActionListener(e -> handleCreate());
        panel.add(createButton, gbc);

        return panel;
    }

    private GridBagConstraints setGbc(int x, int y) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = x; c.gridy = y; c.fill = GridBagConstraints.HORIZONTAL; c.insets = new Insets(5,5,5,5);
        return c;
    }

    private JPanel createListPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Active Sections"));

        // --- 1. Search Bar (NEW) ---
        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        top.add(new JLabel("Search:"));
        searchField = new JTextField(15);
        top.add(searchField);
        p.add(top, BorderLayout.NORTH);

        // --- 2. Table Setup ---
        // ID Column is preserved for internal logic but hidden
        tableModel = new DefaultTableModel(new String[]{"ID", "Course", "Instructor", "Term", "Room", "Time"}, 0);
        sectionTable = new JTable(tableModel);

        // Enable Sorting & Filtering
        rowSorter = new TableRowSorter<>(tableModel);
        sectionTable.setRowSorter(rowSorter);

        // Hide ID column (Index 0)
        sectionTable.getColumnModel().getColumn(0).setMinWidth(0);
        sectionTable.getColumnModel().getColumn(0).setMaxWidth(0);

        p.add(new JScrollPane(sectionTable), BorderLayout.CENTER);

        // --- 3. Search Logic ---
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterTable(); }
            public void removeUpdate(DocumentEvent e) { filterTable(); }
            public void changedUpdate(DocumentEvent e) { filterTable(); }
        });

        // Bottom Buttons
        deleteButton = new JButton("Delete Selected");
        deleteButton.setForeground(Color.RED);
        deleteButton.addActionListener(e -> handleDelete());

        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnP.add(deleteButton);
        p.add(btnP, BorderLayout.SOUTH);

        return p;
    }

    // Helper for Search
    private void filterTable() {
        String text = searchField.getText();
        if (text.trim().length() == 0) {
            rowSorter.setRowFilter(null);
        } else {
            rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        }
    }

    public void refreshData() {
        // 1. Load Courses
        courseCombo.removeAllItems();
        List<Course> courses = adminService.getAllCourses();
        for(Course c : courses) courseCombo.addItem(c);

        // 2. Load Instructors
        instructorCombo.removeAllItems();
        List<AdminUser> insts = adminService.getInstructorsForDropdown();
        for(AdminUser u : insts) instructorCombo.addItem(u);

        // 3. Load Table
        tableModel.setRowCount(0);
        List<Section> sections = adminService.getAllSections();
        for(Section s : sections) {
            tableModel.addRow(new Object[]{
                    s.getSectionId(),
                    s.getCourseCode(),
                    s.getInstructorName(),
                    s.getSemester() + " " + s.getYear(),
                    s.getRoom(),
                    s.getDayTime()
            });
        }
        filterTable();
    }

    private void handleCreate() {
        Course c = (Course) courseCombo.getSelectedItem();
        AdminUser i = (AdminUser) instructorCombo.getSelectedItem();

        // Pass Objects to Service (Service extracts Code/Username)
        String res = adminService.createSection(c, i,
                timeField.getText(), roomField.getText(), capacityField.getText(),
                (String)semesterCombo.getSelectedItem(), yearField.getText(), deadlineField.getText());

        JOptionPane.showMessageDialog(this, res);
        if(res.contains("successfully")) refreshData();
    }

    private void handleDelete() {
        int viewRow = sectionTable.getSelectedRow();
        if(viewRow == -1) return;

        // FIX: Convert View Index -> Model Index
        int modelRow = sectionTable.convertRowIndexToModel(viewRow);

        // Extract internal Section ID (Integer)
        int id = (Integer) tableModel.getValueAt(modelRow, 0);

        String res = adminService.deleteSection(id);

        JOptionPane.showMessageDialog(this, res);
        refreshData();
    }
}