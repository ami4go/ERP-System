package edu.univ.erp.domain;

public class GradingScale {
    private String gradeLetter;
    private double minPercentage;
    private double gradePoints;

    public GradingScale(String gradeLetter, double minPercentage, double gradePoints) {
        this.gradeLetter = gradeLetter;
        this.minPercentage = minPercentage;
        this.gradePoints = gradePoints;
    }

    // Getters
    public String getGradeLetter() { return gradeLetter; }
    public double getMinPercentage() { return minPercentage; }
    public double getGradePoints() { return gradePoints; }
}