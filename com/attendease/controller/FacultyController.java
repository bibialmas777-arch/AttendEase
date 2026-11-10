package com.attendease.controller;

import com.attendease.dto.*;
import com.attendease.model.entity.Faculty;
import com.attendease.model.entity.FacultySubjectAssignment;
import com.attendease.model.entity.Student;
import com.attendease.repository.FacultyRepository;
import com.attendease.repository.FacultySubjectAssignmentRepository;
import com.attendease.repository.StudentRepository;
import com.attendease.security.CustomUserDetails;
import com.attendease.service.AttendanceCorrectionService;
import com.attendease.service.AttendanceService;
import com.attendease.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/faculty")
@PreAuthorize("hasRole('FACULTY')")
@RequiredArgsConstructor
public class FacultyController {

    private final AttendanceService attendanceService;
    private final AttendanceCorrectionService correctionService;
    private final NotificationService notificationService;
    private final FacultyRepository facultyRepository;
    private final FacultySubjectAssignmentRepository assignmentRepository;
    private final StudentRepository studentRepository;

    @GetMapping("/dashboard")
    public ResponseEntity<FacultyDashboardDTO> getDashboard(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(attendanceService.getFacultyDashboard(userDetails.getId()));
    }

    @GetMapping("/assignments")
    public ResponseEntity<List<AssignmentDTO>> getAssignments(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Faculty faculty = facultyRepository.findByUserId(userDetails.getId()).orElseThrow();
        List<FacultySubjectAssignment> assignments = assignmentRepository.findByFacultyIdWithDetails(faculty.getId());
        List<AssignmentDTO> dtos = assignments.stream().map(a -> AssignmentDTO.builder()
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
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/students")
    public ResponseEntity<List<Map<String, Object>>> getStudentsForSection(@RequestParam Long sectionId) {
        List<Student> students = studentRepository.findBySectionId(sectionId);
        List<Map<String, Object>> result = students.stream().map(s -> {
            Map<String, Object> map = new HashMap<>();
            map.put("studentId", s.getId());
            map.put("name", s.getUser().getName());
            map.put("rollNo", s.getRollNo());
            map.put("className", s.getClassEntity().getName());
            map.put("sectionName", s.getSection().getName());
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/attendance/day")
    public ResponseEntity<List<AttendanceRecordDTO>> getAttendanceForDay(
            @RequestParam Long subjectId,
            @RequestParam Long sectionId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(attendanceService.getAttendanceForSectionOnDate(subjectId, sectionId, date));
    }

    @PostMapping("/attendance/mark")
    public ResponseEntity<List<AttendanceRecordDTO>> markAttendance(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody MarkAttendanceRequest request) {
        return ResponseEntity.ok(attendanceService.markAttendance(request, userDetails.getId()));
    }

    @PutMapping("/attendance/edit")
    public ResponseEntity<AttendanceRecordDTO> editAttendance(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody EditAttendanceRequest request) {
        return ResponseEntity.ok(attendanceService.editAttendance(request, userDetails.getId()));
    }

    @GetMapping("/attendance/history")
    public ResponseEntity<List<AttendanceRecordDTO>> getAttendanceHistory(
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(attendanceService.getAttendanceRecords(classId, sectionId, subjectId, startDate, endDate));
    }

    @GetMapping("/complaints")
    public ResponseEntity<List<CorrectionResponseDTO>> getComplaints(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(correctionService.getFacultyRequests(userDetails.getId()));
    }

    @PutMapping("/complaints/{id}/review")
    public ResponseEntity<CorrectionResponseDTO> reviewComplaint(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody CorrectionReviewDTO reviewDTO) {
        return ResponseEntity.ok(correctionService.reviewRequest(id, reviewDTO, userDetails.getId()));
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<NotificationDTO>> getNotifications(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(notificationService.getUserNotifications(userDetails.getId()));
    }

    @PostMapping("/notifications/read-all")
    public ResponseEntity<Map<String, String>> markNotificationsRead(@AuthenticationPrincipal CustomUserDetails userDetails) {
        notificationService.markAllAsRead(userDetails.getId());
        return ResponseEntity.ok(Map.of("message", "All notifications marked as read"));
    }
}

