package edu.univ.erp.domain;

public class Course {
    // Removed: private int courseId;
    private String code; // This is now the Primary Key
    private String title;
    private int credits;
    private String programType;
    private String allowedSemesters;

    // Default Constructor
    public Course() {}

    // FULL CONSTRUCTOR (Updated: Removed courseId)
    public Course(String code, String title, int credits, String programType, String allowedSemesters) {
        this.code = code;
        this.title = title;
        this.credits = credits;
        this.programType = programType;
        this.allowedSemesters = allowedSemesters;
    }

    // --- Getters and Setters ---

    // Removed getCourseId() and setCourseId()

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }

    public String getProgramType() { return programType; }
    public void setProgramType(String programType) { this.programType = programType; }

    public String getAllowedSemesters() { return allowedSemesters; }
    public void setAllowedSemesters(String allowedSemesters) { this.allowedSemesters = allowedSemesters; }

    @Override
    public String toString() {
        return code + ": " + title; // Useful for Dropdowns
    }
}