package edu.univ.erp.domain;

/**
 * A DTO to hold a student's info for the gradebook.
 * REFACTORED: Replaced studentId and userId with username.
 */
public class GradebookStudent {
    private int enrollmentId;
    private String username; // Replaces studentId and userId
    private String studentName;
    private String rollNo;

    // --- Getters ---
    public int getEnrollmentId() { return enrollmentId; }
    public String getUsername() { return username; }
    public String getStudentName() { return studentName; }
    public String getRollNo() { return rollNo; }

    // --- Setters ---
    public void setEnrollmentId(int enrollmentId) { this.enrollmentId = enrollmentId; }
    public void setUsername(String username) { this.username = username; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public void setRollNo(String rollNo) { this.rollNo = rollNo; }
}