package edu.univ.erp.domain;

/**
 * A DTO to hold calculated statistics for a single assessment component.
 */
public class ClassStats {

    private String componentName;
    private int studentCount;
    private double average;
    private double maxScore;
    private double minScore;
    private double totalMarks;

    // --- Getters ---
    public String getComponentName() { return componentName; }
    public int getStudentCount() { return studentCount; }
    public double getAverage() { return average; }
    public double getMaxScore() { return maxScore; }
    public double getMinScore() { return minScore; }
    public double getTotalMarks() { return totalMarks; }

    // --- Setters ---
    public void setComponentName(String componentName) { this.componentName = componentName; }
    public void setStudentCount(int studentCount) { this.studentCount = studentCount; }
    public void setAverage(double average) { this.average = average; }
    public void setMaxScore(double maxScore) { this.maxScore = maxScore; }
    public void setMinScore(double minScore) { this.minScore = minScore; }
    public void setTotalMarks(double totalMarks) { this.totalMarks = totalMarks; }
}