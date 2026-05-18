package edu.univ.erp.ui;

import edu.univ.erp.domain.AcademicRecord;
import edu.univ.erp.domain.CourseGradeSummary;
import edu.univ.erp.service.StudentService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MyGradesPanel extends JPanel {

    private StudentService studentService;

    // UI Components
    private JLabel cgpaLabel;
    private JPanel reportCardContainer;
    private JButton refreshButton;
    private JLabel statusLabel;

    public MyGradesPanel() {
        this.studentService = new StudentService();
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // Top Panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(50, 50, 60));
        topPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel title = new JLabel("Academic Report Card");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);

        cgpaLabel = new JLabel("CGPA: -");
        cgpaLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        cgpaLabel.setForeground(new Color(100, 200, 100));

        topPanel.add(title, BorderLayout.WEST);
        topPanel.add(cgpaLabel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Center Panel
        reportCardContainer = new JPanel();
        reportCardContainer.setLayout(new BoxLayout(reportCardContainer, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(reportCardContainer);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Panel
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        statusLabel = new JLabel(" ");
        refreshButton = new JButton("Refresh Grades");

        bottomPanel.add(statusLabel);
        bottomPanel.add(refreshButton);
        add(bottomPanel, BorderLayout.SOUTH);

        refreshButton.addActionListener(e -> loadGradeData());
        SwingUtilities.invokeLater(this::loadGradeData);
    }

    private void loadGradeData() {
        statusLabel.setText("Loading records...");
        refreshButton.setEnabled(false);

        new SwingWorker<AcademicRecord, Void>() {
            @Override
            protected AcademicRecord doInBackground() {
                return studentService.getAcademicRecord();
            }

            @Override
            protected void done() {
                try {
                    AcademicRecord record = get();
                    updateUI(record);
                    statusLabel.setText("Report updated.");
                } catch (Exception e) {
                    e.printStackTrace();
                    statusLabel.setText("Error loading records.");
                }
                refreshButton.setEnabled(true);
            }
        }.execute();
    }

    private void updateUI(AcademicRecord record) {
        DecimalFormat df = new DecimalFormat("0.00");
        cgpaLabel.setText("CGPA: " + df.format(record.getCgpa()));

        reportCardContainer.removeAll();

        List<CourseGradeSummary> allCourses = record.getCourseSummaries();
        if (allCourses.isEmpty()) {
            reportCardContainer.add(new JLabel("No academic records found."));
            reportCardContainer.revalidate();
            reportCardContainer.repaint();
            return;
        }

        Map<String, List<CourseGradeSummary>> coursesBySem = allCourses.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getSemester() + " " + c.getYear(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        for (Map.Entry<String, List<CourseGradeSummary>> entry : coursesBySem.entrySet()) {
            String semesterTitle = entry.getKey();
            List<CourseGradeSummary> semesterCourses = entry.getValue();
            Double sgpa = record.getSgpaMap().get(semesterTitle);
            String sgpaText = (sgpa != null) ? df.format(sgpa) : "N/A";

            reportCardContainer.add(createSemesterPanel(semesterTitle, semesterCourses, sgpaText));
            reportCardContainer.add(Box.createVerticalStrut(20));
        }

        reportCardContainer.revalidate();
        reportCardContainer.repaint();
    }

    private JPanel createSemesterPanel(String title, List<CourseGradeSummary> courses, String sgpa) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        panel.add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] colNames = {"Code", "Course Title", "Assessments", "Final Score", "Grade"};
        DefaultTableModel model = new DefaultTableModel(colNames, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        DecimalFormat scoreFmt = new DecimalFormat("#.##");
        for (CourseGradeSummary c : courses) {
            model.addRow(new Object[]{
                    c.getCourseCode(),
                    c.getCourseTitle(),
                    c.getAssessmentsSummary(), // Plain text with \n
                    scoreFmt.format(c.getFinalScore()),
                    c.getFinalGrade()
            });
        }

        JTable table = new JTable(model);

        // --- FIX: Use Custom Renderer for Multi-line text ---
        table.getColumnModel().getColumn(2).setCellRenderer(new MultiLineCellRenderer());

        // Adjust heights and widths
        table.setRowHeight(80); // Enough space for 3-4 lines
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        table.getColumnModel().getColumn(0).setPreferredWidth(70);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(400);

        int tableHeight = (table.getRowCount() * table.getRowHeight()) + 30;
        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(0, Math.min(tableHeight, 250)));

        panel.add(scroll, BorderLayout.CENTER);

        // Footer
        JLabel sgpaLabel = new JLabel("SGPA: " + sgpa);
        sgpaLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sgpaLabel.setForeground(new Color(0, 100, 200));
        sgpaLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(sgpaLabel, BorderLayout.SOUTH);

        return panel;
    }

    // --- CUSTOM RENDERER FOR MULTI-LINE TEXT ---
    static class MultiLineCellRenderer extends JTextArea implements TableCellRenderer {
        public MultiLineCellRenderer() {
            setLineWrap(true);
            setWrapStyleWord(true);
            setOpaque(true);
            setFont(new Font("Segoe UI", Font.PLAIN, 12));
            setMargin(new Insets(5, 5, 5, 5)); // Padding
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(table.getBackground());
            }
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }
}