package com.attendease.service;

import com.attendease.dto.*;
import com.attendease.exception.BadRequestException;
import com.attendease.exception.ResourceNotFoundException;
import com.attendease.model.entity.*;
import com.attendease.model.enums.AttendanceStatus;
import com.attendease.model.enums.CorrectionStatus;
import com.attendease.model.enums.Role;
import com.attendease.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminManagementService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final ClassRepository classRepository;
    private final SectionRepository sectionRepository;
    private final SubjectRepository subjectRepository;
    private final FacultySubjectAssignmentRepository assignmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final AttendanceCorrectionRequestRepository correctionRepository;
    private final SettingService settingService;
    private final AlertService alertService;
    private final AuditLogService auditLogService;
    private final PasswordEncoder passwordEncoder;

    public AdminDashboardDTO getAdminDashboard() {
        long totalStudents = studentRepository.findAllActiveWithDetails().size();
        long totalFaculty = facultyRepository.findAllActiveWithDetails().size();
        long totalClasses = classRepository.count();
        long totalSubjects = subjectRepository.count();
        double threshold = settingService.getAttendanceThreshold();

        long totalAttendanceRecords = attendanceRepository.count();
        double overallPercentage = 0.0;
        if (totalAttendanceRecords > 0) {
            long totalPresent = attendanceRepository.findAll().stream()
                    .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
                    .count();
            overallPercentage = BigDecimal.valueOf(totalPresent)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalAttendanceRecords), 2, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        List<Student> allStudents = studentRepository.findAllActiveWithDetails();
        List<Map<String, Object>> defaulters = new ArrayList<>();
        long belowThresholdCount = 0;

        for (Student s : allStudents) {
            long conducted = attendanceRepository.countTotalByStudentId(s.getId());
            if (conducted == 0) continue;
            long attended = attendanceRepository.countByStudentIdAndStatus(s.getId(), AttendanceStatus.PRESENT);
            double pct = BigDecimal.valueOf(attended)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(conducted), 2, RoundingMode.HALF_UP)
                    .doubleValue();

            if (pct < threshold) {
                belowThresholdCount++;
                Map<String, Object> map = new HashMap<>();
                map.put("studentId", s.getId());
                map.put("name", s.getUser().getName());
                map.put("rollNo", s.getRollNo());
                map.put("className", s.getClassEntity().getName());
                map.put("sectionName", s.getSection().getName());
                map.put("percentage", pct);
                map.put("attended", attended);
                map.put("conducted", conducted);
                map.put("status", alertService.getStatusColor(pct, threshold));
                defaulters.add(map);
            }
        }

        long pendingComplaints = correctionRepository.countByStatus(CorrectionStatus.PENDING);

        List<Map<String, Object>> auditMaps = auditLogService.getRecentAudits().stream()
                .limit(10)
                .map(a -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", a.getId());
                    m.put("entity", a.getEntity());
                    m.put("action", a.getAction());
                    m.put("oldValue", a.getOldValue());
                    m.put("newValue", a.getNewValue());
                    m.put("changedBy", a.getChangedBy().getName());
                    m.put("changedAt", a.getChangedAt());
                    return m;
                }).collect(Collectors.toList());

        return AdminDashboardDTO.builder()
                .totalStudents(totalStudents)
                .totalFaculty(totalFaculty)
                .totalClasses(totalClasses)
                .totalSubjects(totalSubjects)
                .overallAttendancePercentage(overallPercentage)
                .studentsBelowThresholdCount(belowThresholdCount)
                .pendingComplaintsCount(pendingComplaints)
                .systemThreshold(threshold)
                .recentAudits(auditMaps)
                .defaultersSummary(defaulters)
                .build();
    }

    public List<StudentManagementDTO> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(this::mapToStudentManagementDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public StudentManagementDTO createStudent(StudentManagementDTO dto, Long adminUserId) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Email already in use: " + dto.getEmail());
        }
        if (studentRepository.findByRollNo(dto.getRollNo()).isPresent()) {
            throw new BadRequestException("Roll number already in use: " + dto.getRollNo());
        }

        ClassEntity classEntity = classRepository.findById(dto.getClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Class not found: " + dto.getClassId()));
        Section section = sectionRepository.findById(dto.getSectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Section not found: " + dto.getSectionId()));

        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .passwordHash(passwordEncoder.encode(dto.getPassword() != null && !dto.getPassword().isBlank() ? dto.getPassword() : "Student@123"))
                .role(Role.STUDENT)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
        User savedUser = userRepository.save(user);

        Student student = Student.builder()
                .user(savedUser)
                .rollNo(dto.getRollNo())
                .classEntity(classEntity)
                .section(section)
                .phone(dto.getPhone())
                .build();
        Student savedStudent = studentRepository.save(student);

        User adminUser = userRepository.findById(adminUserId).orElse(null);
        if (adminUser != null) {
            auditLogService.recordAudit("Student", savedStudent.getId(), "CREATE_STUDENT", null, savedStudent.getRollNo() + " - " + savedUser.getName(), adminUser);
        }

        return mapToStudentManagementDTO(savedStudent);
    }

    @Transactional
    public StudentManagementDTO updateStudent(Long studentId, StudentManagementDTO dto, Long adminUserId) {
        Student student = studentRepository.findByIdWithDetails(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));

        User user = student.getUser();
        if (!user.getEmail().equalsIgnoreCase(dto.getEmail()) && userRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Email already in use: " + dto.getEmail());
        }

        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }
        userRepository.save(user);

        ClassEntity classEntity = classRepository.findById(dto.getClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Class not found: " + dto.getClassId()));
        Section section = sectionRepository.findById(dto.getSectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Section not found: " + dto.getSectionId()));

        student.setRollNo(dto.getRollNo());
        student.setClassEntity(classEntity);
        student.setSection(section);
        student.setPhone(dto.getPhone());
        Student updated = studentRepository.save(student);

        User adminUser = userRepository.findById(adminUserId).orElse(null);
        if (adminUser != null) {
            auditLogService.recordAudit("Student", updated.getId(), "UPDATE_STUDENT", null, updated.getRollNo(), adminUser);
        }

        return mapToStudentManagementDTO(updated);
    }

    @Transactional
    public void softDeleteStudent(Long studentId, Long adminUserId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));

        User user = student.getUser();
        user.setActive(false);
        userRepository.save(user);

        User adminUser = userRepository.findById(adminUserId).orElse(null);
        if (adminUser != null) {
            auditLogService.recordAudit("Student", studentId, "SOFT_DELETE_STUDENT", "active: true", "active: false", adminUser);
        }
    }

    public List<FacultyManagementDTO> getAllFaculty() {
        return facultyRepository.findAll().stream()
                .map(this::mapToFacultyManagementDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public FacultyManagementDTO createFaculty(FacultyManagementDTO dto, Long adminUserId) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Email already in use: " + dto.getEmail());
        }

        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .passwordHash(passwordEncoder.encode(dto.getPassword() != null && !dto.getPassword().isBlank() ? dto.getPassword() : "Faculty@123"))
                .role(Role.FACULTY)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
        User savedUser = userRepository.save(user);

        Faculty faculty = Faculty.builder()
                .user(savedUser)
                .department(dto.getDepartment())
                .phone(dto.getPhone())
                .build();
        Faculty savedFaculty = facultyRepository.save(faculty);

        User adminUser = userRepository.findById(adminUserId).orElse(null);
        if (adminUser != null) {
            auditLogService.recordAudit("Faculty", savedFaculty.getId(), "CREATE_FACULTY", null, savedUser.getName(), adminUser);
        }

        return mapToFacultyManagementDTO(savedFaculty);
    }

    @Transactional
    public FacultyManagementDTO updateFaculty(Long facultyId, FacultyManagementDTO dto, Long adminUserId) {
        Faculty faculty = facultyRepository.findByIdWithDetails(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found: " + facultyId));

        User user = faculty.getUser();
        if (!user.getEmail().equalsIgnoreCase(dto.getEmail()) && userRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Email already in use: " + dto.getEmail());
        }

        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }
        userRepository.save(user);

        faculty.setDepartment(dto.getDepartment());
        faculty.setPhone(dto.getPhone());
        Faculty updated = facultyRepository.save(faculty);

        User adminUser = userRepository.findById(adminUserId).orElse(null);
        if (adminUser != null) {
            auditLogService.recordAudit("Faculty", updated.getId(), "UPDATE_FACULTY", null, updated.getUser().getName(), adminUser);
        }

        return mapToFacultyManagementDTO(updated);
    }

    @Transactional
    public void softDeleteFaculty(Long facultyId, Long adminUserId) {
        Faculty faculty = facultyRepository.findById(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found: " + facultyId));

        User user = faculty.getUser();
        user.setActive(false);
        userRepository.save(user);

        User adminUser = userRepository.findById(adminUserId).orElse(null);
        if (adminUser != null) {
            auditLogService.recordAudit("Faculty", facultyId, "SOFT_DELETE_FACULTY", "active: true", "active: false", adminUser);
        }
    }

    public List<ClassEntity> getAllClasses() {
        return classRepository.findAll();
    }

    public List<Section> getSectionsByClass(Long classId) {
        return sectionRepository.findByClassEntityId(classId);
    }

    public List<Subject> getSubjectsByClass(Long classId) {
        return subjectRepository.findByClassEntityId(classId);
    }

    public List<AssignmentDTO> getAllAssignments() {
        return assignmentRepository.findAllWithDetails().stream().map(a -> AssignmentDTO.builder()
                .id(a.getId())
                .facultyId(a.getFaculty().getId())
                .facultyName(a.getFaculty().getUser().getName())
                .subjectId(a.getSubject().getId())
                .subjectName(a.getSubject().getName())
                .subjectCode(a.getSubject().getCode())
                .sectionId(a.getSection().getId())
                .sectionName(a.getSection().getName())
                .className(a.getSection().getClassEntity().getName())
                .build()).collect(Collectors.toList());
    }

    @Transactional
    public AssignmentDTO assignFaculty(AssignmentDTO dto, Long adminUserId) {
        if (assignmentRepository.existsByFacultyIdAndSubjectIdAndSectionId(
                dto.getFacultyId(), dto.getSubjectId(), dto.getSectionId())) {
            throw new BadRequestException("Faculty is already assigned to this subject and section");
        }

        Faculty faculty = facultyRepository.findById(dto.getFacultyId())
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found: " + dto.getFacultyId()));
        Subject subject = subjectRepository.findById(dto.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found: " + dto.getSubjectId()));
        Section section = sectionRepository.findById(dto.getSectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Section not found: " + dto.getSectionId()));

        FacultySubjectAssignment assignment = FacultySubjectAssignment.builder()
                .faculty(faculty)
                .subject(subject)
                .section(section)
                .build();
        FacultySubjectAssignment saved = assignmentRepository.save(assignment);

        User adminUser = userRepository.findById(adminUserId).orElse(null);
        if (adminUser != null) {
            auditLogService.recordAudit("Assignment", saved.getId(), "ASSIGN_FACULTY", null,
                    faculty.getUser().getName() + " -> " + subject.getName() + " (" + section.getName() + ")", adminUser);
        }

        return AssignmentDTO.builder()
                .id(saved.getId())
                .facultyId(faculty.getId())
                .facultyName(faculty.getUser().getName())
                .subjectId(subject.getId())
                .subjectName(subject.getName())
                .subjectCode(subject.getCode())
                .sectionId(section.getId())
                .sectionName(section.getName())
                .className(section.getClassEntity().getName())
                .build();
    }

    @Transactional
    public void deleteAssignment(Long assignmentId, Long adminUserId) {
        FacultySubjectAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found: " + assignmentId));

        User adminUser = userRepository.findById(adminUserId).orElse(null);
        if (adminUser != null) {
            auditLogService.recordAudit("Assignment", assignmentId, "DELETE_ASSIGNMENT",
                    assignment.getFaculty().getUser().getName() + " -> " + assignment.getSubject().getName(), null, adminUser);
        }

        assignmentRepository.delete(assignment);
    }

    public AnalyticsOverviewDTO getAnalytics(Long classId, Long sectionId, Long subjectId) {
        double threshold = settingService.getAttendanceThreshold();

        List<Object[]> monthlyData = attendanceRepository.getMonthlyTrend(classId, sectionId, subjectId);
        List<String> monthlyLabels = new ArrayList<>();
        List<Double> monthlyPcts = new ArrayList<>();

        for (Object[] row : monthlyData) {
            String month = (String) row[0];
            long total = ((Number) row[1]).longValue();
            long pres = ((Number) row[2]).longValue();
            double pct = total > 0 ? (pres * 100.0 / total) : 0.0;
            monthlyLabels.add(month);
            monthlyPcts.add(BigDecimal.valueOf(pct).setScale(1, RoundingMode.HALF_UP).doubleValue());
        }

        List<Object[]> subjectData = attendanceRepository.getSubjectAttendanceAverages(classId, sectionId);
        List<String> subjectLabels = new ArrayList<>();
        List<Double> subjectPcts = new ArrayList<>();

        for (Object[] row : subjectData) {
            String subName = (String) row[0];
            long total = ((Number) row[1]).longValue();
            long pres = ((Number) row[2]).longValue();
            double pct = total > 0 ? (pres * 100.0 / total) : 0.0;
            subjectLabels.add(subName);
            subjectPcts.add(BigDecimal.valueOf(pct).setScale(1, RoundingMode.HALF_UP).doubleValue());
        }

        List<Attendance> allFiltered = attendanceRepository.findFiltered(classId, sectionId, subjectId, null, null);
        long totalRecords = allFiltered.size();
        long totalPresent = allFiltered.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count();
        long totalAbsent = totalRecords - totalPresent;
        double overallPct = totalRecords > 0 ? (totalPresent * 100.0 / totalRecords) : 0.0;

        List<Student> activeStudents = studentRepository.findAllActiveWithDetails();
        List<Map<String, Object>> defaulters = new ArrayList<>();

        for (Student s : activeStudents) {
            if (classId != null && !s.getClassEntity().getId().equals(classId)) continue;
            if (sectionId != null && !s.getSection().getId().equals(sectionId)) continue;

            long conducted = attendanceRepository.countTotalByStudentId(s.getId());
            if (conducted == 0) continue;
            long attended = attendanceRepository.countByStudentIdAndStatus(s.getId(), AttendanceStatus.PRESENT);
            double pct = (attended * 100.0) / conducted;

            if (pct < threshold) {
                Map<String, Object> map = new HashMap<>();
                map.put("studentId", s.getId());
                map.put("name", s.getUser().getName());
                map.put("rollNo", s.getRollNo());
                map.put("className", s.getClassEntity().getName());
                map.put("sectionName", s.getSection().getName());
                map.put("percentage", BigDecimal.valueOf(pct).setScale(1, RoundingMode.HALF_UP).doubleValue());
                map.put("attended", attended);
                map.put("conducted", conducted);
                map.put("status", alertService.getStatusColor(pct, threshold));
                defaulters.add(map);
            }
        }

        return AnalyticsOverviewDTO.builder()
                .overallPercentage(BigDecimal.valueOf(overallPct).setScale(1, RoundingMode.HALF_UP).doubleValue())
                .totalPresent(totalPresent)
                .totalAbsent(totalAbsent)
                .totalRecords(totalRecords)
                .monthlyTrendLabels(monthlyLabels)
                .monthlyTrendPercentages(monthlyPcts)
                .subjectComparisonLabels(subjectLabels)
                .subjectComparisonPercentages(subjectPcts)
                .defaultersList(defaulters)
                .build();
    }

    private StudentManagementDTO mapToStudentManagementDTO(Student s) {
        return StudentManagementDTO.builder()
                .id(s.getId())
                .userId(s.getUser().getId())
                .name(s.getUser().getName())
                .email(s.getUser().getEmail())
                .rollNo(s.getRollNo())
                .classId(s.getClassEntity().getId())
                .className(s.getClassEntity().getName())
                .sectionId(s.getSection().getId())
                .sectionName(s.getSection().getName())
                .phone(s.getPhone())
                .active(s.getUser().isActive())
                .build();
    }

    private FacultyManagementDTO mapToFacultyManagementDTO(Faculty f) {
        int count = assignmentRepository.findByFacultyIdWithDetails(f.getId()).size();
        return FacultyManagementDTO.builder()
                .id(f.getId())
                .userId(f.getUser().getId())
                .name(f.getUser().getName())
                .email(f.getUser().getEmail())
                .department(f.getDepartment())
                .phone(f.getPhone())
                .active(f.getUser().isActive())
                .assignedCount(count)
                .build();
    }
}

