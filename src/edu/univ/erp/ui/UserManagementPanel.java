package edu.univ.erp.ui;

import edu.univ.erp.domain.AdminUser;
import edu.univ.erp.service.AdminService;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

public class UserManagementPanel extends JPanel {
    private AdminService adminService;

    // --- STATE VARIABLES ---
    private boolean isEditMode = false;
    // REFACTORED: Track by Username (String), not ID (int)
    private String editingUsername = null;

    // Search Components
    private JTextField searchField;
    private TableRowSorter<DefaultTableModel> rowSorter;

    // --- UI COMPONENTS: LEFT FORM ---
    private JComboBox<String> roleCombo;
    private JTextField fullNameField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel passwordLabel;
    private JPanel specificFieldsPanel;
    private CardLayout cardLayout;

    // Student Specific Inputs
    private JTextField rollNoField;
    private JComboBox<String> programCombo;
    private JComboBox<String> branchCombo;
    private JTextField yearField;

    // Instructor Specific Inputs
    private JTextField departmentField;
    private JTextField titleField;

    // Form Buttons
    private JButton actionButton;
    private JButton cancelEditButton;

    // --- UI COMPONENTS: RIGHT LIST ---
    private JComboBox<String> listRoleFilter;
    private JComboBox<String> listProgramFilter;
    private JComboBox<String> listYearFilter;
    private JTable userTable;
    private DefaultTableModel tableModel;
    private JButton refreshButton;
    private JButton editButton;
    private JButton resetPasswordButton;
    private JButton deleteButton;
    private JLabel statusLabel;

    // --- DATA CONSTANTS ---
    private static final String[] PROGRAMS = {"B.Tech", "M.Tech", "PhD"};
    private static final String[] BTECH_BRANCHES = {"CSE", "CSAI", "CSAM", "CSD", "ECE"};
    private static final String[] MTECH_BRANCHES = {"CSE", "ECE", "CB"};
    private static final String[] PHD_BRANCHES = {"CB", "CSE", "Maths", "SSH"};
    private static final String[] FILTER_YEARS = {"All", "2021", "2022", "2023", "2024", "2025"};

    public UserManagementPanel() {
        this.adminService = new AdminService();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.35);

        JPanel leftPanel = createFormWrapper();
        splitPane.setLeftComponent(leftPanel);

        JPanel rightPanel = createListWrapper();
        splitPane.setRightComponent(rightPanel);

        add(splitPane, BorderLayout.CENTER);

        loadUserList();
    }

    private JPanel createFormWrapper() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(BorderFactory.createTitledBorder("User Details"));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Role
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1;
        roleCombo = new JComboBox<>(new String[]{"Student", "Instructor", "Admin"});
        formPanel.add(roleCombo, gbc);

        // Name
        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1;
        fullNameField = new JTextField(15);
        formPanel.add(fullNameField, gbc);

        // Username
        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        usernameField = new JTextField(15);
        formPanel.add(usernameField, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy++;
        passwordLabel = new JLabel("Password:");
        formPanel.add(passwordLabel, gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        formPanel.add(passwordField, gbc);

        // Specific Fields
        gbc.gridx = 0; gbc.gridy++;
        gbc.gridwidth = 2;
        cardLayout = new CardLayout();
        specificFieldsPanel = new JPanel(cardLayout);

        // Student Card
        JPanel studentCard = new JPanel(new GridLayout(4, 2, 5, 5));
        studentCard.setBorder(BorderFactory.createTitledBorder("Student Info"));
        rollNoField = new JTextField();
        programCombo = new JComboBox<>(PROGRAMS);
        branchCombo = new JComboBox<>(BTECH_BRANCHES);
        yearField = new JTextField();

        programCombo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                updateBranchOptions((String) e.getItem());
            }
        });

        studentCard.add(new JLabel("Roll No:")); studentCard.add(rollNoField);
        studentCard.add(new JLabel("Program:")); studentCard.add(programCombo);
        studentCard.add(new JLabel("Branch:")); studentCard.add(branchCombo);
        studentCard.add(new JLabel("Year:")); studentCard.add(yearField);

        // Instructor Card
        JPanel instructorCard = new JPanel(new GridLayout(2, 2, 5, 5));
        instructorCard.setBorder(BorderFactory.createTitledBorder("Faculty Info"));
        departmentField = new JTextField();
        titleField = new JTextField();
        instructorCard.add(new JLabel("Department:")); instructorCard.add(departmentField);
        instructorCard.add(new JLabel("Title/Designation:")); instructorCard.add(titleField);

        // Admin Card
        JPanel adminCard = new JPanel(new FlowLayout());
        adminCard.add(new JLabel("No additional details required for Admin."));

        specificFieldsPanel.add(studentCard, "Student");
        specificFieldsPanel.add(instructorCard, "Instructor");
        specificFieldsPanel.add(adminCard, "Admin");
        formPanel.add(specificFieldsPanel, gbc);

        // Buttons
        gbc.gridy++;
        JPanel btnContainer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actionButton = new JButton("Create User");
        actionButton.setBackground(new Color(60, 179, 113));
        actionButton.setForeground(Color.WHITE);
        cancelEditButton = new JButton("Cancel");
        cancelEditButton.setVisible(false);

        btnContainer.add(actionButton);
        btnContainer.add(cancelEditButton);
        formPanel.add(btnContainer, gbc);

        // Listeners
        roleCombo.addItemListener(e -> {
            if(e.getStateChange() == ItemEvent.SELECTED) {
                cardLayout.show(specificFieldsPanel, (String)e.getItem());
            }
        });
        actionButton.addActionListener(e -> handleSubmit());
        cancelEditButton.addActionListener(e -> clearForm());

        wrapper.add(formPanel, BorderLayout.NORTH);
        return wrapper;
    }

    private void filterTable() {
        String text = searchField.getText();
        if (text.trim().length() == 0) {
            rowSorter.setRowFilter(null);
        } else {
            rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        }
    }

    private JPanel createListWrapper() {
        JPanel wrapper = new JPanel(new BorderLayout(5, 5));
        wrapper.setBorder(BorderFactory.createTitledBorder("Existing Users"));

        JPanel topBar = new JPanel(new BorderLayout(5, 5));
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Role:"));
        listRoleFilter = new JComboBox<>(new String[]{"Student", "Instructor", "Admin"});
        filterPanel.add(listRoleFilter);
        filterPanel.add(new JLabel("  Program:"));
        String[] progOptions = new String[PROGRAMS.length + 1];
        progOptions[0] = "All";
        System.arraycopy(PROGRAMS, 0, progOptions, 1, PROGRAMS.length);
        listProgramFilter = new JComboBox<>(progOptions);
        filterPanel.add(listProgramFilter);
        filterPanel.add(new JLabel("  Year:"));
        listYearFilter = new JComboBox<>(FILTER_YEARS);
        filterPanel.add(listYearFilter);
        topBar.add(filterPanel, BorderLayout.WEST);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.add(new JLabel("Search:"));
        searchField = new JTextField(15);
        searchPanel.add(searchField);
        topBar.add(searchPanel, BorderLayout.EAST);
        wrapper.add(topBar, BorderLayout.NORTH);

        tableModel = new DefaultTableModel();
        userTable = new JTable(tableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.setRowHeight(25);
        rowSorter = new TableRowSorter<>(tableModel);
        userTable.setRowSorter(rowSorter);
        wrapper.add(new JScrollPane(userTable), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        statusLabel = new JLabel("Ready");
        statusLabel.setForeground(Color.GRAY);
        refreshButton = new JButton("Refresh");
        editButton = new JButton("Edit Selected");
        resetPasswordButton = new JButton("Reset Password");
        deleteButton = new JButton("Delete");
        deleteButton.setForeground(Color.RED);

        btnPanel.add(statusLabel);
        btnPanel.add(Box.createHorizontalStrut(10));
        btnPanel.add(refreshButton);
        btnPanel.add(editButton);
        btnPanel.add(resetPasswordButton);
        btnPanel.add(deleteButton);
        wrapper.add(btnPanel, BorderLayout.SOUTH);

        ItemListener filterListener = e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) loadUserList();
        };
        listRoleFilter.addItemListener(filterListener);
        listProgramFilter.addItemListener(filterListener);
        listYearFilter.addItemListener(filterListener);
        refreshButton.addActionListener(e -> loadUserList());
        deleteButton.addActionListener(e -> handleDeleteUser());
        resetPasswordButton.addActionListener(e -> handleResetPassword());
        editButton.addActionListener(e -> handleEditMode());

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        });

        return wrapper;
    }

    private void updateBranchOptions(String selectedProgram) {
        branchCombo.removeAllItems();
        String[] branches;
        if ("B.Tech".equals(selectedProgram)) branches = BTECH_BRANCHES;
        else if ("M.Tech".equals(selectedProgram)) branches = MTECH_BRANCHES;
        else if ("PhD".equals(selectedProgram)) branches = PHD_BRANCHES;
        else branches = new String[]{};
        for (String b : branches) {
            branchCombo.addItem(b);
        }
    }

    private void loadUserList() {
        String selectedRole = (String) listRoleFilter.getSelectedItem();
        String selectedProgram = (String) listProgramFilter.getSelectedItem();
        String selectedYear = (String) listYearFilter.getSelectedItem();

        boolean isStudent = "Student".equals(selectedRole);
        listProgramFilter.setEnabled(isStudent);
        listYearFilter.setEnabled(isStudent);

        // REFACTORED COLUMNS: Removed ID column. Username is the key.
        if (isStudent) {
            tableModel.setColumnIdentifiers(new String[]{"Full Name", "Username", "Roll No", "Program", "Year", "Sem"});
        } else if ("Instructor".equals(selectedRole)) {
            tableModel.setColumnIdentifiers(new String[]{"Full Name", "Username", "Department", "Title"});
        } else {
            tableModel.setColumnIdentifiers(new String[]{"Full Name", "Username"});
        }
        tableModel.setRowCount(0);

        statusLabel.setText("Loading...");
        new SwingWorker<List<AdminUser>, Void>() {
            @Override
            protected List<AdminUser> doInBackground() {
                return adminService.getUsersByRole(selectedRole);
            }
            @Override
            protected void done() {
                try {
                    List<AdminUser> users = get();
                    int count = 0;
                    for(AdminUser u : users) {
                        if (isStudent) {
                            if (!"All".equals(selectedProgram)) {
                                if (u.getProgram() == null || !u.getProgram().startsWith(selectedProgram)) continue;
                            }
                            if (!"All".equals(selectedYear)) {
                                if (u.getYear() != Integer.parseInt(selectedYear)) continue;
                            }
                        }

                        // REFACTORED ROWS: No userId added
                        if (isStudent) {
                            tableModel.addRow(new Object[]{
                                    u.getFullName(),
                                    u.getUsername(), // Key
                                    u.getRollNo(),
                                    u.getProgram(),
                                    u.getYear(),
                                    u.getCurrentSemester()
                            });
                        } else if ("Instructor".equals(selectedRole)) {
                            tableModel.addRow(new Object[]{u.getFullName(), u.getUsername(), u.getDepartment(), u.getTitle()});
                        } else {
                            tableModel.addRow(new Object[]{u.getFullName(), u.getUsername()});
                        }
                        count++;
                    }
                    statusLabel.setText("Showing " + count + " users.");
                } catch (Exception e) {
                    e.printStackTrace();
                    statusLabel.setText("Error loading data");
                }
            }
        }.execute();
    }

    private void handleEditMode() {
        int viewRow = userTable.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to edit.");
            return;
        }
        int modelRow = userTable.convertRowIndexToModel(viewRow);

        // REFACTORED: Extract Username from Column 1
        String name = (String) tableModel.getValueAt(modelRow, 0);
        String username = (String) tableModel.getValueAt(modelRow, 1);
        String role = (String) listRoleFilter.getSelectedItem();

        isEditMode = true;
        editingUsername = username; // Set state to Username

        roleCombo.setSelectedItem(role);
        roleCombo.setEnabled(false);
        fullNameField.setText(name);
        usernameField.setText(username);
        usernameField.setEnabled(false); // Cannot change username (PK)
        passwordField.setText("");
        passwordField.setVisible(false);
        passwordLabel.setVisible(false);

        if ("Student".equals(role)) {
            rollNoField.setText((String) tableModel.getValueAt(modelRow, 2));
            String fullProg = (String) tableModel.getValueAt(modelRow, 3);
            if (fullProg != null && fullProg.contains("(") && fullProg.contains(")")) {
                String p = fullProg.substring(0, fullProg.indexOf("(")).trim();
                String b = fullProg.substring(fullProg.indexOf("(") + 1, fullProg.indexOf(")")).trim();
                programCombo.setSelectedItem(p);
                branchCombo.setSelectedItem(b);
            }
            yearField.setText(tableModel.getValueAt(modelRow, 4).toString());
        } else if ("Instructor".equals(role)) {
            departmentField.setText((String) tableModel.getValueAt(modelRow, 2));
            titleField.setText((String) tableModel.getValueAt(modelRow, 3));
        }

        actionButton.setText("Update User");
        actionButton.setBackground(new Color(70, 130, 180));
        cancelEditButton.setVisible(true);
    }

    private void clearForm() {
        isEditMode = false;
        editingUsername = null;

        roleCombo.setEnabled(true);
        usernameField.setEnabled(true);
        usernameField.setText("");
        fullNameField.setText("");
        passwordField.setVisible(true);
        passwordLabel.setVisible(true);
        passwordField.setText("");
        rollNoField.setText("");
        yearField.setText("");
        departmentField.setText("");
        titleField.setText("");

        actionButton.setText("Create User");
        actionButton.setBackground(new Color(60, 179, 113));
        cancelEditButton.setVisible(false);
    }

    private void handleSubmit() {
        String role = (String) roleCombo.getSelectedItem();
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();

        if (fullName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Full Name is required.");
            return;
        }

        if (isEditMode) {
            // >>> UPDATE FLOW (Uses editingUsername) <<<
            new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() {
                    if ("Student".equals(role)) {
                        String combinedProgram = programCombo.getSelectedItem() + " (" + branchCombo.getSelectedItem() + ")";
                        int year = 0;
                        try { year = Integer.parseInt(yearField.getText()); } catch(Exception e) {}
                        // Fix: Update uses username
                        return adminService.updateStudent(editingUsername, fullName, rollNoField.getText(), combinedProgram, year);
                    } else if ("Instructor".equals(role)) {
                        return adminService.updateInstructor(editingUsername, fullName, departmentField.getText(), titleField.getText());
                    } else {
                        return adminService.updateAdmin(editingUsername, fullName);
                    }
                }
                @Override
                protected void done() {
                    try {
                        String result = get();
                        JOptionPane.showMessageDialog(UserManagementPanel.this, result);
                        if (result.toLowerCase().contains("success") || result.contains("updated")) {
                            clearForm();
                            loadUserList();
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                }
            }.execute();
        } else {
            // >>> CREATE FLOW (Uses Form Data) <<<
            String password = new String(passwordField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username and Password are required.");
                return;
            }

            // --- FIX: Enforce Password Length ---
            if (password.length() < 6) {
                JOptionPane.showMessageDialog(this, "Password must be at least 6 characters long.", "Security Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() {
                    if ("Student".equals(role)) {
                        String combinedProgram = programCombo.getSelectedItem() + " (" + branchCombo.getSelectedItem() + ")";
                        try {
                            int year = Integer.parseInt(yearField.getText());
                            return adminService.createStudent(fullName, username, password, rollNoField.getText(), combinedProgram, year);
                        } catch (Exception e) { return "Error: Invalid Year"; }
                    } else if ("Instructor".equals(role)) {
                        return adminService.createInstructor(fullName, username, password, departmentField.getText(), titleField.getText());
                    } else {
                        return adminService.createAdmin(fullName, username, password);
                    }
                }
                @Override
                protected void done() {
                    try {
                        String res = get();
                        JOptionPane.showMessageDialog(UserManagementPanel.this, res);
                        if (!res.startsWith("Error")) {
                            clearForm();
                            loadUserList();
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                }
            }.execute();
        }
    }

    private void handleDeleteUser() {
        int viewRow = userTable.getSelectedRow();
        if(viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a user to delete.");
            return;
        }
        int modelRow = userTable.convertRowIndexToModel(viewRow);

        // REFACTORED: Extract Username
        String name = (String) tableModel.getValueAt(modelRow, 0);
        String username = (String) tableModel.getValueAt(modelRow, 1); // Key

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete '" + name + "'?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if(confirm != JOptionPane.YES_OPTION) return;

        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() {
                // Fix: Call delete with username
                return adminService.deleteUser(username);
            }
            @Override protected void done() {
                try {
                    if(get()) {
                        JOptionPane.showMessageDialog(UserManagementPanel.this, "User deleted successfully.");
                        loadUserList();
                    } else {
                        JOptionPane.showMessageDialog(UserManagementPanel.this, "Failed to delete user.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch(Exception e) { e.printStackTrace(); }
            }
        }.execute();
    }

    private void handleResetPassword() {
        int viewRow = userTable.getSelectedRow();
        if(viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a user.");
            return;
        }
        int modelRow = userTable.convertRowIndexToModel(viewRow);

        // REFACTORED: Extract Username
        String username = (String) tableModel.getValueAt(modelRow, 1);

        String newPass = JOptionPane.showInputDialog(this, "Enter new password:");
        if (newPass == null || newPass.trim().isEmpty()) return;

        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() {
                // Fix: Call reset with username
                return adminService.resetPassword(username, newPass);
            }
            @Override protected void done() {
                try {
                    if(get()) JOptionPane.showMessageDialog(UserManagementPanel.this, "Password updated.");
                } catch(Exception e) { e.printStackTrace(); }
            }
        }.execute();
    }
}