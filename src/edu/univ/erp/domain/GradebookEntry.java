package edu.univ.erp.domain;

import java.math.BigDecimal;

/**
 * A DTO to hold a single grade entry for a student in the gradebook.
 */
public class GradebookEntry {
    private int gradeId; // 0 if new
    private int enrollmentId;
    private String component;
    private BigDecimal score;
    private BigDecimal totalMarks;
    private BigDecimal weight;
    private String finalGrade;

    // Getters
    public int getGradeId() { return gradeId; }
    public int getEnrollmentId() { return enrollmentId; }
    public String getComponent() { return component; }
    public BigDecimal getScore() { return score; }
    public BigDecimal getTotalMarks() { return totalMarks; }
    public BigDecimal getWeight() { return weight; }
    public String getFinalGrade() { return finalGrade; }

    // Setters
    public void setGradeId(int gradeId) { this.gradeId = gradeId; }
    public void setEnrollmentId(int enrollmentId) { this.enrollmentId = enrollmentId; }
    public void setComponent(String component) { this.component = component; }
    public void setScore(BigDecimal score) { this.score = score; }
    public void setTotalMarks(BigDecimal totalMarks) { this.totalMarks = totalMarks; }
    public void setWeight(BigDecimal weight) { this.weight = weight; }
    public void setFinalGrade(String finalGrade) { this.finalGrade = finalGrade; }
}