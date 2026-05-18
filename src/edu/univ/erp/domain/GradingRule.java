package edu.univ.erp.domain;

public class GradingRule {
    private String grade;
    private double minPercentage;
    private double gradePoints;

    public GradingRule() {}

    public GradingRule(String grade, double minPercentage, double gradePoints) {
        this.grade = grade;
        this.minPercentage = minPercentage;
        this.gradePoints = gradePoints;
    }

    // Getters and Setters
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public double getMinPercentage() { return minPercentage; }
    public void setMinPercentage(double minPercentage) { this.minPercentage = minPercentage; }

    public double getGradePoints() { return gradePoints; }
    public void setGradePoints(double gradePoints) { this.gradePoints = gradePoints; }
}