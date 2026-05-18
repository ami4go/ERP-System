package edu.univ.erp.ui;

import edu.univ.erp.service.AdminService;
import edu.univ.erp.util.DbBackupService;
import edu.univ.erp.data.SystemSettingsRepository;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.time.Year;
import java.util.stream.IntStream;

public class SystemSettingsPanel extends JPanel {

    private AdminService adminService;
    private SystemSettingsRepository settingsRepo;

    // Session Controls
    private JComboBox<String> termCombo;
    private JComboBox<String> sessionCombo;
    private JComboBox<Integer> startDay, startYear, endDay, endYear;
    private JComboBox<String> startMonth, endMonth;

    private JButton startSessionButton; // This button changes behavior (Lock/Unlock)
    private JLabel sessionStatusLabel;  // Shows "Active & Locked" or "Unlocked"

    // Maintenance & Backup Controls
    private JToggleButton maintenanceToggle;
    private JLabel statusLabel;
    private JLabel infoLabel;
    private JButton backupButton;
    private JButton restoreButton;

    public SystemSettingsPanel() {
        this.adminService = new AdminService();
        this.settingsRepo = new SystemSettingsRepository();

        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel title = new JLabel("System Administration", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        add(title, gbc);

        // --- 1. ACADEMIC SESSION MANAGEMENT ---
        gbc.gridy++;
        JPanel sessionPanel = createSessionPanel();
        add(sessionPanel, gbc);

        // --- 2. MAINTENANCE MODE SECTION ---
        gbc.gridy++;
        JPanel maintPanel = new JPanel(new BorderLayout(10, 10));
        maintPanel.setBorder(BorderFactory.createTitledBorder("Maintenance Control"));

        maintenanceToggle = new JToggleButton("Maintenance Mode: OFF");
        maintenanceToggle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        maintenanceToggle.setPreferredSize(new Dimension(300, 50));
        maintPanel.add(maintenanceToggle, BorderLayout.CENTER);

        statusLabel = new JLabel("System is running normally.", SwingConstants.CENTER);
        statusLabel.setForeground(new Color(0, 150, 0));
        maintPanel.add(statusLabel, BorderLayout.SOUTH);

        add(maintPanel, gbc);

        // Explanation
        gbc.gridy++;
        infoLabel = new JLabel("<html><div style='text-align: center; width: 300px; color: gray;'>" +
                "Turn Maintenance ON to block all Student and Instructor changes." +
                "</div></html>");
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(infoLabel, gbc);

        // --- 3. BACKUP & RESTORE SECTION ---
        gbc.gridy++;
        JPanel dataPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        dataPanel.setBorder(BorderFactory.createTitledBorder("Data Management"));

        backupButton = new JButton("Backup Database");
        restoreButton = new JButton("Restore Database");

        dataPanel.add(backupButton);
        dataPanel.add(restoreButton);
        add(dataPanel, gbc);

        // --- LISTENERS ---
        maintenanceToggle.addActionListener(e -> toggleMaintenance());
        backupButton.addActionListener(e -> handleBackup());
        restoreButton.addActionListener(e -> handleRestore());

        // Load initial states
        loadCurrentState();      // Maintenance
        loadTermSettings();      // Term/Year dropdowns
        checkSessionLockStatus();// Lock status
    }

    private JPanel createSessionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Academic Session Master Control"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // A. Term & Session
        gbc.gridx=0; gbc.gridy=0; panel.add(new JLabel("Academic Term:"), gbc);
        termCombo = new JComboBox<>(new String[]{"Monsoon", "Winter"});
        gbc.gridx=1; panel.add(termCombo, gbc);

        gbc.gridx=2; panel.add(new JLabel("Year:"), gbc);
        int cur = Year.now().getValue();
        String[] sessions = {
                (cur-1)+"-"+(cur%100),
                cur+"-"+((cur+1)%100),
                (cur+1)+"-"+((cur+2)%100)
        };
        sessionCombo = new JComboBox<>(sessions);
        sessionCombo.setSelectedItem(cur+"-"+((cur+1)%100));
        gbc.gridx=3; panel.add(sessionCombo, gbc);

        // B. Dates
        gbc.gridx=0; gbc.gridy=1; panel.add(new JLabel("Start Date:"), gbc);
        gbc.gridx=1; gbc.gridwidth=3; panel.add(createDatePicker(true), gbc);

        gbc.gridx=0; gbc.gridy=2; gbc.gridwidth=1; panel.add(new JLabel("End Date:"), gbc);
        gbc.gridx=1; gbc.gridwidth=3; panel.add(createDatePicker(false), gbc);

        // C. Status Label (NEW)
        gbc.gridx=0; gbc.gridy=3; gbc.gridwidth=4;
        sessionStatusLabel = new JLabel("Checking status...", SwingConstants.CENTER);
        sessionStatusLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panel.add(sessionStatusLabel, gbc);

        // D. Action Button
        gbc.gridy=4;
        startSessionButton = new JButton("Initialize Session");
        startSessionButton.setBackground(new Color(220, 50, 50));
        startSessionButton.setForeground(Color.WHITE);
        startSessionButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        startSessionButton.addActionListener(e -> handleSessionAction()); // Points to logic below
        panel.add(startSessionButton, gbc);

        return panel;
    }

    private JPanel createDatePicker(boolean isStart) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        Integer[] days = IntStream.rangeClosed(1, 31).boxed().toArray(Integer[]::new);
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        Integer[] years = {Year.now().getValue(), Year.now().getValue()+1};

        JComboBox<Integer> d = new JComboBox<>(days);
        JComboBox<String> m = new JComboBox<>(months);
        JComboBox<Integer> y = new JComboBox<>(years);

        p.add(d); p.add(m); p.add(y);
        if(isStart) { startDay=d; startMonth=m; startYear=y; } else { endDay=d; endMonth=m; endYear=y; }
        return p;
    }

    // --- LOGIC: SESSION LOCK/UNLOCK ---

    private void checkSessionLockStatus() {
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() {
                String locked = settingsRepo.getSetting("session_locked");
                return "true".equalsIgnoreCase(locked);
            }
            @Override protected void done() {
                try { updateSessionUI(get()); } catch(Exception e) {}
            }
        }.execute();
    }

    private void updateSessionUI(boolean isLocked) {
        if (isLocked) {
            // LOCKED STATE: Disable inputs, Green Label
            setSessionInputsEnabled(false);
            sessionStatusLabel.setText("Session is ACTIVE & LOCKED. Changes Blocked.");
            sessionStatusLabel.setForeground(new Color(0, 128, 0)); // Green

            startSessionButton.setText("Unlock to Change Session");
            startSessionButton.setBackground(new Color(255, 200, 100)); // Orange
            startSessionButton.setForeground(Color.BLACK);
        } else {
            // UNLOCKED STATE: Enable inputs, Gray Label
            setSessionInputsEnabled(true);
            sessionStatusLabel.setText("System Unlocked. Ready to Initialize.");
            sessionStatusLabel.setForeground(Color.GRAY);

            startSessionButton.setText("Initialize New Semester & Promote Students");
            startSessionButton.setBackground(new Color(220, 50, 50)); // Red
            startSessionButton.setForeground(Color.WHITE);
        }
    }

    private void setSessionInputsEnabled(boolean enabled) {
        termCombo.setEnabled(enabled);
        sessionCombo.setEnabled(enabled);
        startDay.setEnabled(enabled); startMonth.setEnabled(enabled); startYear.setEnabled(enabled);
        endDay.setEnabled(enabled); endMonth.setEnabled(enabled); endYear.setEnabled(enabled);
    }

    private void handleSessionAction() {
        String btnText = startSessionButton.getText();

        if (btnText.contains("Unlock")) {
            // --- UNLOCK ACTION ---
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Unlocking allows you to modify the academic calendar.\n" +
                            "WARNING: Re-initializing will re-calculate student semesters again.\n\n" +
                            "Unlock System?", "Confirm Unlock", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                // Use SwingWorker to prevent UI freeze during DB write
                new SwingWorker<Boolean, Void>() {
                    @Override
                    protected Boolean doInBackground() {
                        return settingsRepo.updateSetting("session_locked", "false");
                    }
                    @Override
                    protected void done() {
                        checkSessionLockStatus(); // Refresh UI to "Unlocked" state
                    }
                }.execute();
            }

        } else {
            // --- INITIALIZE ACTION ---
            String term = (String) termCombo.getSelectedItem();
            String session = (String) sessionCombo.getSelectedItem();

            // FIX: Use String.format to ensure YYYY-MM-DD (e.g., 2025-08-01, not 2025-8-1)
            String sDate = String.format("%d-%02d-%02d",
                    startYear.getSelectedItem(),
                    startMonth.getSelectedIndex() + 1,
                    startDay.getSelectedItem());

            String eDate = String.format("%d-%02d-%02d",
                    endYear.getSelectedItem(),
                    endMonth.getSelectedIndex() + 1,
                    endDay.getSelectedItem());

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Initialize " + term + " " + session + "?\n\n" +
                            "- Students will be updated to their correct semester.\n" +
                            "- System will be LOCKED after this action.",
                    "Confirm Initialization", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                startSessionButton.setEnabled(false); // Prevent double clicks

                new SwingWorker<String, Void>() {
                    @Override
                    protected String doInBackground() {
                        // 1. Call Service to Start Session
                        String result = adminService.startNewAcademicSession(term, session, sDate, eDate);

                        // 2. FIX: Only lock if the service didn't return an error
                        if (result != null && !result.startsWith("Error")) {
                            settingsRepo.updateSetting("session_locked", "true");
                        }
                        return result;
                    }
                    @Override
                    protected void done() {
                        try {
                            String msg = get();
                            JOptionPane.showMessageDialog(SystemSettingsPanel.this, msg);
                            checkSessionLockStatus(); // Refresh UI state
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        startSessionButton.setEnabled(true);
                    }
                }.execute();
            }
        }
    }

    // --- EXISTING LOGIC (Maintenance, Load Settings, Backup) ---

    private void loadTermSettings() {
        new SwingWorker<Void, Void>() {
            String term, year, sDate, eDate;

            @Override
            protected Void doInBackground() {
                term = settingsRepo.getSetting("current_term");
                year = settingsRepo.getSetting("current_year");
                sDate = settingsRepo.getSetting("session_start_date"); // Format: YYYY-MM-DD
                eDate = settingsRepo.getSetting("session_end_date");
                return null;
            }

            @Override
            protected void done() {
                // 1. Set Term & Year
                if (term != null) termCombo.setSelectedItem(term);
                // For year combo ("2025-26"), we might need to match the string start
                if (year != null) {
                    for (int i = 0; i < sessionCombo.getItemCount(); i++) {
                        if (sessionCombo.getItemAt(i).startsWith(year)) {
                            sessionCombo.setSelectedIndex(i);
                            break;
                        }
                    }
                }

                // 2. Set Start Date (Parse YYYY-MM-DD)
                if (sDate != null && sDate.contains("-")) {
                    try {
                        String[] parts = sDate.split("-");
                        startYear.setSelectedItem(Integer.parseInt(parts[0]));
                        startMonth.setSelectedIndex(Integer.parseInt(parts[1]) - 1);
                        startDay.setSelectedItem(Integer.parseInt(parts[2]));
                    } catch (Exception e) { System.err.println("Error parsing Start Date: " + e.getMessage()); }
                }

                // 3. Set End Date
                if (eDate != null && eDate.contains("-")) {
                    try {
                        String[] parts = eDate.split("-");
                        endYear.setSelectedItem(Integer.parseInt(parts[0]));
                        endMonth.setSelectedIndex(Integer.parseInt(parts[1]) - 1);
                        endDay.setSelectedItem(Integer.parseInt(parts[2]));
                    } catch (Exception e) { System.err.println("Error parsing End Date: " + e.getMessage()); }
                }
            }
        }.execute();
    }

    private void loadCurrentState() {
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() { return adminService.isMaintenanceModeOn(); }
            @Override protected void done() { try { updateUIState(get()); } catch(Exception e) {} }
        }.execute();
    }

    private void toggleMaintenance() {
        boolean requestState = maintenanceToggle.isSelected();
        maintenanceToggle.setEnabled(false);
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() { return adminService.toggleMaintenanceMode(requestState); }
            @Override protected void done() {
                try { updateUIState(requestState); } catch (Exception e) {}
                maintenanceToggle.setEnabled(true);
            }
        }.execute();
    }

    private void updateUIState(boolean isOn) {
        maintenanceToggle.setSelected(isOn);
        if (isOn) {
            maintenanceToggle.setText("Maintenance Mode: ON");
            maintenanceToggle.setBackground(new Color(255, 200, 200));
            statusLabel.setText("SYSTEM LOCKED. Writes disabled.");
            statusLabel.setForeground(Color.RED);
        } else {
            maintenanceToggle.setText("Maintenance Mode: OFF");
            maintenanceToggle.setBackground(null);
            statusLabel.setText("System is running normally.");
            statusLabel.setForeground(new Color(0, 150, 0));
        }
    }

    private void handleBackup() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Backup");
        fileChooser.setSelectedFile(new File("university_backup.sql"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("SQL Files", "sql"));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String path = file.getAbsolutePath();
            if(!path.toLowerCase().endsWith(".sql")) path += ".sql";
            final String finalPath = path;
            backupButton.setEnabled(false);
            statusLabel.setText("Backing up data...");
            new SwingWorker<Boolean, Void>() {
                @Override protected Boolean doInBackground() { return DbBackupService.backup(finalPath); }
                @Override protected void done() {
                    try {
                        if (get()) { JOptionPane.showMessageDialog(SystemSettingsPanel.this, "Backup saved!"); statusLabel.setText("Backup complete."); }
                        else { JOptionPane.showMessageDialog(SystemSettingsPanel.this, "Backup Failed.", "Error", JOptionPane.ERROR_MESSAGE); }
                    } catch (Exception e) {}
                    backupButton.setEnabled(true);
                }
            }.execute();
        }
    }

    private void handleRestore() {
        // Same as before
        int confirm = JOptionPane.showConfirmDialog(this, "Restoring will OVERWRITE data. Proceed?", "Confirm", JOptionPane.YES_NO_OPTION);
        if(confirm != JOptionPane.YES_OPTION) return;
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Backup");
        fileChooser.setFileFilter(new FileNameExtensionFilter("SQL Files", "sql"));
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            restoreButton.setEnabled(false);
            statusLabel.setText("Restoring...");
            new SwingWorker<Boolean, Void>() {
                @Override protected Boolean doInBackground() { return DbBackupService.restore(file.getAbsolutePath()); }
                @Override protected void done() {
                    try {
                        if (get()) { JOptionPane.showMessageDialog(SystemSettingsPanel.this, "Restored!"); statusLabel.setText("Restore complete."); }
                        else { JOptionPane.showMessageDialog(SystemSettingsPanel.this, "Restore Failed.", "Error", JOptionPane.ERROR_MESSAGE); }
                    } catch (Exception e) {}
                    restoreButton.setEnabled(true);
                }
            }.execute();
        }
    }
}