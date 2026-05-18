package edu.univ.erp.ui;

import edu.univ.erp.auth.CurrentUserSession;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import edu.univ.erp.ui.MySectionsPanel; // <-- ADD THIS
import edu.univ.erp.ui.ClassStatsPanel; // <-- ADD THIS
/**
 * The main dashboard for the Instructor user.
 * Uses a JTabbedPane to organize features.
 */
public class InstructorDashboard extends JFrame {

    // Main UI components
    private JTabbedPane tabbedPane;
    private JLabel welcomeLabel;
    private JButton logoutButton;

    // Panels for each tab (we will build these next)
    private MySectionsPanel mySectionsPanel; // <-- ADD THIS
    // private JPanel classStatsPanel; // <-- DELETE THIS
    private ClassStatsPanel classStatsPanel; // <-- ADD THIS

    public InstructorDashboard() {
        // --- Get Instructor Info ---
        String username = CurrentUserSession.getInstance().getUsername();

        // --- Basic Window Setup ---
        setTitle("Instructor Dashboard");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // --- MAIN CONTAINER (Holds Banner + Content) ---
        // This allows us to put the banner at the very top
        JPanel mainContainer = new JPanel(new BorderLayout());
        setContentPane(mainContainer);

        // 1. ADD MAINTENANCE BANNER (Top of everything)
        mainContainer.add(new MaintenanceBanner(), BorderLayout.NORTH);

        // --- CONTENT CONTAINER (Holds Header + Tabs) ---
        // This behaves like the old "Frame" layout
        JPanel contentContainer = new JPanel(new BorderLayout());
        mainContainer.add(contentContainer, BorderLayout.CENTER);

        // --- Top Panel (Welcome Message + Logout) ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        welcomeLabel = new JLabel("Welcome, " + username);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        topPanel.add(welcomeLabel, BorderLayout.WEST);

        // --- Top Right Panel (Change Pass + Logout) ---
        JPanel topRightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));

        JButton changePassButton = new JButton("Change Password");
        changePassButton.addActionListener(e -> new ChangePasswordDialog(this).setVisible(true));

        logoutButton = new JButton("Logout");

        topRightPanel.add(changePassButton);
        topRightPanel.add(logoutButton);

        topPanel.add(topRightPanel, BorderLayout.EAST);

        // Add Top Panel to Content Container
        contentContainer.add(topPanel, BorderLayout.NORTH);

        // --- Center Panel (Tabs) ---
        tabbedPane = new JTabbedPane();

        // 1. "My Sections" Tab
        mySectionsPanel = new MySectionsPanel();
        tabbedPane.addTab("My Sections (Gradebook)", mySectionsPanel);

        // 2. "Class Stats" Tab
        classStatsPanel = new ClassStatsPanel();
        tabbedPane.addTab("Class Statistics", classStatsPanel);

        // Add Tabs to Content Container
        contentContainer.add(tabbedPane, BorderLayout.CENTER);

        // --- Add Action Listeners ---
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogout();
            }
        });
    }

    private void handleLogout() {
        // 1. Clear the session
        CurrentUserSession.getInstance().destroySession();

        // 2. Close this dashboard
        this.dispose();

        // 3. Open the LoginWindow again
        SwingUtilities.invokeLater(() -> {
            LoginWindow loginWindow = new LoginWindow();
            loginWindow.setVisible(true);
        });
    }
}