package edu.univ.erp.domain;

/**
 * A DTO (Data Transfer Object) to hold data for a single
 * entry in the student's timetable (an enrolled section).
 */
public class TimetableEntry {

    private int enrollmentId;
    private int sectionId;
    private String courseCode;
    private String courseTitle;
    private String instructorName;
    private String dayTime;
    private String room;

    // --- Getters ---
    public int getEnrollmentId() { return enrollmentId; }
    public int getSectionId() { return sectionId; }
    public String getCourseCode() { return courseCode; }
    public String getCourseTitle() { return courseTitle; }
    public String getInstructorName() { return instructorName; }
    public String getDayTime() { return dayTime; }
    public String getRoom() { return room; }

    // --- Setters ---
    public void setEnrollmentId(int enrollmentId) { this.enrollmentId = enrollmentId; }
    public void setSectionId(int sectionId) { this.sectionId = sectionId; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }
    public void setInstructorName(String instructorName) { this.instructorName = instructorName; }
    public void setDayTime(String dayTime) { this.dayTime = dayTime; }
    public void setRoom(String room) { this.room = room; }
}