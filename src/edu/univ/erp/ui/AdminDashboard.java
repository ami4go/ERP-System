package edu.univ.erp.ui;

import edu.univ.erp.auth.CurrentUserSession;
import javax.swing.*;
import java.awt.*;

public class AdminDashboard extends JFrame {

    public AdminDashboard() {
        String username = CurrentUserSession.getInstance().getUsername();

        setTitle("Admin Dashboard - University ERP");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // --- Top Panel ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        topPanel.setBackground(new Color(245, 245, 250));

        JLabel welcomeLabel = new JLabel("Admin Console: " + username);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        welcomeLabel.setForeground(new Color(180, 40, 40));
        topPanel.add(welcomeLabel, BorderLayout.WEST);

        JButton logoutButton = new JButton("Logout");
        logoutButton.setFocusable(false);
        topPanel.add(logoutButton, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // --- Tabs ---
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Initialize Panels
        UserManagementPanel userPanel = new UserManagementPanel();
        CourseManagementPanel coursePanel = new CourseManagementPanel();
        SectionManagementPanel sectionPanel = new SectionManagementPanel(); // This one needs refreshing
        SystemSettingsPanel settingsPanel = new SystemSettingsPanel();

        tabbedPane.addTab("Manage Users", userPanel);
        tabbedPane.addTab("Manage Courses", coursePanel);
        tabbedPane.addTab("Manage Sections", sectionPanel);
        tabbedPane.addTab("System Settings", settingsPanel);

        add(tabbedPane, BorderLayout.CENTER);

        // --- EVENT LISTENER: REFRESH ON TAB CHANGE ---
        tabbedPane.addChangeListener(e -> {
            int index = tabbedPane.getSelectedIndex();
            String title = tabbedPane.getTitleAt(index);

            if ("Manage Sections".equals(title)) {
                // Reload dropdowns (Courses/Instructors) when this tab is clicked
                sectionPanel.refreshData();
            }
            else if ("Manage Courses".equals(title)) {
                coursePanel.refreshList(); // Optional: Refresh course list too
            }
        });

        // --- Logout Action ---
        logoutButton.addActionListener(e -> {
            CurrentUserSession.getInstance().destroySession();
            dispose();
            new LoginWindow().setVisible(true);
        });
    }
}