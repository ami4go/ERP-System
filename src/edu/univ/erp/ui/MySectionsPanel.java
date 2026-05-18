package edu.univ.erp.ui;

import edu.univ.erp.domain.InstructorSection;
import edu.univ.erp.service.InstructorService;
import edu.univ.erp.ui.GradebookWindow; // <-- ADD THIS
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * A JPanel that displays the sections assigned to the instructor
 * and allows them to open the gradebook for one.
 */
public class MySectionsPanel extends JPanel {

    private InstructorService instructorService;

    // UI Components
    private JTable sectionsTable;
    private DefaultTableModel tableModel;
    private JScrollPane scrollPane;
    private JButton manageGradebookButton;
    private JButton refreshButton;

    public MySectionsPanel() {
        this.instructorService = new InstructorService();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- 1. The Table ---
        String[] columnNames = {
                "Section ID", "Course Code", "Course Title", "Day/Time", "Room", "Enrollment"
        };

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        sectionsTable = new JTable(tableModel);
        sectionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sectionsTable.setFillsViewportHeight(true);

        // Hide the 'Section ID' column
        sectionsTable.getColumnModel().getColumn(0).setMinWidth(0);
        sectionsTable.getColumnModel().getColumn(0).setMaxWidth(0);

        scrollPane = new JScrollPane(sectionsTable);
        add(scrollPane, BorderLayout.CENTER);

        // --- 2. The Bottom Panel (Buttons) ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        refreshButton = new JButton("Refresh");
        manageGradebookButton = new JButton("Manage Gradebook for Selected Section");

        bottomPanel.add(refreshButton);
        bottomPanel.add(manageGradebookButton);

        add(bottomPanel, BorderLayout.SOUTH);

        // --- 3. Add Action Listeners ---
        refreshButton.addActionListener(e -> loadSectionsData());
        manageGradebookButton.addActionListener(e -> handleManageGradebook());

        // --- 4. Load Initial Data ---
        SwingUtilities.invokeLater(this::loadSectionsData);
    }

    /**
     * Fetches data from the service and populates the JTable.
     */
    private void loadSectionsData() {
        tableModel.setRowCount(0);

        new SwingWorker<List<InstructorSection>, Void>() {
            @Override
            protected List<InstructorSection> doInBackground() throws Exception {
                return instructorService.getMySections();
            }

            @Override
            protected void done() {
                try {
                    List<InstructorSection> sections = get();
                    for (InstructorSection section : sections) {
                        tableModel.addRow(new Object[]{
                                section.getSectionId(),
                                section.getCourseCode(),
                                section.getCourseTitle(),
                                section.getDayTime(),
                                section.getRoom(),
                                section.getEnrollmentCount()
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(MySectionsPanel.this,
                            "Could not load sections.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    /**
     * Called when the "Manage Gradebook" button is clicked.
     */
    private void handleManageGradebook() {
        int selectedRow = sectionsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a section first.", "No Section Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get data from the selected row
        int sectionId = (Integer) tableModel.getValueAt(selectedRow, 0);
        String courseCode = (String) tableModel.getValueAt(selectedRow, 1);
        String courseTitle = (String) tableModel.getValueAt(selectedRow, 2);
        String fullTitle = courseCode + ": " + courseTitle;

        // --- TODO: Open the Gradebook Window ---
        // We will build this 'GradebookWindow' in the next step.
        // GradebookWindow gradebook = new GradebookWindow(sectionId, fullTitle);
        // gradebook.setVisible(true);

        // --- Open the Gradebook Window ---
        GradebookWindow gradebook = new GradebookWindow(sectionId, fullTitle);
        gradebook.setVisible(true); // This opens the pop-up

// After the pop-up closes, refresh the section list
// in case enrollment numbers changed (not really, but good practice).
        loadSectionsData();
    }
}