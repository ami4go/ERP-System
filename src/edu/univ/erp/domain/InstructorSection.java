package edu.univ.erp.domain;

/**
 * A DTO to hold information about a section assigned to an instructor.
 */
public class InstructorSection {

    private int sectionId;
    private String courseCode;
    private String courseTitle;
    private String dayTime;
    private String room;
    private int enrollmentCount;

    // --- Getters ---
    public int getSectionId() { return sectionId; }
    public String getCourseCode() { return courseCode; }
    public String getCourseTitle() { return courseTitle; }
    public String getDayTime() { return dayTime; }
    public String getRoom() { return room; }
    public int getEnrollmentCount() { return enrollmentCount; }

    // --- Setters ---
    public void setSectionId(int sectionId) { this.sectionId = sectionId; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }
    public void setDayTime(String dayTime) { this.dayTime = dayTime; }
    public void setRoom(String room) { this.room = room; }
    public void setEnrollmentCount(int enrollmentCount) { this.enrollmentCount = enrollmentCount; }
}