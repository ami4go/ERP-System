package edu.univ.erp.ui;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class SplashScreen extends JWindow {

    public SplashScreen() {
        // 1. Setup the window
        setSize(1000, 600); // A nice rectangular size
        setLocationRelativeTo(null); // Center on screen

        // 2. Create a custom panel to paint the background image
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                // FIX: Portable Path Loading logic
                ImageIcon icon = null;

                // Attempt 1: Try Classpath (Best for JARs/Builds)
                URL imgUrl = ClassLoader.getSystemResource("edu/univ/erp/ui/splash.jpeg");
                if (imgUrl != null) {
                    icon = new ImageIcon(imgUrl);
                } else {
                    // Attempt 2: Try local source path (Best for running inside IntelliJ/Eclipse)
                    String localPath = "src/edu/univ/erp/ui/splash.jpeg";
                    icon = new ImageIcon(localPath);

                    // Verify if load succeeded
                    if (icon.getImageLoadStatus() != MediaTracker.COMPLETE) {
                        System.err.println("⚠️ WARNING: Splash image not found!");
                        System.err.println("   Checked Classpath: edu/univ/erp/ui/splash.jpeg");
                        System.err.println("   Checked Local Path: " + localPath);
                        icon = null; // Force fallback
                    }
                }

                // Draw image scaled to fit the window
                if (icon != null && icon.getImage() != null) {
                    // Use high-quality scaling hints
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g.drawImage(icon.getImage(), 0, 0, getWidth(), getHeight(), this);
                } else {
                    // Fallback if image not found (Dark Grey)
                    g.setColor(new Color(45, 45, 45));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }

                // Optional: Add a dark overlay so text pops
                g.setColor(new Color(0, 0, 0, 100)); // Black with opacity
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        // 3. Use GridBagLayout to center the text
        backgroundPanel.setLayout(new GridBagLayout());
        this.setContentPane(backgroundPanel);

        // 4. Add the Text
        JLabel titleLabel = new JLabel("IIIT-Delhi ERP System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);

        // Add a subtle shadow to the text for readability
        backgroundPanel.add(titleLabel);
    }
}