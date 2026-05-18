package edu.univ.erp.domain;

import java.util.List;
import java.util.Map;

public class AcademicRecord {
    private List<CourseGradeSummary> courseSummaries;
    private Map<String, Double> sgpaMap;
    private double cgpa;

    public AcademicRecord(List<CourseGradeSummary> courseSummaries, Map<String, Double> sgpaMap, double cgpa) {
        this.courseSummaries = courseSummaries;
        this.sgpaMap = sgpaMap;
        this.cgpa = cgpa;
    }

    public List<CourseGradeSummary> getCourseSummaries() { return courseSummaries; }
    public Map<String, Double> getSgpaMap() { return sgpaMap; }
    public double getCgpa() { return cgpa; }
}