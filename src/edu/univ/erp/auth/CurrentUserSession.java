package edu.univ.erp.auth;

/**
 * A "Singleton" class to hold the session details of the
 * currently logged-in user.
 * REFACTORED: Removed integer IDs. Now tracks users solely by Username.
 */
public class CurrentUserSession {

    // 1. The single, private instance
    private static CurrentUserSession instance;

    // 2. The data we want to store
    // Verification: Username is unique, so no need for ID.
    private String username;
    private String role; // "Student", "Instructor", "Admin"

    // 3. Private constructor so no one else can create it
    private CurrentUserSession() {}

    /**
     * The only way to get the session object.
     * @return The single instance of CurrentUserSession
     */
    public static CurrentUserSession getInstance() {
        if (instance == null) {
            instance = new CurrentUserSession();
        }
        return instance;
    }

    /**
     * Call this on successful login.
     * Updated: No longer requires userId or profileId.
     */
    public void createSession(String username, String role) {
        this.username = username;
        this.role = role;
    }

    /**
     * Call this on logout.
     */
    public void destroySession() {
        this.username = null;
        this.role = null;
    }


    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public boolean isLoggedIn() {
        return username != null && role != null;
    }
}