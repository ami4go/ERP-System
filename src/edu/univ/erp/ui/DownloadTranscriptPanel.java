package edu.univ.erp.ui;

import edu.univ.erp.util.TranscriptService;

import javax.swing.*;
import java.awt.*;

/**
 * A JPanel with a button to download the student's transcript.
 */
public class DownloadTranscriptPanel extends JPanel {

    private TranscriptService transcriptService;

    // UI Components
    private JButton downloadButton;
    private JLabel statusLabel;

    public DownloadTranscriptPanel() {
        this.transcriptService = new TranscriptService();

        // Use a simple GridBagLayout to center the button
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title
        JLabel titleLabel = new JLabel("Official Transcript", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        contentPanel.add(titleLabel, BorderLayout.NORTH);

        // Button
        downloadButton = new JButton("Download Transcript (CSV)");
        downloadButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        downloadButton.setPreferredSize(new Dimension(250, 40));
        contentPanel.add(downloadButton, BorderLayout.CENTER);

        // Status
        statusLabel = new JLabel("Only completed courses will appear.", SwingConstants.CENTER);
        statusLabel.setForeground(Color.GRAY);
        contentPanel.add(statusLabel, BorderLayout.SOUTH);

        add(contentPanel, gbc); // Add the panel to the center

        // --- Add Action Listener ---
        downloadButton.addActionListener(e -> handleDownload());
    }

    private void handleDownload() {
        statusLabel.setText("Generating transcript...");
        statusLabel.setForeground(Color.BLACK);
        downloadButton.setEnabled(false);

        // Run in background thread
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                // Pass 'DownloadTranscriptPanel.this' as the parent
                return transcriptService.generateTranscriptCSV(DownloadTranscriptPanel.this);
            }

            @Override
            protected void done() {
                try {
                    String result = get();
                    statusLabel.setText(result);

                    // Show result in a popup
                    if (result.startsWith("Error") || result.startsWith("No completed")) {
                        statusLabel.setForeground(Color.RED);
                        JOptionPane.showMessageDialog(DownloadTranscriptPanel.this,
                                result, "Transcript Info", JOptionPane.WARNING_MESSAGE);
                    } else if (result.startsWith("File save")) {
                        statusLabel.setForeground(Color.GRAY);
                    } else {
                        statusLabel.setForeground(new Color(0, 150, 0)); // Green
                        JOptionPane.showMessageDialog(DownloadTranscriptPanel.this,
                                result, "Success", JOptionPane.INFORMATION_MESSAGE);
                    }

                } catch (Exception e) {
                    statusLabel.setText("An unexpected error occurred.");
                    JOptionPane.showMessageDialog(DownloadTranscriptPanel.this,
                            "An unexpected error occurred: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
                downloadButton.setEnabled(true);
            }
        }.execute();
    }
}