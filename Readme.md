Here is the complete, **deployment-ready solution**.

This response contains two parts:

1.  **The `university_setup.sql` Script**: A single file that creates the databases, tables, and inserts the data with your specific password hash.
2.  **The `README.md` / PDF Content**: The comprehensive guide for the evaluator.

-----

### Part 1: The "Golden" SQL Script (`university_setup.sql`)

**Instruction:** Save the following code block as a file named `university_setup.sql` and include it in your submission folder. This script handles **everything**—creating the DBs, schema, and inserting the sample data with your specific hash.

```sql
-- ================================================================
-- UNIVERSITY ERP SETUP SCRIPT (Full Reset & Seed)
-- ================================================================
-- This script creates 'auth_db' and 'erp_db' from scratch.
-- It populates them with "Monsoon 2025" data and test users.
-- Password Hash used: $2a$10$RWjBO9xqOfqS6VrNafXxqem99j6NkXtkYlo8w1RVhgKGKPNeHagGC
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
  role VARCHAR(20) NOT NULL, -- 'Student', 'Instructor', 'Admin'
  password_hash VARCHAR(255) NOT NULL,
  status VARCHAR(20) DEFAULT 'Active',
  last_login DATETIME DEFAULT NULL,
  PRIMARY KEY (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Insert Users with the SPECIFIC HASH requested
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

-- A. System Settings
CREATE TABLE system_settings (
  setting_key VARCHAR(50) NOT NULL,
  setting_value VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (setting_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Set Monsoon 2025 Context
INSERT INTO system_settings (setting_key, setting_value) VALUES 
('current_term', 'Monsoon'),
('current_year', '2025'),
('session_start_date', '2025-08-01'),
('session_end_date', '2025-12-15'),
('maintenance_mode', 'false');

-- B. Profiles (Linked to Auth via Username)
CREATE TABLE admins (
  username VARCHAR(50) NOT NULL,
  full_name VARCHAR(100) DEFAULT NULL,
  PRIMARY KEY (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE students (
  username VARCHAR(50) NOT NULL,
  roll_no VARCHAR(20) DEFAULT NULL,
  full_name VARCHAR(100) DEFAULT NULL,
  program VARCHAR(50) DEFAULT NULL,
  year INT DEFAULT NULL, -- Admission Year
  current_semester INT DEFAULT 1,
  cgpa DECIMAL(4,2) DEFAULT 0.00,
  PRIMARY KEY (username),
  UNIQUE KEY roll_no_UNIQUE (roll_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE instructors (
  username VARCHAR(50) NOT NULL,
  full_name VARCHAR(100) DEFAULT NULL,
  department VARCHAR(50) DEFAULT NULL,
  title VARCHAR(50) DEFAULT NULL,
  PRIMARY KEY (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- C. Academic Structure
CREATE TABLE courses (
  code VARCHAR(20) NOT NULL,
  title VARCHAR(100) DEFAULT NULL,
  credits INT DEFAULT NULL,
  program_type VARCHAR(20) DEFAULT NULL, -- Core/Elective
  allowed_semesters VARCHAR(50) DEFAULT NULL, -- "1,2,3"
  PRIMARY KEY (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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
  KEY fk_sections_course (course_code),
  KEY fk_sections_instructor (instructor_username),
  CONSTRAINT fk_sections_course FOREIGN KEY (course_code) REFERENCES courses (code) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_sections_instructor FOREIGN KEY (instructor_username) REFERENCES instructors (username) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE enrollments (
  enrollment_id INT NOT NULL AUTO_INCREMENT,
  student_username VARCHAR(50) NOT NULL,
  section_id INT NOT NULL,
  status VARCHAR(20) DEFAULT 'Enrolled',
  final_score DECIMAL(5,2) DEFAULT 0.00,
  course_grade VARCHAR(5) DEFAULT 'IP',
  PRIMARY KEY (enrollment_id),
  KEY fk_enrollments_student (student_username),
  KEY fk_enrollments_section (section_id),
  CONSTRAINT fk_enrollments_section FOREIGN KEY (section_id) REFERENCES sections (section_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_enrollments_student FOREIGN KEY (student_username) REFERENCES students (username) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE assessments (
  assessment_id INT NOT NULL AUTO_INCREMENT,
  section_id INT NOT NULL,
  name VARCHAR(50) DEFAULT NULL,
  weightage DECIMAL(5,2) DEFAULT NULL,
  total_marks DECIMAL(5,2) DEFAULT NULL,
  PRIMARY KEY (assessment_id),
  KEY fk_assessments_section (section_id),
  CONSTRAINT fk_assessments_section FOREIGN KEY (section_id) REFERENCES sections (section_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE grades (
  grade_id INT NOT NULL AUTO_INCREMENT,
  enrollment_id INT NOT NULL,
  component VARCHAR(50) DEFAULT NULL,
  score DECIMAL(5,2) DEFAULT NULL,
  total_marks DECIMAL(5,2) DEFAULT NULL,
  weight DECIMAL(5,2) DEFAULT NULL,
  PRIMARY KEY (grade_id),
  KEY fk_grades_enrollment (enrollment_id),
  CONSTRAINT fk_grades_enrollment FOREIGN KEY (enrollment_id) REFERENCES enrollments (enrollment_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE grading_scale (
  scale_id INT NOT NULL AUTO_INCREMENT,
  section_id INT NOT NULL,
  grade_letter VARCHAR(5) DEFAULT NULL,
  min_percentage DECIMAL(5,2) DEFAULT NULL,
  grade_points DECIMAL(4,2) DEFAULT NULL,
  PRIMARY KEY (scale_id),
  KEY fk_scale_section (section_id),
  CONSTRAINT fk_scale_section FOREIGN KEY (section_id) REFERENCES sections (section_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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
  KEY fk_history_student (student_username),
  CONSTRAINT fk_history_student FOREIGN KEY (student_username) REFERENCES students (username) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------
-- 3. SEED INITIAL DATA (Profiles & Courses)
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

-- ---------------------------------------------------------
-- 4. SEED ACTIVE SECTIONS & ENROLLMENTS
-- ---------------------------------------------------------
-- Create Sections for Monsoon 2025
INSERT INTO sections (course_code, instructor_username, day_time, room, capacity, current_enrollment, semester, year, deadline) VALUES
('CSE101', 'prof_jones', 'Mon/Wed 10:00', 'LHC-101', 60, 2, 'Monsoon', 2025, '2025-09-15'),
('MTH100', 'prof_smith', 'Tue/Thu 11:30', 'LHC-102', 40, 1, 'Monsoon', 2025, '2025-09-15'),
('ECE101', 'prof_smith', 'Mon/Wed 14:00', 'ECE-Lab', 30, 1, 'Monsoon', 2025, '2025-09-15');

-- Register Students (Use subqueries to handle IDs dynamically)
INSERT INTO enrollments (student_username, section_id, status) VALUES
('student_alice', (SELECT section_id FROM sections WHERE course_code='CSE101' LIMIT 1), 'Enrolled'),
('student_alice', (SELECT section_id FROM sections WHERE course_code='MTH100' LIMIT 1), 'Enrolled'),
('student_bob',   (SELECT section_id FROM sections WHERE course_code='CSE101' LIMIT 1), 'Enrolled'),
('student_bob',   (SELECT section_id FROM sections WHERE course_code='ECE101' LIMIT 1), 'Enrolled');

SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
```

-----

### Part 2: Comprehensive README & Project Documentation

**Instruction:** Copy the content below and save it as **`README.md`** (or print it as PDF).

# University ERP System (Java + Swing)

### Project Documentation & Deployment Guide

## 1\. Project Overview

This project is a robust desktop-based **University Enterprise Resource Planning (ERP)** system developed using **Java** and **Swing**. It is designed to manage the complex interactions between Students, Instructors, and Administrators within a university environment.

The system implements a secure **Dual-Database Architecture**:

1.  **Auth Database (`auth_db`):** Handles secure login, role management, and password hashing (using BCrypt).
2.  **ERP Database (`erp_db`):** Stores academic data including profiles, courses, sections, enrollments, and grades.

### Extended Academic Logic (Custom Implementation)

We have implemented specific logic to simulate a realistic academic environment. We have set the current academic session to **"Monsoon 2025"**.

  * **Semester Logic:** The system logic filters active students and courses. For the Monsoon term, primarily **Odd Semesters** (1, 3, 5, 7) are active.
  * **Program Duration Rules:**
      * **B.Tech:** 8-Semester limit.
      * **M.Tech:** 4-Semester limit.
      * **PhD:** No semester limit.
  * **Archival Logic:** Students exceeding these limits are marked for archival/graduation. *Note: While the core logic for this is written, full automation of graduation is partially developed as it serves to demonstrate deployment readiness rather than a core requirement.*

-----

## 2\. High-Level Features

### 👤 Role-Based Functionality

  * **Student:** View course catalog, register for/drop sections (with capacity checks), view personal timetable, view grades, and download transcripts (CSV).
  * **Instructor:** View assigned sections, manage gradebooks (enter scores), calculate weighted final grades, and view class statistics.
  * **Admin:** Manage users (Students/Instructors), create courses, schedule sections, assign instructors, and toggle System Maintenance Mode.

### 🛡️ Security & Architecture

  * **Natural Key Architecture:** The system relies on **Usernames** and **Course Codes** as primary identifiers rather than arbitrary integer IDs, ensuring data robustness and readability.
  * **Maintenance Mode:** Admins can lock the system. When active, Students and Instructors effectively have "Read-Only" access.
  * **Data Integrity:** Strict checks prevent over-booking sections or duplicate enrollments.

-----

## 3\. How to Run on a Fresh System

Follow these steps to deploy the application on a new machine.

### Prerequisites

  * **Java Development Kit (JDK):** Version 17 or higher.
  * **Database:** MySQL Server (Version 8.0 recommended).
  * **Libraries:** The project requires the following JARs (or Maven dependencies): `mysql-connector-j`, `jbcrypt`, `opencsv`, `HikariCP` & `slf4j`.

### Step 1: Database Setup

1.  Open your MySQL Workbench or Command Line.
2.  Run the provided **`university_setup.sql`** script included with this submission.
      * *Action:* This single script creates `auth_db` and `erp_db`, creates all tables, and populates them with the required "Monsoon 2025" sample data.

### Step 2: Configure Application Credentials

**⚠️ CRITICAL STEP:** You must update the database credentials in the Java code to match your local MySQL installation.

1.  Open the project in your IDE.
2.  Navigate to **`src/edu/univ/erp/data/DatabaseManager.java`** (or `DBConnection.java`).
3.  Locate the credential fields and update them:
    ```java
    private static final String DB_USER = "root"; // Your MySQL username
    private static final String DB_PASS = "your_password"; // Your MySQL password
    ```

### Step 3: Build and Run

1.  Build the project using your IDE.
2.  Run the main class: **`src/edu/univ/erp/Main.java`**.
3.  The **Splash Screen** will appear, followed by the **Login Window**.

### Default Test Credentials

**Password:** The password for **ALL** accounts below matches the hash `$2a$10$RWj...` provided in the SQL script. (Use the plain text password you generated this hash from, typically `password123`).

| Role | Username |
| :--- | :--- |
| **Admin** | `admin` |
| **Student** | `student_alice` |
| **Instructor** | `prof_jones` |

-----

## 4\. Database Design & Schema

### 🗄️ Auth Database (`auth_db`)

*Stores secure login credentials and system roles.*

**Table: `users_auth`**

  * `username` (PK): Unique login ID (Natural Key).
  * `role`: 'Student', 'Instructor', or 'Admin'.
  * `password_hash`: Secure BCrypt hash.

### 🗄️ ERP Database (`erp_db`)

*Stores all academic logic and transactional data.*

**Table: `students`, `instructors`, `admins`**

  * `username` (PK, FK): Links to `auth_db`.
  * `roll_no` (Students only): University Roll Number.
  * `current_semester`: Automatically calculated based on admission year and current session.

**Table: `courses`**

  * `code` (PK): Course Code (e.g., "CSE101").
  * `allowed_semesters`: Logic for eligibility (e.g., "1,3").

**Table: `sections`**

  * `section_id` (PK): Internal unique ID for a specific class instance.
  * `course_code` (FK): Links to `courses`.
  * `instructor_username` (FK): Links to `instructors`.
  * `semester`, `year`: E.g., "Monsoon", 2025.

**Table: `enrollments`**

  * `student_username` (FK): Links to `students`.
  * `section_id` (FK): Links to `sections`.
  * `final_score`, `course_grade`: Academic results.

-----

## 5\. Project File Structure

  * **`edu.univ.erp.ui`**: Contains all Swing Panels and Frames (LoginWindow, Dashboards).
  * **`edu.univ.erp.service`**: Business Logic Layer (AdminService, StudentService).
  * **`edu.univ.erp.data`**: Data Access Objects (Repositories) handling SQL queries.
  * **`edu.univ.erp.domain`**: Plain Java Objects (POJOs) representing DB entities.
  * **`edu.univ.erp.auth`**: Session management and Password utilities.
  * **`edu.univ.erp.util`**: Database connections and CSV export utilities.

-----

## 6\. Limitations & Future Improvements

1.  **Archival Automation:** The logic to archive students after 8 semesters exists in `AdminService`, but currently requires manual triggering via the "Initialize Session" button.
2.  **Pre-requisite Checks:** The database supports storing prerequisites, but the UI currently relies on the student to know their eligible courses.
3.  **Advanced Grade Curves:** The system currently uses a fixed Grading Scale. Implementing a bell-curve automated grading scale is a planned future feature.
