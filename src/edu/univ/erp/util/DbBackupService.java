package edu.univ.erp.util;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Utility to Backup and Restore MySQL databases using mysqldump.
 */
public class DbBackupService {

    // --- CONFIGURATION ---
    // Update these to match your DatabaseManager settings
    private static final String DB_USER = "root";
    private static final String DB_PASS = "India@123"; // <-- YOUR DB PASSWORD

    // List of databases to backup
    private static final List<String> DATABASES = List.of("auth_db", "erp_db");

    /**
     * Backs up the databases to the specified file path.
     */
    public static boolean backup(String filePath) {
        // Command: mysqldump -u root -ppassword --databases auth_db erp_db -r "C:/path/to/file.sql"

        ProcessBuilder pb = new ProcessBuilder(
                "mysqldump",
                "-u" + DB_USER,
                "-p" + DB_PASS,
                "--databases",
                "auth_db", "erp_db",
                "-r", filePath
        );

        try {
            Process process = pb.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Restores the databases from the specified SQL file.
     */
    public static boolean restore(String filePath) {
        // Command: mysql -u root -ppassword < "C:/path/to/file.sql"
        // Since Java ProcessBuilder doesn't support "<" directly, we pass the file as Input Stream.

        // We run this command just to open the mysql shell
        ProcessBuilder pb = new ProcessBuilder(
                "mysql",
                "-u" + DB_USER,
                "-p" + DB_PASS
        );

        // Redirect the file content into the command
        pb.redirectInput(new File(filePath));

        try {
            Process process = pb.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }
}