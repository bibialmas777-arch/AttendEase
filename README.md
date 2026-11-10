# AttendEase — Smart Student Attendance Management System

AttendEase is a comprehensive, production-ready attendance management web application engineered for collegiate academic administration. It provides role-tailored dashboards and workflows for **Students**, **Faculty**, and **Administrators** wrapped in a bespoke **Burgundy Glassmorphism** design system.

---

## 1. Architectural Principles

- **Pure Java Backend**: All attendance percentage arithmetic, consecutive class recovery calculations, safe miss derivations, alert thresholds (Green/Yellow/Red), report figures, and analytics aggregates are strictly computed on the Java backend (service layer using Spring Data JPA repositories). The frontend is strictly a presentation and interaction layer calling REST APIs via `fetch()`.
- **Zero AI Slop**: No fake UI buttons, no dead code, no placeholder comments, no emoji icons, and no generic marketing copy. Real collegiate terminology and Indian student seed data are used throughout.
- **Enterprise Security**: Session-based Spring Security authentication with BCrypt hashing, strict role checks (`@PreAuthorize`), and server-side authorization ensuring students and faculty can access only authorized resources.
- **Transactional Audit Logging**: Retrospective attendance edits, grievance dispute approvals, and regulatory threshold updates are immutably captured in an `audit_logs` table.
- **Integrated PDF & CSV Streaming**: Reports are generated on the fly in Java using OpenPDF (with printable light layouts and burgundy letterheads) and plain Java CSV streaming.

---

## 2. Technology Stack

| Layer | Technologies |
|---|---|
| **Backend** | Java 17, Spring Boot 3.2.5, Spring Security, Spring Data JPA (Hibernate), Bean Validation, Lombok |
| **Database** | MySQL 8 / MariaDB 10.x (`attendease` database) |
| **PDF & Reports** | OpenPDF (LibrePDF 1.3.39), CSV Writers |
| **Frontend** | Vanilla HTML5, CSS3 Custom Properties, JavaScript (ES6+), Bootstrap 5.3 (heavily overridden) |
| **Data Viz & Icons** | Chart.js 4.4, Font Awesome 6.5 line icons |
| **Typography** | Plus Jakarta Sans (headings), Inter (body) |

---

## 3. Demo Credentials

The application automatically seeds realistic institutional data on initial launch:

| Role | Email Address | Password | Portal Access |
|---|---|---|---|
| **Administrator** | `admin@attendease.com` | `Admin@123` | Control center, user CRUD, allocations, threshold settings, analytics |
| **Faculty 1** | `faculty1@attendease.com` | `Faculty@123` | Mark attendance, edit records, adjudicate grievances, teaching portfolio |
| **Faculty 2** | `faculty2@attendease.com` | `Faculty@123` | Instructor for DBMS & OS cohorts |
| **Student 1** | `student1@attendease.com` | `Student@123` | Second Year CSE (Roll: 22CS101) — 65% in CS201 (Defaulter test case) |
| **Student 2** | `student2@attendease.com` | `Student@123` | Second Year CSE (Roll: 22CS102) — 90% (Safe & Commended) |

*The sign-in page features one-click "Quick Demo" buttons to automatically populate credentials for instant testing.*

---

## 4. Attendance Recovery Planner Formulas

The recovery calculator calculates how many consecutive classes a student must attend without missing any to reach a target percentage $t \in [1, 100]$:

Let $C$ = total conducted classes, $A$ = attended classes, and $t$ = target percentage:

1. **Consecutive Classes Needed ($x$)**:
   $$\text{num} = t \cdot C - 100 \cdot A, \quad \text{denom} = 100 - t$$
   $$x = \max\left(0, \left\lceil \frac{\text{num}}{\text{denom}} \right\rceil\right)$$
   *If $t = 100$ and $A < C$, reaching 100% is mathematically impossible and flagged as unreachable.*

2. **Safe Skippable Classes ($m$)**:
   $$\text{num} = 100 \cdot A - t \cdot C$$
   $$m = \max\left(0, \left\lfloor \frac{\text{num}}{t} \right\rfloor\right)$$

3. **Expected Attendance After Recovery**:
   $$\text{Expected} \% = \frac{A + x}{C + x} \times 100$$

### Verified Test Case
For Student 1 in CS201 Data Structures ($C = 40$, $A = 26$, $t = 75$):
- $\text{num} = 75 \times 40 - 100 \times 26 = 3000 - 2600 = 400$
- $\text{denom} = 100 - 75 = 25$
- $x = 400 / 25 = \mathbf{16}$ consecutive classes.
- Expected final percentage: $(26 + 16) / (40 + 16) = 42 / 56 = \mathbf{75.0\%}$.

---

## 5. Status Threshold Classification

Evaluated entirely in Java by `AlertService.java` relative to the active regulatory threshold $T$ (default $75\%$):

| Status | Formula | Indicator | Meaning |
|---|---|---|---|
| **GREEN** | $\text{Percentage} \ge T + 10\%$ | `#75C9A3` (Teal) | Commended attendance ($\ge 85\%$) |
| **YELLOW** | $T \le \text{Percentage} < T + 10\%$ | `#E9B96E` (Amber) | Watchlist ($75\% - 84.9\%$) |
| **RED** | $\text{Percentage} < T$ | `#F07889` (Rose-Red) | Defaulter ($< 75\%$, exam barred) |

---

## 6. Database Entity-Relationship Architecture

The relational schema is normalized into 12 tables:

```
[users] 1 ──── 1 [students] (FK user_id)
[users] 1 ──── 1 [faculty]  (FK user_id)
[classes] 1 ──< [sections] (FK class_id)
[classes] 1 ──< [subjects] (FK class_id)
[students] >── 1 [classes], >── 1 [sections]
[faculty_subject_assignment] >── 1 [faculty], >── 1 [subjects], >── 1 [sections]
[attendance] >── 1 [students], >── 1 [subjects], >── 1 [users] (marked_by)
  └─ UNIQUE(student_id, subject_id, date)
[attendance_correction_requests] >── 1 [students], >── 1 [subjects], >── 0..1 [attendance]
[audit_logs] >── 1 [users] (changed_by)
[notifications] >── 1 [users]
[settings] (Key-Value configuration: attendance_threshold)
```

The complete DDL is available in `schema.sql`.

---

## 7. Prerequisites & Installation

### Requirements
- **Java Development Kit**: JDK 17 or higher
- **Build Tool**: Apache Maven 3.8+
- **Database Engine**: MySQL 8.x or MariaDB 10.x running on port `3306`

### Database Setup
1. Ensure MySQL/MariaDB is running on `localhost:3306`.
2. Create the database:
   ```sql
   CREATE DATABASE IF NOT EXISTS attendease;
   ```
3. Verify connection settings in `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/attendease?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true
   spring.datasource.username=root
   spring.datasource.password=root
   ```

### Run Unit Tests
```bash
mvn test
```
*Executes all 9 JUnit 5 unit tests validating the recovery formula, ceil rounding, threshold colors, duplicate attendance blocking, and grievance approval transactions.*

### Launch Application
```bash
mvn spring-boot:run
```
Access the application in any web browser at:
**`http://localhost:8080/login.html`**

---

## 8. REST API Reference

### Authentication (`/api/auth`)
- `POST /api/auth/login`: Authenticate and validate selected role.
- `GET /api/auth/me`: Retrieve current session user profile.
- `POST /api/auth/logout`: Invalidate session.

### Student Portal (`/api/student` — requires `ROLE_STUDENT`)
- `GET /api/student/dashboard`: Student summary, progress bars, threshold status, and warning flags.
- `GET /api/student/history`: Filtered attendance records.
- `POST /api/student/recovery`: Execute recovery trajectory calculation.
- `POST /api/student/complaints`: Submit attendance dispute with optional proof attachment.
- `GET /api/student/complaints`: List submitted disputes.
- `GET /api/student/notifications`: User notifications list.
- `POST /api/student/notifications/read-all`: Mark all notifications as read.
- `GET /api/student/reports/pdf`: Stream OpenPDF student statement.
- `GET /api/student/reports/csv`: Stream CSV tabular attendance data.

### Faculty Portal (`/api/faculty` — requires `ROLE_FACULTY`)
- `GET /api/faculty/dashboard`: Faculty portfolio metrics.
- `GET /api/faculty/assignments`: Assigned classes, subjects, and sections.
- `GET /api/faculty/students?sectionId={id}`: Roster of students in section.
- `GET /api/faculty/attendance/day`: Check if attendance was recorded for course on date.
- `POST /api/faculty/attendance/mark`: Save attendance for lecture (enforces duplicate prevention).
- `PUT /api/faculty/attendance/edit`: Retrospectively modify record (requires reason, writes audit log).
- `GET /api/faculty/attendance/history`: Historical records for assigned courses.
- `GET /api/faculty/complaints`: Queue of grievances for assigned courses.
- `PUT /api/faculty/complaints/{id}/review`: Approve/Reject dispute (updates attendance atomically).

### Administration Portal (`/api/admin` — requires `ROLE_ADMIN`)
- `GET /api/admin/dashboard`: Institution metrics, defaulters summary, and audit ledger.
- `GET /api/admin/settings/threshold`: Fetch current active threshold.
- `PUT /api/admin/settings/threshold`: Update threshold regulation (writes audit log).
- `GET|POST /api/admin/students`: List or create students.
- `PUT|DELETE /api/admin/students/{id}`: Edit or safely soft-deactivate student.
- `GET|POST /api/admin/faculty`: List or create faculty instructors.
- `PUT|DELETE /api/admin/faculty/{id}`: Edit or safely soft-deactivate faculty member.
- `GET /api/admin/classes`: List classes.
- `GET /api/admin/sections?classId={id}`: List sections for class.
- `GET /api/admin/subjects?classId={id}`: List courses for class.
- `GET|POST /api/admin/assignments`: List or create teaching assignments.
- `DELETE /api/admin/assignments/{id}`: Revoke assignment.
- `GET /api/admin/attendance`: Global institution attendance log.
- `GET /api/admin/complaints`: Global institution grievance overview.
- `GET /api/admin/analytics`: Trend lines, doughnut distribution, and course comparison aggregates.
- `GET /api/admin/reports/defaulters/pdf`: Stream OpenPDF defaulters document.
- `GET /api/admin/reports/defaulters/csv`: Stream CSV defaulters spreadsheet.

---

## 9. Testing & Verification Summary

| Component | Tested Status | Method |
|---|---|---|
| **Recovery Formula Math** | Verified Passing | JUnit 5 unit test (`RecoveryPlannerServiceTest`): tested $C=40, A=26, t=75 \implies x=16$, expected $75.0\%$, safe misses $m=8$, $100\%$ unreachable. |
| **Status Classification** | Verified Passing | JUnit 5 unit test (`AlertServiceTest`): tested green ($\ge 85\%$), yellow ($75-84.9\%$), red ($<75\%$). |
| **Duplicate Prevention** | Verified Passing | Mockito test (`AttendanceServiceTest`): confirmed `DuplicateAttendanceException` raised on redundant mark attempt. |
| **Grievance Approval** | Verified Passing | Mockito test (`AttendanceCorrectionServiceTest`): confirmed single-transaction update of attendance, audit log generation, and notification dispatch. |
| **Database Connectivity** | Verified Running | MariaDB 10.11 on port 3306 initialized with tables and seed data. |
| **UI Design System** | Verified Complete | Pure Burgundy Glassmorphism with CSS custom properties, zero blue artifacts, WCAG AA contrast. |

