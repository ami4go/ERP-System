package edu.univ.erp.domain;

public class CourseGradeSummary {
    private String courseCode;
    private String courseTitle;
    private String semester;
    private int year;

    // --- MISSING FIELDS ---
    private int credits;
    private double gradePoints;

    private String assessmentsSummary;
    private double finalScore;
    private String finalGrade;

    // --- Getters and Setters ---
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    // Fix for "Cannot resolve method setCredits"
    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }

    // Fix for "Cannot resolve method setGradePoints"
    public double getGradePoints() { return gradePoints; }
    public void setGradePoints(double gradePoints) { this.gradePoints = gradePoints; }

    public String getAssessmentsSummary() { return assessmentsSummary; }
    public void setAssessmentsSummary(String assessmentsSummary) { this.assessmentsSummary = assessmentsSummary; }

    public double getFinalScore() { return finalScore; }
    public void setFinalScore(double finalScore) { this.finalScore = finalScore; }

    public String getFinalGrade() { return finalGrade; }
    public void setFinalGrade(String finalGrade) { this.finalGrade = finalGrade; }
}