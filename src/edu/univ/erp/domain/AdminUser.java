package edu.univ.erp.domain;

public class AdminUser {
    private String fullName;
    private String username; // This is the Primary Key

    // Student specific
    private String rollNo;
    private String program;
    private int year;
    private int currentSemester;

    // Instructor specific
    private String department;
    private String title;

    // --- Getters and Setters ---

    // Removed getUserId() and setUserId()

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRollNo() { return rollNo; }
    public void setRollNo(String rollNo) { this.rollNo = rollNo; }

    public String getProgram() { return program; }
    public void setProgram(String program) { this.program = program; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getCurrentSemester() { return currentSemester; }
    public void setCurrentSemester(int currentSemester) { this.currentSemester = currentSemester; }

    @Override
    public String toString() { return fullName; } // For dropdowns
}