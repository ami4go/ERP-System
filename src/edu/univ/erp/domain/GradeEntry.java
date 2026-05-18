package edu.univ.erp.domain;

import java.math.BigDecimal;

public class GradeEntry {
    private String courseCode;
    private String courseTitle;
    private String component;
    private BigDecimal score;
    private BigDecimal totalMarks;
    private String finalGrade;
    private BigDecimal weight;

    // --- FIELDS FOR UI DISPLAY ---
    private double calculatedFinalScore;

    // --- FIELDS FOR GPA CALCULATION ---
    private String semester;
    private int year;
    private int credits;
    private double gradePoints;

    // --- Getters and Setters ---
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }

    public String getComponent() { return component; }
    public void setComponent(String component) { this.component = component; }

    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }

    public BigDecimal getTotalMarks() { return totalMarks; }
    public void setTotalMarks(BigDecimal totalMarks) { this.totalMarks = totalMarks; }

    public String getFinalGrade() { return finalGrade; }
    public void setFinalGrade(String finalGrade) { this.finalGrade = finalGrade; }

    public BigDecimal getWeight() { return weight; }
    public void setWeight(BigDecimal weight) { this.weight = weight; }

    // Fix for "Cannot resolve method setCalculatedFinalScore"
    public double getCalculatedFinalScore() { return calculatedFinalScore; }
    public void setCalculatedFinalScore(double calculatedFinalScore) { this.calculatedFinalScore = calculatedFinalScore; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }

    public double getGradePoints() { return gradePoints; }
    public void setGradePoints(double gradePoints) { this.gradePoints = gradePoints; }
}