package edu.univ.erp.ui;

import edu.univ.erp.domain.ClassStats;
import edu.univ.erp.domain.InstructorSection;
import edu.univ.erp.service.InstructorService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;

/**
 * A JPanel that displays class statistics for an instructor's sections.
 */
public class ClassStatsPanel extends JPanel {

    private InstructorService instructorService;

    // UI Components
    // FIXED: This must hold 'SectionItem' objects, not 'InstructorSection'
    private JComboBox<SectionItem> sectionsDropdown;
    private JButton loadStatsButton;
    private JTable statsTable;
    private DefaultTableModel tableModel;

    // Custom class to hold section data in the JComboBox
    private static class SectionItem {
        private InstructorSection section;
        public SectionItem(InstructorSection section) { this.section = section; }
        public int getSectionId() { return section.getSectionId(); }
        @Override
        public String toString() {
            return section.getCourseCode() + " - " + section.getCourseTitle() + " (" + section.getDayTime() + ")";
        }
    }

    public ClassStatsPanel() {
        this.instructorService = new InstructorService();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- 1. Top Panel (Section Selector) ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Select Section:"));

        sectionsDropdown = new JComboBox<>(); // It will now infer <SectionItem>
        sectionsDropdown.setPreferredSize(new Dimension(400, 25));

        loadStatsButton = new JButton("Load Statistics");

        topPanel.add(sectionsDropdown);
        topPanel.add(loadStatsButton);
        add(topPanel, BorderLayout.NORTH);

        // --- 2. Center Panel (Stats Table) ---
        String[] columnNames = {"Assessment", "Students Graded", "Average", "Highest", "Lowest", "Out of"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        statsTable = new JTable(tableModel);
        statsTable.setFillsViewportHeight(true);
        add(new JScrollPane(statsTable), BorderLayout.CENTER);

        // --- 3. Action Listeners ---
        loadStatsButton.addActionListener(e -> loadStatistics());

        // --- 4. Load Initial Data ---
        loadSectionsDropdown();
    }

    /**
     * Loads the instructor's sections into the dropdown menu.
     */
    private void loadSectionsDropdown() {
        new SwingWorker<List<InstructorSection>, Void>() {
            @Override
            protected List<InstructorSection> doInBackground() throws Exception {
                return instructorService.getMySections();
            }

            @Override
            protected void done() {
                try {
                    List<InstructorSection> sections = get();
                    sectionsDropdown.removeAllItems();
                    for (InstructorSection section : sections) {
                        // This line caused the error before, now it will work
                        sectionsDropdown.addItem(new SectionItem(section));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    /**
     * Called when the "Load Statistics" button is clicked.
     */
    private void loadStatistics() {
        SectionItem selectedItem = (SectionItem) sectionsDropdown.getSelectedItem();
        if (selectedItem == null) {
            JOptionPane.showMessageDialog(this, "Please select a section.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int sectionId = selectedItem.getSectionId();
        tableModel.setRowCount(0); // Clear table

        DecimalFormat df = new DecimalFormat("#.##"); // For formatting scores

        new SwingWorker<List<ClassStats>, Void>() {
            @Override
            protected List<ClassStats> doInBackground() throws Exception {
                return instructorService.getStatsForSection(sectionId);
            }

            @Override
            protected void done() {
                try {
                    List<ClassStats> statsList = get();
                    if (statsList.isEmpty()) {
                        tableModel.addRow(new Object[]{"No graded assessments found.", "", "", "", "", ""});
                    } else {
                        for (ClassStats stats : statsList) {
                            tableModel.addRow(new Object[]{
                                    stats.getComponentName(),
                                    stats.getStudentCount(),
                                    df.format(stats.getAverage()),
                                    df.format(stats.getMaxScore()),
                                    df.format(stats.getMinScore()),
                                    df.format(stats.getTotalMarks())
                            });
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(ClassStatsPanel.this, "Could not load statistics.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}