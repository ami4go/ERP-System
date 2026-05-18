package edu.univ.erp.auth;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class for hashing and verifying passwords using jBCrypt.
 */
public class PasswordUtil {

    /**
     * Hashes a plaintext password using BCrypt.
     * @param plainTextPassword The password to hash
     * @return A securely hashed password (e.g., "$2a$10$...")
     */
    public static String hashPassword(String plainTextPassword) {
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());
    }

    /**
     * Checks if a plaintext password matches a stored hash.
     * @param plainTextPassword The password from the login form
     * @param hashedPasswordFromDB The hash stored in the auth_db
     * @return true if the password matches, false otherwise
     */
    public static boolean checkPassword(String plainTextPassword, String hashedPasswordFromDB) {
        if (hashedPasswordFromDB == null || !hashedPasswordFromDB.startsWith("$2a$")) {
            // Not a valid bcrypt hash
            return false;
        }
        return BCrypt.checkpw(plainTextPassword, hashedPasswordFromDB);
    }

    // --- Optional: Main method for testing ---
    // You can run this file directly to see how it works
    // or to generate new hashes, and a similar file is also available named GenerateHash in the src folder.
    public static void main(String[] args) {
        String myPassword = "password123";

        // 1. Generate a hash
        String hash = hashPassword(myPassword);
        System.out.println("Generated hash: " + hash);

        // This is the hash we put in the database
        String dbHash = "$2a$10$3n.sCjD.s/IiBEP.QO81h.8b.mDtQu8gUiV.pBEqaRzU.NKY0sUey";

        // 2. Check a correct password
        boolean isCorrect = checkPassword(myPassword, dbHash);
        System.out.println("Does 'password123' match the hash? " + isCorrect); // Should be true

        // 3. Check an incorrect password
        boolean isWrong = checkPassword("wrongpass", dbHash);
        System.out.println("Does 'wrongpass' match the hash? " + isWrong); // Should be false
    }
}