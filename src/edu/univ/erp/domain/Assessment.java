package edu.univ.erp.domain;

public class Assessment {
    private int id;
    private String name;
    private double weightage;
    private double totalMarks;

    public Assessment(int id, String name, double weightage, double totalMarks) {
        this.id = id;
        this.name = name;
        this.weightage = weightage;
        this.totalMarks = totalMarks;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public double getWeightage() { return weightage; }
    public double getTotalMarks() { return totalMarks; }
}