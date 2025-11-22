Got it bro ✅ — **ONE SINGLE README.md**, complete, clean, GitHub-ready, with:

✅ Full Project Documentation
✅ Full SQL Script included inside the README
✅ SQL clearly highlighted in its own code block
✅ Test accounts updated to **India@123**
✅ Nothing missing, nothing external

Just **copy–paste** into `README.md` and you're done ✅

---

# ✅ FINAL `README.md`

```markdown
# University ERP System (Java + Swing)

**Course Project - Monsoon 2025**

---

## 1. Project Overview

This is a comprehensive **University Enterprise Resource Planning (ERP)** desktop application. It manages the academic lifecycle of a university, handling users, courses, enrollments, and grading.

The system is built using **Java (Swing)** for the UI and **MySQL** for data persistence. It features a secure **Dual-Database Architecture** to separate sensitive authentication data from general academic records.

### Key Architectural Features

- **Natural Key Design:** Uses **Usernames** and **Course Codes** instead of numeric IDs.
- **Role-Based Access Control (RBAC):** Different features for Admins, Students, and Instructors.
- **Secure Authentication:** BCrypt password hashing.

### Academic Logic (Monsoon 2025)

- Only **Odd Semesters (1, 3, 5, 7)** are active.
- Program limits:
  - **B.Tech:** Max 8 semesters
  - **M.Tech:** Max 4 semesters
  - **PhD:** No fixed limit
- Students beyond limits are flagged for graduation/archival.

---

## 2. Database Architecture

The system uses two separate databases:

### ✅ `auth_db` – Authentication Database
Stores secure login credentials.

**Table: `users_auth`**
- `username` (PK)
- `full_name`
- `role`
- `password_hash`
- `status`

### ✅ `erp_db` – Academic Database
Stores academic records.

**Major Tables**
- `students`, `instructors`, `admins`
- `courses`
- `sections`
- `enrollments`
- `assessments`
- `grades`
- `grading_scale`
- `academic_history`

---

## 3. Project Structure (MVC)

### 📦 UI (`edu.univ.erp.ui`)
- `LoginWindow.java`
- `AdminDashboard.java`
- `StudentDashboard.java`
- `InstructorDashboard.java`
- `UserManagementPanel.java`
- `SectionManagementPanel.java`
- `CourseCatalogPanel.java`
- `GradebookWindow.java`

### 📦 Services (`edu.univ.erp.service`)
- `AuthService.java`
- `AdminService.java`
- `StudentService.java`
- `InstructorService.java`

### 📦 Data Access (`edu.univ.erp.data`)
- `DatabaseManager.java`
- `AdminRepository.java`
- `SectionRepository.java`
- `GradeRepository.java`

### 📦 Models (`edu.univ.erp.domain`)
- `AdminUser.java`
- `Student.java`
- `Course.java`
- `Section.java`

### 📦 Utilities
- `CurrentUserSession.java`
- `PasswordUtil.java`
- `DBConnection.java`

---

## 4. How to Run

### ✅ Requirements
- **Java 17+**
- **MySQL 8+**
- **IntelliJ / Eclipse / NetBeans**

### ✅ Step 1: Configure DB Password
Open:
```

src/edu/univ/erp/util/DBConnection.java

````

Update:
```java
private static final String DB_USER = "root";
private static final String DB_PASS = "YOUR_MYSQL_PASSWORD";
````

### ✅ Step 2: Create Databases

Run the SQL script provided below.

### ✅ Step 3: Launch

Run:

```
src/edu/univ/erp/Main.java
```

### ✅ Test Accounts

| Role       | Username        | Password    | Notes          |
| ---------- | --------------- | ----------- | -------------- |
| Admin      | `admin`         | `India@123` | Full Access    |
| Student    | `student_alice` | `India@123` | Sem 3 CSE      |
| Student    | `student_bob`   | `India@123` | Sem 3 ECE      |
| Instructor | `prof_jones`    | `India@123` | Teaches CSE101 |

✅ All accounts use the password: **India@123**

---

## 5. ✅ FULL DEPLOYMENT SQL SCRIPT

> Copy-paste and run this entire script in MySQL Workbench.

```sql
-- ================================================================
-- UNIVERSITY ERP SETUP SCRIPT (Full Reset & Seed)
-- ================================================================
-- This script creates 'auth_db' and 'erp_db' from scratch.
-- It populates them with "Monsoon 2025" data and test users.
-- Password corresponds to: "India@123"
-- ================================================================

SET NAMES utf8mb4;
SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

-- ---------------------------------------------------------
-- 1. SETUP AUTH DATABASE (Users & Roles)
-- ---------------------------------------------------------
DROP DATABASE IF EXISTS auth_db;
CREATE DATABASE auth_db;
USE auth_db;

CREATE TABLE users_auth (
  username VARCHAR(50) NOT NULL,
  full_name VARCHAR(100) NOT NULL,
  role VARCHAR(20) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  status VARCHAR(20) DEFAULT 'Active',
  last_login DATETIME DEFAULT NULL,
  PRIMARY KEY (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Insert Users
INSERT INTO users_auth (username, full_name, role, password_hash) VALUES 
('admin', 'System Administrator', 'Admin', '$2a$10$RWjBO9xqOfqS6VrNafXxqem99j6NkXtkYlo8w1RVhgKGKPNeHagGC'),
('student_alice', 'Alice Smith', 'Student', '$2a$10$RWjBO9xqOfqS6VrNafXxqem99j6NkXtkYlo8w1RVhgKGKPNeHagGC'),
('student_bob', 'Bob Johnson', 'Student', '$2a$10$RWjBO9xqOfqS6VrNafXxqem99j6NkXtkYlo8w1RVhgKGKPNeHagGC'),
('prof_jones', 'Dr. Janet Jones', 'Instructor', '$2a$10$RWjBO9xqOfqS6VrNafXxqem99j6NkXtkYlo8w1RVhgKGKPNeHagGC'),
('prof_smith', 'Dr. Alan Smith', 'Instructor', '$2a$10$RWjBO9xqOfqS6VrNafXxqem99j6NkXtkYlo8w1RVhgKGKPNeHagGC');

-- ---------------------------------------------------------
-- 2. SETUP ERP DATABASE (Academic Data)
-- ---------------------------------------------------------
DROP DATABASE IF EXISTS erp_db;
CREATE DATABASE erp_db;
USE erp_db;

CREATE TABLE system_settings (
  setting_key VARCHAR(50) NOT NULL,
  setting_value VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (setting_key)
);

INSERT INTO system_settings (setting_key, setting_value) VALUES 
('current_term', 'Monsoon'),
('current_year', '2025'),
('session_start_date', '2025-08-01'),
('session_end_date', '2025-12-15'),
('maintenance_mode', 'false');

CREATE TABLE admins (
  username VARCHAR(50) NOT NULL,
  full_name VARCHAR(100) DEFAULT NULL,
  PRIMARY KEY (username)
);

CREATE TABLE students (
  username VARCHAR(50) NOT NULL,
  roll_no VARCHAR(20) DEFAULT NULL,
  full_name VARCHAR(100) DEFAULT NULL,
  program VARCHAR(50) DEFAULT NULL,
  year INT DEFAULT NULL,
  current_semester INT DEFAULT 1,
  cgpa DECIMAL(4,2) DEFAULT 0.00,
  PRIMARY KEY (username),
  UNIQUE KEY roll_no_UNIQUE (roll_no)
);

CREATE TABLE instructors (
  username VARCHAR(50) NOT NULL,
  full_name VARCHAR(100) DEFAULT NULL,
  department VARCHAR(50) DEFAULT NULL,
  title VARCHAR(50) DEFAULT NULL,
  PRIMARY KEY (username)
);

CREATE TABLE courses (
  code VARCHAR(20) NOT NULL,
  title VARCHAR(100) DEFAULT NULL,
  credits INT DEFAULT NULL,
  program_type VARCHAR(20) DEFAULT NULL,
  allowed_semesters VARCHAR(50) DEFAULT NULL,
  PRIMARY KEY (code)
);

CREATE TABLE sections (
  section_id INT NOT NULL AUTO_INCREMENT,
  course_code VARCHAR(20) NOT NULL,
  instructor_username VARCHAR(50) DEFAULT NULL,
  day_time VARCHAR(50) DEFAULT NULL,
  room VARCHAR(20) DEFAULT NULL,
  capacity INT DEFAULT 60,
  current_enrollment INT DEFAULT 0,
  semester VARCHAR(20) DEFAULT NULL,
  year INT DEFAULT NULL,
  deadline DATE DEFAULT NULL,
  PRIMARY KEY (section_id),
  FOREIGN KEY (course_code) REFERENCES courses (code),
  FOREIGN KEY (instructor_username) REFERENCES instructors (username)
);

CREATE TABLE enrollments (
  enrollment_id INT NOT NULL AUTO_INCREMENT,
  student_username VARCHAR(50) NOT NULL,
  section_id INT NOT NULL,
  status VARCHAR(20) DEFAULT 'Enrolled',
  final_score DECIMAL(5,2) DEFAULT 0.00,
  course_grade VARCHAR(5) DEFAULT 'IP',
  PRIMARY KEY (enrollment_id),
  FOREIGN KEY (section_id) REFERENCES sections (section_id),
  FOREIGN KEY (student_username) REFERENCES students (username)
);

CREATE TABLE assessments (
  assessment_id INT NOT NULL AUTO_INCREMENT,
  section_id INT NOT NULL,
  name VARCHAR(50) DEFAULT NULL,
  weightage DECIMAL(5,2) DEFAULT NULL,
  total_marks DECIMAL(5,2) DEFAULT NULL,
  PRIMARY KEY (assessment_id),
  FOREIGN KEY (section_id) REFERENCES sections (section_id)
);

CREATE TABLE grades (
  grade_id INT NOT NULL AUTO_INCREMENT,
  enrollment_id INT NOT NULL,
  component VARCHAR(50) DEFAULT NULL,
  score DECIMAL(5,2) DEFAULT NULL,
  total_marks DECIMAL(5,2) DEFAULT NULL,
  weight DECIMAL(5,2) DEFAULT NULL,
  PRIMARY KEY (grade_id),
  FOREIGN KEY (enrollment_id) REFERENCES enrollments (enrollment_id)
);

CREATE TABLE grading_scale (
  scale_id INT NOT NULL AUTO_INCREMENT,
  section_id INT NOT NULL,
  grade_letter VARCHAR(5) DEFAULT NULL,
  min_percentage DECIMAL(5,2) DEFAULT NULL,
  grade_points DECIMAL(4,2) DEFAULT NULL,
  PRIMARY KEY (scale_id),
  FOREIGN KEY (section_id) REFERENCES sections (section_id)
);

CREATE TABLE academic_history (
  history_id INT NOT NULL AUTO_INCREMENT,
  student_username VARCHAR(50) DEFAULT NULL,
  course_code VARCHAR(20) DEFAULT NULL,
  course_title VARCHAR(100) DEFAULT NULL,
  instructor_name VARCHAR(100) DEFAULT NULL,
  semester VARCHAR(20) DEFAULT NULL,
  year INT DEFAULT NULL,
  final_score DECIMAL(5,2) DEFAULT NULL,
  letter_grade VARCHAR(5) DEFAULT NULL,
  PRIMARY KEY (history_id),
  FOREIGN KEY (student_username) REFERENCES students (username)
);

-- ---------------------------------------------------------
-- 3. SEED INITIAL DATA
-- ---------------------------------------------------------
INSERT INTO admins (username, full_name) VALUES ('admin', 'System Administrator');

INSERT INTO students (username, roll_no, full_name, program, year, current_semester) VALUES
('student_alice', '2024BTCS001', 'Alice Smith', 'B.Tech (CSE)', 2024, 3),
('student_bob', '2024BTEC002', 'Bob Johnson', 'B.Tech (ECE)', 2024, 3);

INSERT INTO instructors (username, full_name, department, title) VALUES
('prof_jones', 'Dr. Janet Jones', 'CSE', 'Professor'),
('prof_smith', 'Dr. Alan Smith', 'ECE', 'Assistant Professor');

INSERT INTO courses (code, title, credits, program_type, allowed_semesters) VALUES
('CSE101', 'Intro to Programming', 4, 'Core', '1,2,3'),
('MTH100', 'Linear Algebra', 4, 'Core', '1,2,3'),
('ECE101', 'Digital Circuits', 4, 'Core', '1,2,3'),
('DES101', 'Design Drawing', 3, 'Elective', '1,2,3');

INSERT INTO sections (course_code, instructor_username, day_time, room, capacity, current_enrollment, semester, year, deadline) VALUES
('CSE101', 'prof_jones', 'Mon/Wed 10:00', 'LHC-101', 60, 2, 'Monsoon', 2025, '2025-09-15'),
('MTH100', 'prof_smith', 'Tue/Thu 11:30', 'LHC-102', 40, 1, 'Monsoon', 2025, '2025-09-15'),
('ECE101', 'prof_smith', 'Mon/Wed 14:00', 'ECE-Lab', 30, 1, 'Monsoon', 2025, '2025-09-15');

INSERT INTO enrollments (student_username, section_id, status) VALUES
('student_alice', (SELECT section_id FROM sections WHERE course_code='CSE101' LIMIT 1), 'Enrolled'),
('student_alice', (SELECT section_id FROM sections WHERE course_code='MTH100' LIMIT 1), 'Enrolled'),
('student_bob',   (SELECT section_id FROM sections WHERE course_code='CSE101' LIMIT 1), 'Enrolled'),
('student_bob',   (SELECT section_id FROM sections WHERE course_code='ECE101' LIMIT 1), 'Enrolled');

SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
```

---

## ✅ Done!

You now have:

✔ Full documentation
✔ Full SQL included
✔ GitHub-ready README
✔ Working test accounts using `India@123`

If you want:

✅ Screenshots section
✅ Features badges
✅ Demo video link styling

Just say the word 🚀
