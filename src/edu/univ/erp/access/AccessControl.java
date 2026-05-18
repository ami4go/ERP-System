package edu.univ.erp.access;

import edu.univ.erp.auth.CurrentUserSession;
import edu.univ.erp.data.SystemSettingsRepository;

public class AccessControl {

    private SystemSettingsRepository settingsRepo;

    public AccessControl() {
        this.settingsRepo = new SystemSettingsRepository();
    }

    /**
     * Checks if the current user is allowed to write (save/update/delete) data.
     * @param role The role of the user trying to act (Student, Instructor, Admin)
     * @return null if allowed, or an error message String if blocked.
     */
    public String checkWriteAccess(String role) {
        // 1. Admins are always allowed, even in maintenance mode.
        if ("Admin".equalsIgnoreCase(role)) {
            return null; // OK
        }

        // 2. Check Maintenance Mode
        String maintMode = settingsRepo.getSetting("maintenance_mode");
        boolean isMaintenance = "true".equalsIgnoreCase(maintMode);

        if (isMaintenance) {
            return "System is under maintenance. Action blocked.";
        }

        return null; // OK, access granted
    }
}