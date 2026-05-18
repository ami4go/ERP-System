package edu.univ.erp;

import javax.swing.*;
import edu.univ.erp.ui.SplashScreen;
import edu.univ.erp.ui.LoginWindow;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // 1. Splash
            SplashScreen splash = new SplashScreen();
            splash.setVisible(true);

            // 2. Wait 3 seconds
            Timer timer = new Timer(3000, e -> {
                splash.setVisible(false);
                splash.dispose();

                new LoginWindow().setVisible(true);
            });
            timer.setRepeats(false);
            timer.start();
        });
    }
}   