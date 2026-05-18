package edu.univ.erp.domain;

public class CatalogSection {
    private int sectionId;
    private String courseCode;
    private String courseTitle;
    private int credits;
    private String instructorName;

    // REFACTORED: Changed from int instructorUserId to String instructorUsername
    private String instructorUsername;

    private String dayTime;
    private String room;
    private java.sql.Date deadline;
    private int capacity;
    private int currentEnrollment;
    private String semester;
    private int year;
    private String allowedSemesters;

    // Default Constructor
    public CatalogSection() {}

    // Full Constructor
    public CatalogSection(int sectionId, String courseCode, String courseTitle, int credits,
                          String instructorUsername, String dayTime, String room, int capacity, int currentEnrollment,
                          String allowedSemesters, String semester, int year) {
        this.sectionId = sectionId;
        this.courseCode = courseCode;
        this.courseTitle = courseTitle;
        this.credits = credits;
        this.instructorUsername = instructorUsername;
        this.dayTime = dayTime;
        this.room = room;
        this.capacity = capacity;
        this.currentEnrollment = currentEnrollment;
        this.allowedSemesters = allowedSemesters;
        this.semester = semester;
        this.year = year;
    }

    // --- Getters and Setters ---

    public int getSectionId() { return sectionId; }
    public void setSectionId(int sectionId) { this.sectionId = sectionId; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }

    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }

    public String getInstructorName() { return instructorName; }
    public void setInstructorName(String instructorName) { this.instructorName = instructorName; }

    // REFACTORED GETTER/SETTER
    public String getInstructorUsername() { return instructorUsername; }
    public void setInstructorUsername(String instructorUsername) { this.instructorUsername = instructorUsername; }

    public String getDayTime() { return dayTime; }
    public void setDayTime(String dayTime) { this.dayTime = dayTime; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public int getCurrentEnrollment() { return currentEnrollment; }
    public void setCurrentEnrollment(int currentEnrollment) { this.currentEnrollment = currentEnrollment; }

    // Helper for UI
    public String getSeatsInfo() {
        return currentEnrollment + "/" + capacity;
    }

    public String getAllowedSemesters() { return allowedSemesters; }
    public void setAllowedSemesters(String allowedSemesters) { this.allowedSemesters = allowedSemesters; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public java.sql.Date getDeadline() { return deadline; }
    public void setDeadline(java.sql.Date deadline) { this.deadline = deadline; }
}