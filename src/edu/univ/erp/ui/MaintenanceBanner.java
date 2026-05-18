package edu.univ.erp.ui;

import edu.univ.erp.service.AdminService;
import javax.swing.*;
import java.awt.*;

public class MaintenanceBanner extends JPanel {
    private AdminService adminService;
    private JLabel warningLabel;

    public MaintenanceBanner() {
        this.adminService = new AdminService();
        setLayout(new BorderLayout());
        setBackground(new Color(255, 69, 0)); // Red-Orange
        setVisible(false); // Hidden by default

        warningLabel = new JLabel("SYSTEM IS IN MAINTENANCE MODE. CHANGES ARE DISABLED.", SwingConstants.CENTER);
        warningLabel.setForeground(Color.WHITE);
        warningLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        warningLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        add(warningLabel, BorderLayout.CENTER);

        // Poll every 5 seconds
        Timer timer = new Timer(5000, e -> checkStatus());
        timer.start();
        checkStatus(); // Check immediately
    }

    private void checkStatus() {
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override protected Boolean doInBackground() {
                return adminService.isMaintenanceModeOn();
            }
            @Override protected void done() {
                try {
                    boolean isOn = get();
                    setVisible(isOn); // Show if ON, Hide if OFF
                    if (getParent() != null) getParent().revalidate(); // Refresh layout
                } catch (Exception e) {}
            }
        };
        worker.execute();
    }
}