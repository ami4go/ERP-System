package edu.univ.erp.ui;

import edu.univ.erp.domain.CatalogSection;
import edu.univ.erp.service.StudentService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

/**
 * A JPanel that displays the course catalog in a JTable
 * and allows registration.
 * FINAL VERSION: Includes Search Bar and Row Sorter.
 */
public class CourseCatalogPanel extends JPanel {

    private StudentService studentService;

    // UI Components
    private JTable catalogTable;
    private DefaultTableModel tableModel;
    private JScrollPane scrollPane;
    private JButton registerButton;
    private JButton refreshButton;
    private JLabel statusLabel;

    // Search Components
    private JTextField searchField;
    private TableRowSorter<DefaultTableModel> rowSorter;

    public CourseCatalogPanel() {
        this.studentService = new StudentService();

        // Use BorderLayout for this panel
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- 0. Search Bar (Top) ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.add(new JLabel("Search Catalog:"));
        searchField = new JTextField(20);
        searchPanel.add(searchField);
        add(searchPanel, BorderLayout.NORTH);

        // --- 1. The Table ---
        String[] columnNames = {
                "Section ID", "Code", "Title", "Credits", "Instructor",
                "Day/Time", "Room", "Seats", "Deadline"
        };

        // Create a table model that makes cells non-editable
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // All cells are non-editable
            }
        };

        catalogTable = new JTable(tableModel);
        catalogTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Only one row at a time
        catalogTable.setFillsViewportHeight(true); // Fills the available space

        // NEW: Enable Sorting & Filtering
        rowSorter = new TableRowSorter<>(tableModel);
        catalogTable.setRowSorter(rowSorter);

        // Hide the 'Section ID' column (it's for internal use)
        catalogTable.getColumnModel().getColumn(0).setMinWidth(0);
        catalogTable.getColumnModel().getColumn(0).setMaxWidth(0);
        catalogTable.getColumnModel().getColumn(0).setWidth(0);

        scrollPane = new JScrollPane(catalogTable);
        add(scrollPane, BorderLayout.CENTER); // Add table to the center

        // --- 2. The Bottom Panel (Buttons & Status) ---
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        statusLabel = new JLabel(" "); // For messages
        bottomPanel.add(statusLabel, BorderLayout.CENTER);

        // Panel for buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        refreshButton = new JButton("Refresh");
        registerButton = new JButton("Register for Selected Section");
        buttonPanel.add(refreshButton);
        buttonPanel.add(registerButton);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH); // Add bottom panel to the south

        // --- 3. Add Action Listeners ---

        refreshButton.addActionListener(e -> loadCatalogData());
        registerButton.addActionListener(e -> handleRegistration());

        // Search Listener
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterTable(); }
            public void removeUpdate(DocumentEvent e) { filterTable(); }
            public void changedUpdate(DocumentEvent e) { filterTable(); }
        });

        // --- 4. Load Initial Data ---
        SwingUtilities.invokeLater(this::loadCatalogData);
    }

    // Helper for Search Logic
    private void filterTable() {
        String text = searchField.getText();
        if (text.trim().length() == 0) {
            rowSorter.setRowFilter(null);
        } else {
            // Case-insensitive regex search on any column
            rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        }
    }

    /**
     * Fetches data from the service and populates the JTable.
     */
    private void loadCatalogData() {
        // Clear old data
        tableModel.setRowCount(0);
        statusLabel.setText("Loading catalog...");

        // Run DB query in a background thread
        new SwingWorker<List<CatalogSection>, Void>() {

            @Override
            protected List<CatalogSection> doInBackground() throws Exception {
                // This runs on a worker thread
                return studentService.getCourseCatalog();
            }

            @Override
            protected void done() {
                // This runs back on the Swing thread
                try {
                    List<CatalogSection> sections = get();
                    // Populate the table model
                    for (CatalogSection s : sections) {
                        tableModel.addRow(new Object[]{
                                s.getSectionId(),
                                s.getCourseCode(),
                                s.getCourseTitle(),
                                s.getCredits(),
                                s.getInstructorName(),
                                s.getDayTime(),
                                s.getRoom(),
                                s.getSeatsInfo(),
                                s.getDeadline()
                        });
                    }
                    statusLabel.setText("Catalog loaded successfully.");
                    filterTable(); // Re-apply filter if user was searching
                } catch (Exception e) {
                    e.printStackTrace();
                    statusLabel.setText("Error loading catalog.");
                    JOptionPane.showMessageDialog(CourseCatalogPanel.this,
                            "Could not load course catalog: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    /**
     * Called when the "Register" button is clicked.
     */
    private void handleRegistration() {
        int viewRow = catalogTable.getSelectedRow();

        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a section from the table first.",
                    "No Section Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // CRITICAL FIX: Convert View Index -> Model Index (Handles Filtering/Sorting)
        int modelRow = catalogTable.convertRowIndexToModel(viewRow);

        // Get the hidden Section ID from column 0 using modelRow
        int sectionId = (Integer) tableModel.getValueAt(modelRow, 0);
        String courseCode = (String) tableModel.getValueAt(modelRow, 1);

        // Confirm with the user
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to register for " + courseCode + "?",
                "Confirm Registration",
                JOptionPane.YES_NO_OPTION);

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        // Disable buttons during operation
        registerButton.setEnabled(false);
        refreshButton.setEnabled(false);
        statusLabel.setText("Registering for " + courseCode + "...");

        // Run in a background thread so the UI doesn't freeze
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                // Call the "brain" (service)
                return studentService.registerStudentForSection(sectionId);
            }

            @Override
            protected void done() {
                try {
                    String result = get();

                    // Show the result in a popup
                    if (result.startsWith("Successfully")) {
                        JOptionPane.showMessageDialog(CourseCatalogPanel.this,
                                result, "Success", JOptionPane.INFORMATION_MESSAGE);
                        // Refresh the table to show the new seat count
                        loadCatalogData();
                    } else {
                        JOptionPane.showMessageDialog(CourseCatalogPanel.this,
                                result, "Registration Failed", JOptionPane.ERROR_MESSAGE);
                    }

                } catch (Exception e) {
                    JOptionPane.showMessageDialog(CourseCatalogPanel.this,
                            "An error occurred: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }

                // Re-enable buttons
                registerButton.setEnabled(true);
                refreshButton.setEnabled(true);
                statusLabel.setText(" ");
            }
        }.execute();
    }
}