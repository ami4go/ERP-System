<div align="center">

# 🎓 University ERP System (Java + Swing)

**📚 Course Project — Monsoon 2025**

</div>

---

## 📌 1. Project Overview

The **University ERP System** is a comprehensive desktop application that manages the academic workflow of a university, including users, courses, enrollments, and grading.

Built using:

- **Java (Swing)** for UI  
- **MySQL** for persistent storage  
- **BCrypt** for secure authentication  
- **Dual-Database Architecture**

### 🧠 Key Architectural Features

- **Natural Key Design:** Uses **Usernames** & **Course Codes** as identifiers.
- **Role-Based Access Control (RBAC):** Separate access for Students, Instructors, and Admins.
- **Secure Authentication:** Passwords stored as BCrypt hashes.
- **Deployment-Ready Academic Logic:** Configured for **Monsoon 2025**.

### 🗓 Academic Logic (Monsoon 2025)

- Only **Odd Semesters (1, 3, 5, 7)** are active.
- Program Limits:
  - **B.Tech:** Max 8 Semesters
  - **M.Tech:** Max 4 Semesters
  - **PhD:** No fixed limit
- Students exceeding limits are flagged for graduation/archival.

---

## 🗄️ 2. Database Design & Schema

The system uses **two separate databases**:

---

### 🔐 `auth_db` — Authentication Database

Stores secure account information.

**Table: `users_auth`**

| Column | Description |
|--------|------------|
| `username` | Primary Key |
| `full_name` | Display Name |
| `role` | Student / Instructor / Admin |
| `password_hash` | BCrypt Hash |
| `status` | Account Status |

---

### 🏫 `erp_db` — Academic Database

Stores academic data and transactions.

**Key Tables**

| Table | Purpose |
|-------|---------|
| `students` | Student profiles |
| `instructors` | Faculty profiles |
| `admins` | Admin profiles |
| `courses` | Course catalog |
| `sections` | Course offerings |
| `enrollments` | Student registrations |
| `grades` | Assessment scores |
| `assessments` | Mark breakdown |
| `grading_scale` | Grade thresholds |
| `academic_history` | Archived records |

---

## 🏗️ 3. Project File Structure (MVC)

```text
src/
 ├─ edu.univ.erp.ui/        
 ├─ edu.univ.erp.service/   
 ├─ edu.univ.erp.data/      
 ├─ edu.univ.erp.domain/    
 ├─ edu.univ.erp.auth/      
 ├─ edu.univ.erp.util/      
 ├─ edu.univ.erp.access/    
 └─ edu.univ.erp/           (root)
````

---

## 📦 4. Package Details

### Package: `edu.univ.erp.access` (Security & Permissions)

* **AccessControl.java**

  * Acts as the central security gatekeeper for all write operations (Create, Update, Delete) within the application.
  * Maintenance Enforcement: Queries the system settings to check if "Maintenance Mode" is currently active.
  * Role-Based Logic: If Maintenance Mode is ON, it explicitly blocks Students and Instructors from performing actions (returning an error message), while ensuring Admins always retain full access to fix or update the system.

---

### Package: `edu.univ.erp.auth` (Authentication & User Identity)

* **AuthRepository.java**

  * Acts as a read-only data bridge to the Auth Database (`auth_db`) for fetching user identity details.
  * Name Resolution: Contains logic to efficiently resolve a set of Usernames into their corresponding Full Names via the `users_auth` table.
  * UI Support: Critical for the "Natural Key" architecture, allowing the application to display human-readable names (e.g., "Dr. Janet Jones") in the interface while storing only the username (e.g., `prof_jones`) in the academic records.

* **CurrentUserSession.java**

  * Implements the Singleton Pattern to maintain a globally accessible instance of the currently logged-in user's state throughout the application lifecycle.
  * Session Management: Stores the active username and role in memory, allowing different UI panels to customize their view (e.g., "Welcome, `student_alice`") without re-querying the database.
  * Refactored Design: Updated to store Natural Keys (Strings) instead of integer IDs, ensuring full compatibility with the new database architecture where username is the primary link between Auth and ERP data.

* **PasswordUtil.java**

  * A security utility class that wraps the jBCrypt library to handle sensitive credential operations.
  * Hashing: Generates secure, salted BCrypt hashes for new passwords before they are written to the `auth_db`.
  * Verification: Compares plaintext passwords entered at login against the stored hashes, ensuring that real passwords are never exposed or stored in plain text.

---

### Package: `edu.univ.erp.data` (Data Access / DAO)

* **AdminRepository.java**

  * Serves as the primary data layer for all Administrative operations (User Creation, Course Management, Section Scheduling).
  * User Management: Handles complex, multi-step user creation by first inserting credentials into `auth_db` and then creating the corresponding profile in `erp_db` using the Username as the link.
  * Academic Logic: Manages the creation of Courses and Sections, ensuring foreign keys (Course Code, Instructor Username) are valid before insertion.
  * Refactored Queries: All SQL queries have been updated to use Natural Keys (Strings) instead of integer IDs, aligning with the new database architecture.

* **DatabaseManager.java**

  * Handles the low-level database connectivity for the entire application.
  * Connection Pooling: Implements HikariCP to create efficient pools of reusable database connections, significantly improving performance compared to opening a new connection for every query.
  * Dual-Database Management: Maintains separate configurations and data sources for `auth_db` (Security) and `erp_db` (Academic Data), ensuring traffic to one doesn't block the other.

* **GradeRepository.java**

  * Handles all database operations related to student assessment and grading.
  * Gradebook Management: Fetches and saves raw scores for individual assessment components (e.g., "Quiz 1", "Midsem") linked to a specific enrollment.
  * Transcript Generation: Retrieves a student's complete academic history, including final letter grades for completed courses, which is essential for generating the downloadable transcript CSV.
  * Refactored Logic: Updated to fetch student data using Usernames while maintaining the internal `enrollment_id` for precise grade tracking within a specific section.

* **SectionRepository.java**

  * The primary interface for managing academic offerings (Classes/Sections).
  * Course Catalog: Builds the complex query to display available courses to students, joining sections, courses, and instructors to show a complete picture (Time, Room, Professor).
  * Registration Engine: Handles the critical `registerStudent` transaction, which atomically inserts an enrollment record and increments the `current_enrollment` counter, ensuring no race conditions or over-booking occur.
  * Constraint Checking: Provides methods to verify business rules like "Is the student already enrolled?" or "Has the deadline passed?" before allowing actions.

* **SettingsRepository.java**

  * Manages application-wide configuration flags stored in the `settings` table.
  * Maintenance Control: Specifically handles reading and writing the `maintenance_on` flag. This is the data source that `AccessControl` checks to decide whether to block user actions.
  * Robust Updates: Uses the SQL `ON DUPLICATE KEY UPDATE` syntax to safely insert the setting if it doesn't exist, or update it if it does, ensuring the system state is always consistent.

* **SystemSettingsRepository.java**

  * Manages the dynamic configuration of the university's academic calendar stored in the `system_settings` table.
  * Session Control: Stores and retrieves critical flags like `current_term` (Monsoon/Winter), `current_year`, and `session_locked` status, which drive the logic for student promotion and course filtering.
  * Persistence Strategy: Uses `ON DUPLICATE KEY UPDATE` to ensure that settings are either created (if new) or updated (if existing) in a single atomic operation, preventing duplicate configuration errors.

* **UserRepository.java**

  * Acts as a specialized data fetcher for user profile attributes within the `erp_db`.
  * Natural Key Lookup: Refactored to query student data (Program, Year, Semester) directly using the Username instead of relying on integer IDs, ensuring alignment with the new database architecture.
  * Logic Support: Provides essential data to `StudentService` to determine eligibility rules, such as filtering the Course Catalog based on the student's `current_semester`.

---

### Package: `edu.univ.erp.domain` (Models)

* **AcademicRecord.java**

  * A composite Data Transfer Object (DTO) designed to encapsulate a student's entire academic history for display purposes.
  * Data Aggregation: Bundles a list of `CourseGradeSummary` objects (individual course performance), a Map of SGPA values per semester, and the final CGPA into a single object.
  * UI Support: Specifically structured to provide all necessary data for the "My Grades" panel in a single fetch operation, minimizing database calls.

* **AdminUser.java**

  * A flexible Data Transfer Object (DTO) representing any user in the system (Admin, Student, or Instructor).
  * Unified Structure: Contains fields for all roles (e.g., `rollNo` for students, `department` for instructors), allowing a single object type to populate the "Manage Users" table regardless of the user's specific role.
  * Refactored Design: Updated to use `username` as the unique identifier instead of an integer ID, matching the new database schema.

* **Assessment.java**

  * A simple Data Transfer Object (DTO) representing a specific assessment task (e.g., "Quiz 1", "Midterm") created by an instructor for a class section.
  * Gradebook Configuration: Stores critical metadata like `weightage` (percentage contribution to the final grade) and `totalMarks` (maximum score), which allows the system to automatically calculate weighted totals in the Gradebook UI.
  * Database Alignment: Corresponds directly to the `assessments` table, utilizing an internal integer ID (`id`) to uniquely identify components within the database.

* **CatalogSection.java**

  * A specialized Data Transfer Object (DTO) designed to populate the "Course Catalog" table for students.
  * Data Aggregation: Combines data from three different tables (`sections`, `courses`, `instructors`) into a single object view, including the Course Title, Instructor Name, and Schedule.
  * Refactored Design: Updated to store the Instructor's Username (String) instead of an integer ID, aligning with the Natural Key architecture.
  * UI Helper: Includes the `getSeatsInfo()` method to format enrollment data (e.g., "30/60") specifically for display in the `JTable`.

* **ClassStats.java**

  * A specialized Data Transfer Object (DTO) used exclusively for the Class Statistics feature on the Instructor Dashboard.
  * Data Aggregation: Holds calculated performance metrics for a single assessment component (e.g., "Quiz 1"), including the Average, Maximum, and Minimum scores across all students in a section.
  * Reporting: Allows the service layer to perform complex aggregations and pass simple, read-only summary data to the UI for display.

* **Course.java**

  * A Data Transfer Object (DTO) representing a generic subject in the university catalog (e.g., "Intro to Programming").
  * Natural Key Implementation: Unlike traditional designs, this class uses the Course Code (String) as its unique identifier, eliminating the need for an artificial integer ID.
  * UI Integration: Overrides the `toString()` method to return a user-friendly format ("Code: Title"), which allows instances of this class to be populated directly into Swing `JComboBox` dropdowns while remaining readable.

* **CourseGradeSummary.java**

  * A specialized Data Transfer Object (DTO) designed for the Student "My Grades" panel.
  * Data Consolidation: Aggregates disparate academic data into a single view, combining basic course info (Code, Title) with performance metrics (Final Score, Grade Points) and a textual summary of component scores.
  * GPA Calculation: Stores essential credit and grade point values needed by the `StudentService` to compute the Semester GPA (SGPA) and Cumulative GPA (CGPA) without needing to requery the database for each calculation step.

* **GradebookData.java**

  * A specialized helper class used to transfer complex Gradebook state from the Service layer to the UI in a single return object.
  * UI Binding: Wraps the `DefaultTableModel` (pre-loaded with student rows) along with critical metadata maps required for interactivity.
  * Logic Mapping: Stores the `assessmentMap` (linking specific table columns to math rules) and `rowMap` (linking visual table rows to internal database `enrollment_ids`), enabling the UI to handle live edits and save operations correctly.

* **GradebookEntry.java**

  * A Data Transfer Object (DTO) representing a single grade record (e.g., "Midsem: 45/50") for a specific student.
  * Database Mapping: Maps directly to the `grades` table, utilizing the internal `enrollment_id` to associate the score with the correct student and section.
  * Precision Handling: Uses `BigDecimal` for scores, totals, and weights to ensure mathematical accuracy during weighted average calculations, avoiding floating-point errors common with standard `double` types.

* **GradebookStudent.java**

  * A specialized Data Transfer Object (DTO) used specifically to populate the rows of the Instructor's Gradebook interface.
  * Refactored Identity: Updated to store the Student Username instead of legacy integer IDs (`studentId`/`userId`), ensuring seamless integration with the new Natural Key database schema.
  * Data Mapping: Retains the `enrollmentId` as a critical internal key, allowing the system to map UI edits back to the specific record in the `enrollments` table without exposing database mechanics to the user.

* **GradeEntry.java**

  * A versatile Data Transfer Object (DTO) used primarily for the Student's "My Grades" view.
  * Dual Purpose: Handles both granular assessment data (e.g., "Midsem: 20/30") and high-level course performance data (Credits, Grade Points) required for SGPA/CGPA calculation.
  * Database Alignment: Aligned with the Natural Key architecture by using `courseCode` (String) to link grades back to specific subjects without needing integer IDs.

* **GradingRule.java**

  * A simple Data Transfer Object (DTO) representing a single logic row in a course's grading scheme (e.g., "Grade A = Minimum 90%").
  * Customization: Allows instructors to define granular grade boundaries (`minPercentage`) and their corresponding GPA weights (`gradePoints`) for each section, rather than enforcing a hardcoded university-wide standard.
  * Data Flow: Used to transport these rules from the "Define Grading Scale" UI dialog to the `InstructorService` for saving in the database.

* **GradingScale.java**

  * A Data Transfer Object (DTO) used to define the specific grading criteria for a class section.
  * Policy Definition: Encapsulates the three key components of a grade: the Letter (e.g., "A"), the Minimum Percentage required to achieve it (e.g., 90.0), and the corresponding Grade Points (e.g., 10.0) used for GPA calculation.
  * Service Integration: Used extensively by the `InstructorService` to save and retrieve the custom grading logic defined by an instructor for their specific course sections.

* **InstructorSection.java**

  * A specialized Data Transfer Object (DTO) tailored for the Instructor Dashboard's "My Sections" view.
  * View Model: Aggregates essential class details (Course Code, Time, Room) with the live `enrollmentCount`, providing a snapshot of the instructor's teaching load.
  * Efficiency: Simplifies data transport by containing only the fields necessary for display, avoiding the overhead of passing full `Section` or `Course` objects to the UI layer.

* **Section.java**

  * A Data Transfer Object (DTO) representing a specific scheduled instance of a course (e.g., "CSE101 offered in Monsoon 2025").
  * Scheduling Hub: Bridges the abstract Course definition with concrete logistics, storing the Instructor, Room, Time, and Deadline for a specific term.
  * Refactored Design: Updated to use Natural Keys (`courseCode` and `instructorUsername`) instead of integer IDs to link to the Course and Instructor tables, ensuring data consistency with the new schema.

* **TimetableEntry.java**

  * A specialized Data Transfer Object (DTO) used to populate the Student's "My Timetable" panel.
  * View Optimization: Flattens the complex relationship between Enrollment, Section, Course, and Instructor into a single, simple object containing only the display fields needed for the schedule view (Code, Title, Time, Room).
  * Action Support: Retains the internal `enrollmentId` (int) to allow the UI to easily identify and drop specific class registrations when the "Drop Section" button is clicked.

* **TranscriptEntry.java**

  * A lightweight Data Transfer Object (DTO) used specifically for generating the official Student Transcript.
  * Data Encapsulation: Holds the final results of a completed course, including the Course Code, Title, Credits, and the Final Letter Grade.
  * Export Utility: Includes a helper method `toCsvRow()` that formats the object data into a String array, streamlining the process of writing records to a CSV file via the `TranscriptService`.

---

### Package: `edu.univ.erp.service` (Business Logic)

* **AdminService.java**

  * The "Brain" of the administrative module, orchestrating all complex user and academic operations.
  * Session Management: Implements the critical "Start New Academic Session" logic, which archives old records to history, promotes students to the next semester, and identifies graduates based on the "Monsoon 2025" rules.
  * Data Validation: Sanitizes inputs for User Creation and Course Management before passing them to the repository, ensuring data integrity (e.g., "Username cannot contain spaces").
  * Refactored Logic: Fully updated to handle the new string-based keys, ensuring that creating a section correctly links a Course Code to an Instructor Username.

* **AuthService.java**

  * The central logic hub for user authentication and session management.
  * Login Workflow: Orchestrates the login process by fetching the user's role and password hash from the database, verifying the password using `PasswordUtil`, and initializing the `CurrentUserSession` upon success.
  * Security: Handles sensitive operations like Password Changes and Last Login Updates, ensuring that all security-related write operations are centralized in one service rather than scattered across the UI.

* **InstructorService.java**

  * Orchestrates all Instructor-specific operations, acting as the bridge between the Dashboard UI and the Database.
  * Gradebook Logic: Constructs the complex table model for the Gradebook, implementing strict column locking to ensure Instructors can only edit "Raw Scores" while Student Details and Calculated Weights remain read-only.
  * Data Integrity: Validates new assessments (preventing 0-mark totals) and handles the transactional saving of grades, ensuring that updating a score automatically recalculates the weighted total and final letter grade in the database.
  * Natural Key Integration: Fetches assigned sections by querying the `instructor_username` directly from the session, fully aligning with the new database architecture.

* **StudentService.java**

  * The "Brain" of the student module, coordinating all academic actions for the logged-in student.
  * Course Registration: Validates complex enrollment rules (Seats Available, Deadline Check, Duplicate Enrollment) before committing a registration to the database.
  * Academic Record: Aggregates data from both the live `enrollments` table and the archived `academic_history` table to produce a comprehensive Report Card, calculating SGPA and CGPA on the fly.
  * Catalog Filtering: Intelligently filters the course catalog to only show sections relevant to the student's current semester and program type, enhancing the user experience.

* **TranscriptService.java**

  * Encapsulates the business logic for generating official academic transcripts.
  * Data Retrieval: Uses the `CurrentUserSession` to identify the student by username and fetches their completed course history via `GradeRepository`, filtering out courses still in progress.
  * File Generation: Integrates with the OpenCSV library to write the transcript data (Course, Title, Credits, Grade) into a standard CSV format, handling file creation and error management robustly.

---

### Package: `edu.univ.erp.ui` (User Interface)

* **AdminDashboard.java**

  * Serves as the central command center for the Administrator role, using a `JTabbedPane` layout to organize complex management features into distinct tabs.
  * Dynamic Refreshing: Implements an event listener to automatically refresh data (e.g., reloading dropdowns in "Manage Sections") whenever a specific tab is selected, ensuring the UI always displays the latest database state.
  * Session Control: Displays the active admin's username and provides a secure "Logout" function that clears the `CurrentUserSession` and returns to the login screen.

* **ChangePasswordDialog.java**

  * A modal dialog window that allows any logged-in user (Student, Instructor, or Admin) to securely update their password.
  * Validation Logic: Enforces basic security rules on the client side, such as ensuring all fields are filled and checking that the "New Password" matches the "Confirm Password" field before sending a request.
  * Service Integration: Calls `AuthService.changePassword()` in a background `SwingWorker` thread to perform the actual update in the database without freezing the UI.

* **ClassStatsPanel.java**

  * An analytical dashboard for Instructors to review student performance metrics.
  * Data Visualization: Fetches calculated statistics (Average, Highest, Lowest scores) for each assessment component in a section and displays them in a read-only `JTable`.
  * Section Selector: Uses a `JComboBox` populated via `InstructorService` to allow the instructor to switch between their assigned classes easily.

* **CourseCatalogPanel.java**

  * The central interface for Students to browse available courses for the current term.
  * Intelligent Display: Implements a smart table that filters out irrelevant courses (e.g., odd semesters for a Monsoon term) and displays real-time seat availability (e.g., "30/60").
  * Registration Logic: Contains the critical "Register" button handler, which confirms user intent, calls the `StudentService` in a background thread to prevent UI freezing, and refreshes the table instantly upon success to update seat counts.

* **CourseManagementPanel.java**

  * A comprehensive Admin interface for managing the university's academic catalog.
  * Catalog Maintenance: Allows creating new courses (with strict code, title, and credit validation) and deleting existing ones, ensuring the curriculum database remains up-to-date.
  * Modern UX: Features a live search bar and sortable table columns, making it easy for administrators to find specific subjects in a large catalog.

* **DownloadTranscriptPanel.java**

  * A dedicated UI component for Students to request an official academic transcript.
  * Asynchronous Processing: Executes the file generation logic on a background thread (`SwingWorker`) to ensure the application remains responsive during the I/O operation.
  * User Feedback: Provides real-time status updates (e.g., "Generating...", "Success") and handles errors gracefully if no completed courses are found.

* **GradebookCellEditor.java**

  * A custom Swing component that overrides the default table editing behavior to enforce strict data validation rules during grade entry.
  * Input Validation: Intercepts the "Stop Editing" event to check user input before it is committed to the table model, preventing invalid data (like negative numbers or non-numeric text) from corrupting the gradebook.
  * Business Rule Enforcement: Uses the `assessmentMap` to determine the specific maximum marks for the column being edited (e.g., "Quiz 1 is out of 10") and blocks scores that exceed this limit, ensuring data integrity.

* **GradebookTableModel.java**

  * A sophisticated `DefaultTableModel` extension that powers the dynamic Gradebook grid.
  * Dynamic Column Generation: Automatically builds the table structure based on the number of assessments, creating paired columns for "Raw Score" (Editable) and "Weighted Score" (Read-Only Calculation) for each component.
  * Live Calculation: Overrides `setValueAt` to intercept user edits. When an instructor enters a raw score, it instantly recalculates the weighted score for that component and updates the student's "Total %" column in real-time without needing a database save first.
  * Data Loading: Efficiently maps raw database rows (Students and Grades) into the visual grid structure, ensuring that existing scores are placed in the correct cells corresponding to their `Enrollment ID` and `Assessment Name`.

* **GradebookWindow.java**

  * The primary workspace for Instructors to manage student assessments and grades.
  * Dynamic Interface: Features a responsive `JTable` that adapts columns based on the number of assessments added, with built-in search filtering to quickly find specific students.
  * Live Calculation Engine: Includes a "Compute & View Finals" feature that aggregates all weighted scores against the custom Grading Scale to preview final letter grades before saving them to the database.
  * Data Persistence: Implements a robust "Save All" function that commits both individual component scores and the final calculated grade to the backend in a single transaction.

* **GradingScaleDialog.java**

  * A modal configuration window enabling Instructors to define the specific grading logic for a class section (e.g., "90% = A = 10 Points").
  * Interactive Design: Features an editable `JTable` that allows users to dynamically add rows for different grade tiers, providing flexibility over a fixed standard scale.
  * Validation & Persistence: Parses table input into `GradingScale` domain objects and transmits them to the `InstructorService`, which handles Access Control checks (blocking updates during Maintenance Mode) before saving to the database.

* **InstructorDashboard.java**

  * The central command center for Instructors, serving as the main window after a successful login.
  * Workflow Organization: Utilizes a `JTabbedPane` layout to cleanly separate distinct functional areas: "My Sections" (for entering grades) and "Class Statistics" (for reviewing performance metrics).
  * System Awareness: Embeds the `MaintenanceBanner` at the root level of the layout, ensuring that system-wide lock notifications remain visible regardless of which tab is currently active.
  * Session Management: Displays the active user's identity and handles the secure logout process, which clears the `CurrentUserSession` and redirects to the login screen.

* **LoginWindow.java**

  * The application's graphical entry point, featuring a branded splash layout and secure credential form.
  * UX Enhancements: Includes a "Show Password" toggle button (Eye Icon) for usability and high-quality rendering hints for smooth image scaling.
  * Role Routing: Validates credentials via `AuthService` and dynamically instantiates the correct dashboard (Student, Instructor, or Admin) based on the user's role stored in the session.

* **MaintenanceBanner.java**

  * A specialized UI component designed to provide real-time system status feedback to all users.
  * Live Polling: Implements a Swing `Timer` to check the system's maintenance status every 5 seconds without user intervention.
  * Dynamic Visibility: Automatically toggles its own visibility (`setVisible`) based on the backend state, ensuring users are immediately alerted with a high-contrast red banner when administrators lock the system for maintenance.

* **MyGradesPanel.java**

  * The "Report Card" view for Students, presenting a holistic view of their academic progress.
  * Data Aggregation: Fetches the student's entire academic record (both current semester grades and archived history) via `StudentService`, grouping courses by semester for a chronological display.
  * Custom Rendering: Implements a specialized `MultiLineCellRenderer` to display detailed assessment breakdowns (e.g., "Quiz 1: 8/10\nMidsem: 45/50") neatly within a single table cell.
  * Performance Metrics: Automatically calculates and displays the SGPA for each semester and the overall CGPA at the top of the dashboard.

* **MySectionsPanel.java**

  * The primary workspace panel for Instructors to view their teaching load.
  * Section Management: Fetches and displays a list of all course sections assigned to the logged-in instructor for the current term, showing real-time enrollment counts.
  * Gradebook Access: Serves as the gateway to the grading interface. When an instructor selects a row and clicks "Manage Gradebook," it launches the `GradebookWindow` specifically for that section ID.

* **MyTimetablePanel.java**

  * A dedicated dashboard panel for Students to view their active class schedule.
  * Timetable Visualization: Displays a concise list of enrolled sections, including critical logistics like Time, Room, and Instructor, fetched via the `StudentService`.
  * Drop Functionality: Provides a "Drop Selected Section" feature that uses the hidden `enrollmentId` column to securely remove a specific registration, complete with a confirmation dialog to prevent accidental drops.

* **SectionManagementPanel.java**

  * A dedicated interface for Administrators to manage the scheduling of classes (Sections).
  * Intelligent Scheduling: Uses a split-pane layout with a creation form on the left and a live search/filter list on the right, allowing Admins to quickly assign Instructors to Courses for specific semesters (e.g., "Monsoon 2025").
  * Object Mapping: Leverages custom cell renderers for `JComboBox` to display human-readable names (e.g., "Dr. Janet Jones") while internally passing full `AdminUser` objects to the `AdminService` for accurate database linking.

* **SplashScreen.java**

  * The graphical launch window displayed immediately upon application startup.
  * Visual Branding: Renders a full-window background image with a semi-transparent dark overlay to ensure the white "IIIT-Delhi ERP System" title text remains legible against any backdrop.
  * Custom Painting: Overrides `paintComponent` to handle image scaling dynamically, ensuring the splash graphic fits perfectly regardless of the screen resolution or window size.

* **StudentDashboard.java**

  * The central command center for Student users, organizing all student-facing features into a tabbed interface.
  * Integrated Workflow: Hosts dedicated panels for the Course Catalog, Personal Timetable, Grade Report, and Transcript Download, allowing seamless navigation between academic tasks.
  * System Awareness: Like the Instructor Dashboard, it embeds the `MaintenanceBanner` at the top level to ensure students are immediately aware of system lockouts.
  * Session Control: Displays the student's identity and manages the secure logout process via `CurrentUserSession`.

* **SystemSettingsPanel.java**

  * An advanced configuration dashboard exclusively for Administrators.
  * Session Management: Provides the critical "Initialize Session" control, which triggers the complex academic rollover logic (promoting students, archiving records) and locks the system to prevent accidental changes mid-term.
  * Maintenance Control: Features a toggle switch to enable/disable "Maintenance Mode," instantly blocking write access for all non-admin users across the entire application.
  * Disaster Recovery: Includes simple "Backup" and "Restore" buttons that interface with the `DbBackupService` to safeguard the entire database state.

* **UserManagementPanel.java**

  * The administrative interface for managing the entire user base (Students, Instructors, Admins).
  * Dynamic Form Handling: Uses a `CardLayout` to switch input fields on the fly (e.g., showing "Roll No" for students vs. "Department" for instructors) based on the selected role, ensuring a clean UI.
  * Advanced List Management: Features a searchable and sortable `JTable` with multiple filters (Role, Program, Year), allowing admins to quickly locate and edit specific user profiles.

---

### Package: `edu.univ.erp.util` (Utilities)

* **CsvExporter.java**

  * A utility class focused on data portability and archival.
  * Graduation Archival: Specifically designed to generate the "Graduates Report" during the session transition logic. It takes a list of students who have exceeded their program duration (e.g., > 8 semesters) and writes their details to a CSV file.
  * Data Formatting: Automatically generates unique, timestamped filenames (e.g., `Graduates_B.Tech_20251122.csv`) and formats raw student maps into structured comma-separated values for external use.

* **DbBackupService.java**

  * A system administration utility facilitating disaster recovery and data migration.
  * External Execution: Uses Java's `ProcessBuilder` to invoke native MySQL command-line tools (`mysqldump` and `mysql`) directly from the application, allowing it to snapshot the entire database state into a single SQL file.
  * Full Restoration: Provides a "Restore" function that pipes a backup SQL file back into the database engine, effectively resetting the system to a previous state.

* **DBConnection.java**

  * The fundamental database connectivity utility for the application.
  * Credential Storage: Centralizes the MySQL connection details (Host, Port, Username, Password), making it the single file that needs configuration during deployment.
  * Connection Factory: Provides a static `getConnection(dbName)` method that dynamically connects to either `auth_db` or `erp_db` based on the request, handling driver loading and JDBC URL formatting.

---

### Package: `edu.univ.erp` (Root Package)

* **Main.java**

  * The application's bootstrap entry point containing the `public static void main` method.
  * Thread Safety: Initializes the Swing UI on the Event Dispatch Thread (EDT) using `SwingUtilities.invokeLater`, ensuring thread-safe component creation.
  * Startup Sequence: Orchestrates the launch flow by displaying the `SplashScreen` for exactly 3 seconds to allow background resources to load (conceptually) before automatically transitioning to the `LoginWindow`.

---

## 🚀 5. How to Run the Application

### ✅ Requirements

* **Java JDK 17+**
* **MySQL 8+**
* **IntelliJ / Eclipse / NetBeans**

### ✅ Step 1 — Configure Database Password

Open:

```text
src/edu/univ/erp/util/DBConnection.java
```

Update:

```java
private static final String DB_USER = "root";
private static final String DB_PASS = "YOUR_MYSQL_PASSWORD";
```

### ✅ Step 2 — Run SQL Setup Script

This script will:

✅ Drop old databases
✅ Create fresh databases
✅ Seed Monsoon 2025 data
✅ Create test users

### ✅ Step 3 — Launch

Run:

```text
src/edu/univ/erp/Main.java
```

---

## 🔑 6. Test Accounts

| Role       | Username        | Password      |
| ---------- | --------------- | ------------- |
| Admin      | `admin`         | **India@123** |
| Student    | `student_alice` | **India@123** |
| Student    | `student_bob`   | **India@123** |
| Instructor | `prof_jones`    | **India@123** |

✅ All accounts use **India@123**

---

## 🧩 7. FULL DEPLOYMENT SQL SCRIPT (`university_setup.sql`)

> 📌 **Copy-Paste & Run Entire Script in MySQL Workbench**

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

<div align="center">

</div>
```
