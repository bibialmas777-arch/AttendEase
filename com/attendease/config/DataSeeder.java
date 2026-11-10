package com.attendease.config;

import com.attendease.model.entity.*;
import com.attendease.model.enums.AttendanceStatus;
import com.attendease.model.enums.CorrectionStatus;
import com.attendease.model.enums.NotificationType;
import com.attendease.model.enums.Role;
import com.attendease.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final ClassRepository classRepository;
    private final SectionRepository sectionRepository;
    private final SubjectRepository subjectRepository;
    private final FacultySubjectAssignmentRepository assignmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final AttendanceCorrectionRequestRepository correctionRepository;
    private final SettingRepository settingRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        // Ensure default attendance threshold setting
        if (settingRepository.findBySettingKey("attendance_threshold").isEmpty()) {
            settingRepository.save(Setting.builder()
                    .settingKey("attendance_threshold")
                    .settingValue("75.0")
                    .updatedAt(LocalDateTime.now())
                    .build());
            log.info("Initialized default attendance threshold: 75.0%");
        }

        // If users exist, seeding is already complete
        if (userRepository.count() > 0) {
            log.info("Database already contains {} users. Skipping initial seed.", userRepository.count());
            return;
        }

        log.info("Seeding realistic college attendance database...");

        // 1. Admin
        User admin = User.builder()
                .name("Dr. Rajesh Sharma")
                .email("admin@attendease.com")
                .passwordHash(passwordEncoder.encode("Admin@123"))
                .role(Role.ADMIN)
                .active(true)
                .createdAt(LocalDateTime.now().minusMonths(6))
                .build();
        User savedAdmin = userRepository.save(admin);

        // 2. Faculty
        String[][] facultyData = {
                {"Dr. Meenakshi Sundaram", "faculty1@attendease.com", "Computer Science", "+91 98450 11223"},
                {"Prof. Arvind Kulkarni", "faculty2@attendease.com", "Computer Science", "+91 98450 22334"},
                {"Dr. Shalini Rao", "faculty3@attendease.com", "Information Technology", "+91 98450 33445"},
                {"Prof. Vikram Deshmukh", "faculty4@attendease.com", "Mathematics", "+91 98450 44556"}
        };

        List<Faculty> facultyList = new ArrayList<>();
        for (String[] f : facultyData) {
            User fUser = User.builder()
                    .name(f[0])
                    .email(f[1])
                    .passwordHash(passwordEncoder.encode("Faculty@123"))
                    .role(Role.FACULTY)
                    .active(true)
                    .createdAt(LocalDateTime.now().minusMonths(6))
                    .build();
            User savedFacultyUser = userRepository.save(fUser);

            Faculty faculty = Faculty.builder()
                    .user(savedFacultyUser)
                    .department(f[2])
                    .phone(f[3])
                    .build();
            facultyList.add(facultyRepository.save(faculty));
        }

        // 3. Classes and Sections
        String[][] classData = {
                {"Second Year CSE", "CSE-2"},
                {"Third Year CSE", "CSE-3"},
                {"Fourth Year CSE", "CSE-4"}
        };

        List<ClassEntity> classList = new ArrayList<>();
        List<Section> sectionList = new ArrayList<>();

        for (String[] c : classData) {
            ClassEntity classEntity = classRepository.save(ClassEntity.builder()
                    .name(c[0])
                    .code(c[1])
                    .build());
            classList.add(classEntity);

            Section secA = sectionRepository.save(Section.builder()
                    .name("Section A")
                    .classEntity(classEntity)
                    .build());
            Section secB = sectionRepository.save(Section.builder()
                    .name("Section B")
                    .classEntity(classEntity)
                    .build());
            sectionList.add(secA);
            sectionList.add(secB);
        }

        // 4. Subjects for Second Year CSE (classList.get(0))
        ClassEntity cse2 = classList.get(0);
        String[][] subjectData = {
                {"Data Structures & Algorithms", "CS201", "4"},
                {"Database Management Systems", "CS202", "4"},
                {"Operating Systems", "CS203", "4"},
                {"Computer Networks", "CS204", "3"},
                {"Discrete Mathematics", "CS205", "3"},
                {"Java Programming & Lab", "CS206", "3"}
        };

        List<Subject> subjectList = new ArrayList<>();
        for (String[] s : subjectData) {
            subjectList.add(subjectRepository.save(Subject.builder()
                    .name(s[0])
                    .code(s[1])
                    .credits(Integer.parseInt(s[2]))
                    .classEntity(cse2)
                    .build()));
        }

        // 5. Faculty Subject Assignments
        Section cse2A = sectionList.get(0);
        Section cse2B = sectionList.get(1);

        assignmentRepository.save(FacultySubjectAssignment.builder().faculty(facultyList.get(0)).subject(subjectList.get(0)).section(cse2A).build());
        assignmentRepository.save(FacultySubjectAssignment.builder().faculty(facultyList.get(0)).subject(subjectList.get(0)).section(cse2B).build());
        assignmentRepository.save(FacultySubjectAssignment.builder().faculty(facultyList.get(1)).subject(subjectList.get(1)).section(cse2A).build());
        assignmentRepository.save(FacultySubjectAssignment.builder().faculty(facultyList.get(1)).subject(subjectList.get(1)).section(cse2B).build());
        assignmentRepository.save(FacultySubjectAssignment.builder().faculty(facultyList.get(2)).subject(subjectList.get(2)).section(cse2A).build());
        assignmentRepository.save(FacultySubjectAssignment.builder().faculty(facultyList.get(2)).subject(subjectList.get(3)).section(cse2A).build());
        assignmentRepository.save(FacultySubjectAssignment.builder().faculty(facultyList.get(3)).subject(subjectList.get(4)).section(cse2A).build());
        assignmentRepository.save(FacultySubjectAssignment.builder().faculty(facultyList.get(0)).subject(subjectList.get(5)).section(cse2A).build());

        // 6. Students (40 realistic Indian student names)
        String[] studentNames = {
                "Aarav Sharma", "Ananya Iyer", "Rohan Gupta", "Priya Nair", "Sneha Patel",
                "Aditya Verma", "Kavya Reddy", "Siddharth Joshi", "Diya Mukherjee", "Varun Mehta",
                "Isha Choudhury", "Rahul Menon", "Tanvi Bhat", "Arjun Kulkarni", "Neha Nambiar",
                "Karan Malhotra", "Pooja Hegde", "Nikhil Pillai", "Shruti Deshmukh", "Gaurav Agarwal",
                "Riya Sen", "Manish Saxena", "Meera Pillai", "Harsh Vardhan", "Anjali Trivedi",
                "Ayush Soni", "Shreya Rao", "Akash Das", "Divya Pillai", "Kunal Tiwari",
                "Sanya Kapoor", "Rishi Bhatt", "Tarun Goel", "Swati Mishra", "Vivek Pandey",
                "Bhavna Jain", "Sameer Sen", "Deepika Roy", "Pranav Bansal", "Rachna Chawla"
        };

        List<Student> students = new ArrayList<>();
        for (int i = 0; i < studentNames.length; i++) {
            int rollNum = 101 + i;
            String rollNo = "22CS" + rollNum;
            String email = "student" + (i + 1) + "@attendease.com";
            Section sec = (i < 20) ? cse2A : cse2B;

            User sUser = User.builder()
                    .name(studentNames[i])
                    .email(email)
                    .passwordHash(passwordEncoder.encode("Student@123"))
                    .role(Role.STUDENT)
                    .active(true)
                    .createdAt(LocalDateTime.now().minusMonths(4))
                    .build();
            User savedUser = userRepository.save(sUser);

            Student student = Student.builder()
                    .user(savedUser)
                    .rollNo(rollNo)
                    .classEntity(cse2)
                    .section(sec)
                    .phone("+91 97" + (10000000 + i * 111111))
                    .build();
            students.add(studentRepository.save(student));
        }

        // 7. Seed ~40 dates of attendance history (past 2 months)
        // Some students Green (85%+), some Yellow (75-84%), some Red (<75%)
        LocalDate today = LocalDate.now();
        List<LocalDate> lectureDates = new ArrayList<>();
        LocalDate cur = today.minusDays(60);
        while (cur.isBefore(today)) {
            // Monday to Friday
            if (cur.getDayOfWeek().getValue() <= 5) {
                lectureDates.add(cur);
            }
            cur = cur.plusDays(1);
        }

        Random rand = new Random(42); // Deterministic seed

        // Keep 40 class sessions for subject 0 (CS201) to verify formula
        List<Attendance> attendancesToSave = new ArrayList<>();
        for (Student s : students) {
            int studentIdx = students.indexOf(s);
            // Profile attendance probability:
            // Top students: 90% attendance
            // Medium students: 78% attendance
            // Defaulters (student 1, 3, 7, 12, 18, 25): ~65% attendance (e.g. Student 1: 26 out of 40 = 65%)
            double presentProb;
            if (studentIdx == 0) {
                // Exactly 26 out of 40 present for Student 1 in CS201 to match test case C=40, A=26!
                presentProb = 0.65;
            } else if (studentIdx % 5 == 0) {
                presentProb = 0.62; // Red (<75%)
            } else if (studentIdx % 3 == 0) {
                presentProb = 0.78; // Yellow (75-84%)
            } else {
                presentProb = 0.92; // Green (85%+)
            }

            for (Subject sub : subjectList) {
                int classCount = 0;
                int student1AttendedCS201 = 0;

                for (LocalDate date : lectureDates) {
                    if (classCount >= 40) break; // 40 classes conducted per subject
                    classCount++;

                    AttendanceStatus status;
                    if (studentIdx == 0 && sub.getCode().equals("CS201")) {
                        // Ensure exactly 26 out of 40 for Student 1 on CS201
                        if (student1AttendedCS201 < 26 && (rand.nextDouble() < 0.65 || 40 - classCount < 26 - student1AttendedCS201)) {
                            status = AttendanceStatus.PRESENT;
                            student1AttendedCS201++;
                        } else {
                            status = AttendanceStatus.ABSENT;
                        }
                    } else {
                        status = (rand.nextDouble() < presentProb) ? AttendanceStatus.PRESENT : AttendanceStatus.ABSENT;
                    }

                    attendancesToSave.add(Attendance.builder()
                            .student(s)
                            .subject(sub)
                            .date(date)
                            .status(status)
                            .markedBy(facultyList.get(0).getUser())
                            .createdAt(date.atTime(10, 0))
                            .updatedAt(date.atTime(10, 0))
                            .build());
                }
            }
        }
        attendanceRepository.saveAll(attendancesToSave);

        // 8. Seed Correction Requests
        Student s1 = students.get(0); // Aarav Sharma
        Subject cs201 = subjectList.get(0);
        LocalDate grievanceDate1 = lectureDates.get(lectureDates.size() - 5);
        Attendance a1 = attendanceRepository.findByStudentIdAndSubjectIdAndDate(s1.getId(), cs201.getId(), grievanceDate1).orElse(null);

        correctionRepository.save(AttendanceCorrectionRequest.builder()
                .student(s1)
                .attendance(a1)
                .subject(cs201)
                .date(grievanceDate1)
                .currentStatus(AttendanceStatus.ABSENT)
                .requestedStatus(AttendanceStatus.PRESENT)
                .reason("Attended the lecture on time, but RFID card reader showed scanner timeout. Verified with faculty in class.")
                .status(CorrectionStatus.PENDING)
                .createdAt(LocalDateTime.now().minusDays(3))
                .updatedAt(LocalDateTime.now().minusDays(3))
                .build());

        Student s2 = students.get(1); // Ananya Iyer
        LocalDate grievanceDate2 = lectureDates.get(lectureDates.size() - 8);
        Attendance a2 = attendanceRepository.findByStudentIdAndSubjectIdAndDate(s2.getId(), cs201.getId(), grievanceDate2).orElse(null);

        correctionRepository.save(AttendanceCorrectionRequest.builder()
                .student(s2)
                .attendance(a2)
                .subject(cs201)
                .date(grievanceDate2)
                .currentStatus(AttendanceStatus.ABSENT)
                .requestedStatus(AttendanceStatus.PRESENT)
                .reason("Participated in Inter-College Hackathon on behalf of CSE Department with official duty leave approval.")
                .status(CorrectionStatus.APPROVED)
                .facultyRemarks("Duty leave certificate verified. Attendance marked present.")
                .reviewedBy(facultyList.get(0).getUser())
                .createdAt(LocalDateTime.now().minusDays(5))
                .updatedAt(LocalDateTime.now().minusDays(4))
                .build());

        // 9. Seed Notifications
        notificationRepository.save(Notification.builder()
                .user(s1.getUser())
                .message("Attendance warning: Your attendance in CS201 Data Structures is currently below the mandatory 75% threshold.")
                .type(NotificationType.WARNING)
                .isRead(false)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build());

        notificationRepository.save(Notification.builder()
                .user(s2.getUser())
                .message("Your attendance correction request for CS201 Data Structures on " + grievanceDate2 + " has been APPROVED.")
                .type(NotificationType.SUCCESS)
                .isRead(true)
                .createdAt(LocalDateTime.now().minusDays(4))
                .build());

        log.info("Realistic database seed completed successfully!");
    }
}

