package edu.univ.erp.ui;

import edu.univ.erp.service.AuthService;
import edu.univ.erp.auth.CurrentUserSession;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

// Dashboards
import edu.univ.erp.ui.StudentDashboard;
import edu.univ.erp.ui.InstructorDashboard;
import edu.univ.erp.ui.AdminDashboard;

public class LoginWindow extends JFrame {

    // UI Components
    private JTextField tfUsername;
    private JPasswordField tfPassword;
    private JButton loginButton;
    private JLabel statusLabel;

    // Images
    private Image backgroundImage;
    private ImageIcon logoIcon;

    // Service
    private AuthService authService;

    public LoginWindow() {
        this.authService = new AuthService();
        loadImages(); // 1. Load images first

        // 2. Frame Setup
        setTitle("University ERP Login");
        setSize(1100, 700); // Increased window start size slightly
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 3. Background Panel (Your existing high-quality logic)
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    g.setColor(Color.DARK_GRAY);
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        backgroundPanel.setLayout(new GridBagLayout());
        this.setContentPane(backgroundPanel);

        // 4. Add the updated Login Box
        JPanel loginBox = createLoginBox();
        backgroundPanel.add(loginBox);
    }

    private void loadImages() {
        try {
            // Background
            URL bgUrl = ClassLoader.getSystemResource("edu/univ/erp/ui/splash.jpeg");
            if (bgUrl != null) backgroundImage = new ImageIcon(bgUrl).getImage();

            // Logo - SCALED TO 300x300 NOW
            URL logoUrl = ClassLoader.getSystemResource("edu/univ/erp/ui/logo.png");
            if (logoUrl != null) {
                Image img = new ImageIcon(logoUrl).getImage();
                // Changed from 200 to 300
                Image scaled = img.getScaledInstance(300, 300, Image.SCALE_SMOOTH);
                logoIcon = new ImageIcon(scaled);
            }
        } catch (Exception e) {
            System.err.println("Error loading images");
        }
    }

    private JPanel createLoginBox() {
        JPanel box = new JPanel();
        box.setBackground(new Color(255, 255, 255, 245));

        // INCREASED BOX SIZE to fit the 300px logo
        box.setPreferredSize(new Dimension(850, 450));
        box.setLayout(null);

        // --- LEFT SIDE: BIGGER LOGO ---
        if (logoIcon != null) {
            JLabel logoLabel = new JLabel(logoIcon);
            // Positioned to fit the new height
            logoLabel.setBounds(30, 75, 300, 300);
            box.add(logoLabel);
        }

        // --- RIGHT SIDE: FORM (Shifted Right) ---
        int formX = 360; // Moved from 250 to 360 to clear the big logo
        int formWidth = 350;

        // Heading
        JLabel heading = new JLabel("Sign In");
        heading.setBounds(formX, 40, formWidth, 40);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 32));
        heading.setForeground(new Color(60, 60, 60));
        box.add(heading);

        // Username
        JLabel lblUser = new JLabel("Username");
        lblUser.setBounds(formX, 100, formWidth, 20);
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        box.add(lblUser);

        tfUsername = new JTextField();
        tfUsername.setBounds(formX, 125, formWidth, 35);
        tfUsername.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        box.add(tfUsername);

        // Password
        JLabel lblPass = new JLabel("Password");
        lblPass.setBounds(formX, 170, formWidth, 20);
        lblPass.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        box.add(lblPass);

        tfPassword = new JPasswordField();
        // Width is formWidth - 50 to make room for the eye button
        tfPassword.setBounds(formX, 195, formWidth - 50, 35);
        tfPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        box.add(tfPassword);

        // --- NEW FEATURE: EYE BUTTON ---
        JToggleButton showPassBtn = new JToggleButton("👁");
        showPassBtn.setBounds(formX + formWidth - 45, 195, 45, 35); // Placed right next to field
        showPassBtn.setFocusable(false);
        showPassBtn.setBackground(Color.LIGHT_GRAY);
        showPassBtn.setBorder(BorderFactory.createEmptyBorder());

        // Logic to toggle password visibility
        showPassBtn.addActionListener(e -> {
            if (showPassBtn.isSelected()) {
                tfPassword.setEchoChar((char) 0); // Show password
            } else {
                tfPassword.setEchoChar('•'); // Hide password (default dot)
            }
        });
        box.add(showPassBtn);
        // -------------------------------

        // Login Button
        loginButton = new JButton("Login");
        loginButton.setBounds(formX, 260, formWidth, 45);
        loginButton.setBackground(new Color(65, 105, 225));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        loginButton.setFocusPainted(false);
        box.add(loginButton);

        // Status Label
        statusLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel.setBounds(formX, 320, formWidth, 20);
        statusLabel.setForeground(Color.RED);
        box.add(statusLabel);

        // Enter Key Listeners
        ActionListener runLogin = e -> handleLogin();
        loginButton.addActionListener(runLogin);
        tfPassword.addActionListener(runLogin);

        return box;
    }

    private void handleLogin() {
        String username = tfUsername.getText();
        String password = new String(tfPassword.getPassword());

        loginButton.setEnabled(false);
        statusLabel.setForeground(Color.BLUE);
        statusLabel.setText("Logging in...");

        String result = authService.login(username, password);

        if ("SUCCESS".equals(result)) {
            statusLabel.setForeground(new Color(0, 150, 0));
            statusLabel.setText("Success!");
            this.dispose();
            openUserDashboard();
        } else {
            statusLabel.setForeground(Color.RED);
            statusLabel.setText(result);
            loginButton.setEnabled(true);
        }
    }

    private void openUserDashboard() {
        String role = CurrentUserSession.getInstance().getRole();
        if ("Student".equals(role)) new StudentDashboard().setVisible(true);
        else if ("Instructor".equals(role)) new InstructorDashboard().setVisible(true);
        else if ("Admin".equals(role)) new AdminDashboard().setVisible(true);
    }
}