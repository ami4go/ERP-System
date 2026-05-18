package edu.univ.erp.ui;

import edu.univ.erp.domain.TimetableEntry;
import edu.univ.erp.service.StudentService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * A JPanel that displays the student's personal timetable
 * and allows dropping sections.
 */
public class MyTimetablePanel extends JPanel {

    private StudentService studentService;

    // UI Components
    private JTable timetableTable;
    private DefaultTableModel tableModel;
    private JScrollPane scrollPane;
    private JButton dropButton;
    private JButton refreshButton;
    private JLabel statusLabel;

    public MyTimetablePanel() {
        this.studentService = new StudentService();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- 1. The Table ---
        String[] columnNames = {
                "Enrollment ID", "Code", "Title", "Instructor", "Day/Time", "Room"
        };

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        timetableTable = new JTable(tableModel);
        timetableTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        timetableTable.setFillsViewportHeight(true);

        // Hide the 'Enrollment ID' column (it's for internal use)
        timetableTable.getColumnModel().getColumn(0).setMinWidth(0);
        timetableTable.getColumnModel().getColumn(0).setMaxWidth(0);
        timetableTable.getColumnModel().getColumn(0).setWidth(0);

        scrollPane = new JScrollPane(timetableTable);
        add(scrollPane, BorderLayout.CENTER);

        // --- 2. The Bottom Panel (Buttons & Status) ---
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        statusLabel = new JLabel(" ");
        bottomPanel.add(statusLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        refreshButton = new JButton("Refresh");
        dropButton = new JButton("Drop Selected Section");
        dropButton.setForeground(Color.RED); // Make it look like a "danger" action
        buttonPanel.add(refreshButton);
        buttonPanel.add(dropButton);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);

        // --- 3. Add Action Listeners ---
        refreshButton.addActionListener(e -> loadTimetableData());
        dropButton.addActionListener(e -> handleDrop());

        // --- 4. Load Initial Data ---
        SwingUtilities.invokeLater(this::loadTimetableData);
    }

    /**
     * Fetches data from the service and populates the JTable.
     */
    private void loadTimetableData() {
        tableModel.setRowCount(0);
        statusLabel.setText("Loading timetable...");

        new SwingWorker<List<TimetableEntry>, Void>() {
            @Override
            protected List<TimetableEntry> doInBackground() throws Exception {
                return studentService.getStudentTimetable();
            }

            @Override
            protected void done() {
                try {
                    List<TimetableEntry> timetable = get();
                    for (TimetableEntry entry : timetable) {
                        tableModel.addRow(new Object[]{
                                entry.getEnrollmentId(),
                                entry.getCourseCode(),
                                entry.getCourseTitle(),
                                entry.getInstructorName(),
                                entry.getDayTime(),
                                entry.getRoom()
                        });
                    }
                    statusLabel.setText("Timetable loaded successfully.");
                } catch (Exception e) {
                    e.printStackTrace();
                    statusLabel.setText("Error loading timetable.");
                }
            }
        }.execute();
    }

    /**
     * Called when the "Drop" button is clicked.
     */
    private void handleDrop() {
        int selectedRow = timetableTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a section to drop.",
                    "No Section Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get the hidden Enrollment ID from column 0
        int enrollmentId = (Integer) tableModel.getValueAt(selectedRow, 0);
        String courseCode = (String) tableModel.getValueAt(selectedRow, 1);

        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to drop " + courseCode + "?",
                "Confirm Drop",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        // --- Call the Drop Service ---
        statusLabel.setText("Dropping " + courseCode + "...");
        dropButton.setEnabled(false);
        refreshButton.setEnabled(false);

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                return studentService.dropStudentFromSection(enrollmentId);
            }

            @Override
            protected void done() {
                try {
                    String result = get();
                    if (result.startsWith("Successfully")) {
                        JOptionPane.showMessageDialog(MyTimetablePanel.this,
                                result, "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadTimetableData(); // Refresh the table
                    } else {
                        JOptionPane.showMessageDialog(MyTimetablePanel.this,
                                result, "Drop Failed", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MyTimetablePanel.this,
                            "An error occurred: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
                dropButton.setEnabled(true);
                refreshButton.setEnabled(true);
                statusLabel.setText(" ");
            }
        }.execute();
    }
}