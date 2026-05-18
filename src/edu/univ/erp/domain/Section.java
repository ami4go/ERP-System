package edu.univ.erp.domain;

import java.sql.Date;

public class Section {
    private int sectionId;
    private String courseCode;

    private String instructorUsername; // New Foreign Key

    private String instructorName; // For UI display
    private String dayTime;
    private String room;
    private int capacity;
    private String semester;
    private int year;
    private Date deadline;

    // 1. Default Constructor
    public Section() {
    }

    // 2. Full Parameterized Constructor (Updated)
    public Section(int sectionId, String courseCode, String instructorUsername,
                   String instructorName, String dayTime, String room, int capacity,
                   String semester, int year, Date deadline) {
        this.sectionId = sectionId;
        this.courseCode = courseCode;
        this.instructorUsername = instructorUsername;
        this.instructorName = instructorName;
        this.dayTime = dayTime;
        this.room = room;
        this.capacity = capacity;
        this.semester = semester;
        this.year = year;
        this.deadline = deadline;
    }

    // --- Getters and Setters ---

    public int getSectionId() { return sectionId; }
    public void setSectionId(int sectionId) { this.sectionId = sectionId; }

    // Removed getCourseId() / setCourseId()

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    // Removed getInstructorId() / setInstructorId()

    public String getInstructorUsername() { return instructorUsername; }
    public void setInstructorUsername(String instructorUsername) { this.instructorUsername = instructorUsername; }

    public String getInstructorName() { return instructorName; }
    public void setInstructorName(String instructorName) { this.instructorName = instructorName; }

    public String getDayTime() { return dayTime; }
    public void setDayTime(String dayTime) { this.dayTime = dayTime; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public Date getDeadline() { return deadline; }
    public void setDeadline(Date deadline) { this.deadline = deadline; }

    @Override
    public String toString() {
        return courseCode + " (" + semester + " " + year + ")";
    }
}