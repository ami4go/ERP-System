<h1 align="center">🎓 University ERP System</h1>

A comprehensive, desktop-based Enterprise Resource Planning (ERP) system designed for university management. Built using **Java Swing** and **MySQL**, this application streamlines the academic lifecycle—from user administration and course creation to student registration and complex grade calculations.

## 🌟 Key Features

### 🧑‍🎓 Student Module
* **Course Catalog:** Browse available courses and sections with real-time seat availability.
* **Smart Registration:** Enrolls students with strict validation checks:
    * Prevents duplicate enrollments (same section or same course).
    * Checks section capacity limits.
    * Respects system-wide "Maintenance Mode" locks.
* **My Timetable:** View enrolled classes and drop sections if needed.
* **Grade Summary:** A consolidated view of all grades, showing assessments, weights, and the calculated final score per course.
* **Transcript Generation:** Exports an official-style CSV transcript of all completed courses.

### 👩‍🏫 Instructor Module
* **Section Management:** View assigned teaching sections and enrollment counts.
* **Advanced Gradebook:**
    * **Dynamic Assessments:** Add custom components (e.g., "Quiz 1", "Lab 2") on the fly.
    * **Weighted Grading:** Define total marks and weight percentage (e.g., "Midsem: 30%").
    * **Live Calculation:** Scores are automatically weighted and totaled as they are entered.
* **Custom Grading Scales:** Define specific grading rules (e.g., "A = 85-100, 10 Points") via a GUI dialog.
* **Class Statistics:** View performance metrics (Average, Highest, Lowest scores) per assessment.

### 🛡️ Admin Module
* **User Management:** Master-detail view to Create, List, Filter, and Delete users (Students, Instructors, Admins). Includes **Password Reset** functionality.
* **Course & Section Management:** Create master courses and schedule specific sections with assigned instructors.
* **System Security:** * **Maintenance Mode:** A global toggle to lock the system (blocks students/instructors from modifying data during critical periods).
    * **Secure Auth:** Passwords are hashed using **BCrypt** before storage.

---

## 🏗️ Technical Architecture

The project follows a strict **Layered Architecture** to ensure separation of concerns:

* **`ui` (Presentation Layer):** Contains all Swing Frames, Panels, and Dialogs. Uses `FlatLaf` for modern styling.
* **`service` (Business Logic Layer):** Handles validation, calculations (GPA, Weights), and coordinates data flow.
* **`data` (Persistence Layer):** Direct JDBC implementation using `HikariCP` for connection pooling.
* **`domain` (Data Transfer Objects):** POJOs representing core entities (Student, Course, GradeEntry).
* **`util`:** Helper classes for PDF/CSV generation and password hashing.

---

## ⚙️ Prerequisites

Before running the application, ensure you have the following installed:

1.  **Java Development Kit (JDK):** Version 17 or higher.
2.  **MySQL Server:** Version 8.0+.
3.  **Maven:** For dependency management.
4.  **IDE:** IntelliJ IDEA (recommended), Eclipse, or VS Code.

---

## 🚀 Installation & Setup Guide

Follow these steps to get the project running on your local machine.

### Step 1: Database Configuration
The system requires two databases: `auth_db` (for credentials) and `erp_db` (for academic data).

1.  Open your MySQL Client (Workbench, DBeaver, or Terminal).
2.  **Run the following SQL script** to create the schema and seed initial data:

```sql
/* --- RESET & INITIALIZE DATABASES --- */
DROP DATABASE IF EXISTS erp_db;
DROP DATABASE IF EXISTS auth_db;
CREATE DATABASE auth_db;
CREATE DATABASE erp_db;

/* --- 1. AUTH DATABASE SETUP --- */
USE auth_db;

CREATE TABLE users_auth (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    full_name VARCHAR(100),
    role ENUM('Student', 'Instructor', 'Admin') NOT NULL,
    password_hash CHAR(60) NOT NULL,
    status ENUM('Active', 'Locked') NOT NULL DEFAULT 'Active',
    last_login TIMESTAMP
);

/* Default Accounts (Password: password123) */
INSERT INTO users_auth (username, full_name, role, password_hash) VALUES
('admin1', 'System Admin', 'Admin', '$2a$10$wG7R0jrJY4BvwivS.yKjU.xmrYCKC873snX5qXnBAeI.Wd78RfXQi'),
('inst1', 'Dr. Rakesh Babu', 'Instructor', '$2a$10$wG7R0jrJY4BvwivS.yKjU.xmrYCKC873snX5qXnBAeI.Wd78RfXQi'),
('stu1', 'Amit Kumar', 'Student', '$2a$10$wG7R0jrJY4BvwivS.yKjU.xmrYCKC873snX5qXnBAeI.Wd78RfXQi');

/* --- 2. ERP DATABASE SETUP --- */
USE erp_db;

CREATE TABLE students (
    student_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    roll_no VARCHAR(20) NOT NULL UNIQUE,
    program VARCHAR(100),
    year INT
);

CREATE TABLE instructors (
    instructor_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    department VARCHAR(100),
    title VARCHAR(100)
);

CREATE TABLE courses (
    course_id INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    credits INT NOT NULL
);

CREATE TABLE sections (
    section_id INT AUTO_INCREMENT PRIMARY KEY,
    course_id INT NOT NULL,
    instructor_id INT,
    day_time VARCHAR(100),
    room VARCHAR(50),
    capacity INT NOT NULL,
    semester VARCHAR(50) NOT NULL,
    year INT NOT NULL,
    FOREIGN KEY (course_id) REFERENCES courses(course_id),
    FOREIGN KEY (instructor_id) REFERENCES instructors(instructor_id)
);

CREATE TABLE enrollments (
    enrollment_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    section_id INT NOT NULL,
    status VARCHAR(50) DEFAULT 'Enrolled',
    FOREIGN KEY (student_id) REFERENCES students(student_id),
    FOREIGN KEY (section_id) REFERENCES sections(section_id),
    UNIQUE KEY uk_student_section (student_id, section_id)
);

CREATE TABLE grading_rules (
    rule_id INT AUTO_INCREMENT PRIMARY KEY,
    section_id INT NOT NULL,
    grade_letter VARCHAR(5) NOT NULL,
    min_percentage DECIMAL(5, 2) NOT NULL,
    grade_points DECIMAL(4, 2) NOT NULL,
    FOREIGN KEY (section_id) REFERENCES sections(section_id)
);

CREATE TABLE grades (
    grade_id INT AUTO_INCREMENT PRIMARY KEY,
    enrollment_id INT NOT NULL,
    component VARCHAR(100) NOT NULL,
    score DECIMAL(5, 2),
    total_marks DECIMAL(5, 2) DEFAULT 100.00,
    weight DECIMAL(5, 2),
    final_grade VARCHAR(2),
    grade_points DECIMAL(4, 2),
    FOREIGN KEY (enrollment_id) REFERENCES enrollments(enrollment_id),
    UNIQUE KEY uk_enrollment_component (enrollment_id, component)
);

CREATE TABLE settings (
    setting_key VARCHAR(100) PRIMARY KEY,
    setting_value VARCHAR(255) NOT NULL
);

/* --- 3. SEED DATA --- */
INSERT INTO settings VALUES ('maintenance_on', 'false');

/* Link Users (IDs match auth_db auto_increments) */
INSERT INTO instructors (user_id, department, title) VALUES (2, 'Computer Science', 'Professor');
INSERT INTO students (user_id, roll_no, program, year) VALUES (3, 'B23CS001', 'B.Tech CSE', 2);

/* Create Courses & Sections */
INSERT INTO courses (code, title, credits) VALUES ('CS101', 'Introduction to Programming', 4);
INSERT INTO sections (course_id, instructor_id, day_time, room, capacity, semester, year) 
VALUES (1, 1, 'Mon/Wed 10:00', 'Room A101', 60, 'Fall', 2024);

/* Enroll Student */
INSERT INTO enrollments (student_id, section_id) VALUES (1, 1);
````

### Step 2: Connect the Application

1.  Open the project in your IDE.
2.  Navigate to `src/main/java/edu/univ/erp/data/DatabaseManager.java`.
3.  Update the database credentials to match your local MySQL installation:
    ```java
    private static final String DB_USER = "root"; // Your MySQL Username
    private static final String DB_PASS = "your_password"; // Your MySQL Password
    ```

### Step 3: Dependencies

The project uses Maven. Ensure your `pom.xml` includes the following dependencies (they should automatically download):

  * `mysql-connector-j` (Database Driver)
  * `HikariCP` (Connection Pooling)
  * `jbcrypt` (Password Hashing)
  * `flatlaf` (UI Theme)
  * `opencsv` (Transcript Export)

### Step 4: Run

1.  Navigate to `src/main/java/edu/univ/erp/Main.java`.
2.  Run the file.

-----

## 🔑 Usage & Credentials

Use the following default accounts to explore the system. All passwords are: **`password123`**

| Role | Username | Permissions |
| :--- | :--- | :--- |
| **Admin** | `admin1` | Full control. Create users, courses, sections. Toggle maintenance. |
| **Instructor** | `inst1` | Manage "CS101". Enter grades, set grading scales, view stats. |
| **Student** | `stu1` | View grades, transcript, timetable. Register for new courses. |

-----

## ⚠️ Troubleshooting

  * **"Column 'weight' not found"**: Ensure you ran the full SQL script above. The schema was updated to support custom grading weights.
  * **"Username exists"**: The Admin panel prevents duplicate usernames. If you deleted a user but the name is still taken, use the "Refresh" button in the Admin User List to see if the delete persisted.
  * **"Cannot delete section"**: You cannot delete a section if students are enrolled in it. You must drop the students first (or delete the enrollments via SQL) before deleting the section.

<!-- end list -->

```
```
