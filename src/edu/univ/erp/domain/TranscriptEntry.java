package edu.univ.erp.domain;

/**
 * A DTO to hold data for a single row in a student's transcript.
 */
public class TranscriptEntry {

    private String courseCode;
    private String courseTitle;
    private int credits;
    private String finalGrade;

    // Getters
    public String getCourseCode() { return courseCode; }
    public String getCourseTitle() { return courseTitle; }
    public int getCredits() { return credits; }
    public String getFinalGrade() { return finalGrade; }

    // Setters
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }
    public void setCredits(int credits) { this.credits = credits; }
    public void setFinalGrade(String finalGrade) { this.finalGrade = finalGrade; }

    // For the CSV writer
    public String[] toCsvRow() {
        return new String[]{
                courseCode,
                courseTitle,
                String.valueOf(credits),
                finalGrade
        };
    }
}