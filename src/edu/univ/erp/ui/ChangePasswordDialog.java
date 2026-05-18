package edu.univ.erp.ui;

import edu.univ.erp.service.AuthService;

import javax.swing.*;
import java.awt.*;

public class ChangePasswordDialog extends JDialog {

    private AuthService authService;
    private JPasswordField currentPassField;
    private JPasswordField newPassField;
    private JPasswordField confirmPassField;
    private JButton changeButton;
    private JButton cancelButton;

    public ChangePasswordDialog(Frame owner) {
        super(owner, "Change Password", true);
        this.authService = new AuthService();

        setSize(400, 250);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Current Password:"), gbc);
        gbc.gridx = 1;
        currentPassField = new JPasswordField(15);
        formPanel.add(currentPassField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("New Password:"), gbc);
        gbc.gridx = 1;
        newPassField = new JPasswordField(15);
        formPanel.add(newPassField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("Confirm New Password:"), gbc);
        gbc.gridx = 1;
        confirmPassField = new JPasswordField(15);
        formPanel.add(confirmPassField, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Button Panel
        JPanel btnPanel = new JPanel();
        changeButton = new JButton("Change Password");
        cancelButton = new JButton("Cancel");
        btnPanel.add(changeButton);
        btnPanel.add(cancelButton);
        add(btnPanel, BorderLayout.SOUTH);

        // Listeners
        changeButton.addActionListener(e -> handleChange());
        cancelButton.addActionListener(e -> dispose());
    }

    private void handleChange() {
        String current = new String(currentPassField.getPassword());
        String newVal = new String(newPassField.getPassword());
        String confirm = new String(confirmPassField.getPassword());

        if (current.isEmpty() || newVal.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // --- FIX: Enforce Password Length ---
        if (newVal.length() < 6) {
            JOptionPane.showMessageDialog(this, "New password must be at least 6 characters long.", "Weak Password", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!newVal.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "New passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        changeButton.setEnabled(false);

        // Call service
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                return authService.changePassword(current, newVal);
            }
            @Override
            protected void done() {
                try {
                    String result = get();
                    if (result.startsWith("Error")) {
                        JOptionPane.showMessageDialog(ChangePasswordDialog.this, result, "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(ChangePasswordDialog.this, result, "Success", JOptionPane.INFORMATION_MESSAGE);
                        dispose(); // Close dialog
                    }
                } catch (Exception e) { e.printStackTrace(); }
                changeButton.setEnabled(true);
            }
        }.execute();
    }
}