package com.attendease.controller;

import com.attendease.dto.*;
import com.attendease.model.entity.ClassEntity;
import com.attendease.model.entity.Section;
import com.attendease.model.entity.Subject;
import com.attendease.security.CustomUserDetails;
import com.attendease.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminManagementService adminService;
    private final SettingService settingService;
    private final AttendanceService attendanceService;
    private final AttendanceCorrectionService correctionService;
    private final ReportService reportService;

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardDTO> getDashboard() {
        return ResponseEntity.ok(adminService.getAdminDashboard());
    }

    @GetMapping("/settings/threshold")
    public ResponseEntity<Map<String, Object>> getThreshold() {
        return ResponseEntity.ok(Map.of("threshold", settingService.getAttendanceThreshold()));
    }

    @PutMapping("/settings/threshold")
    public ResponseEntity<Map<String, Object>> updateThreshold(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ThresholdUpdateRequest request) {
        double updated = settingService.updateAttendanceThreshold(request.getThreshold(), userDetails.getId());
        return ResponseEntity.ok(Map.of(
                "message", "Attendance threshold successfully updated to " + updated + "%",
                "threshold", updated
        ));
    }

    @GetMapping("/students")
    public ResponseEntity<List<StudentManagementDTO>> getStudents() {
        return ResponseEntity.ok(adminService.getAllStudents());
    }

    @PostMapping("/students")
    public ResponseEntity<StudentManagementDTO> createStudent(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody StudentManagementDTO dto) {
        return ResponseEntity.ok(adminService.createStudent(dto, userDetails.getId()));
    }

    @PutMapping("/students/{id}")
    public ResponseEntity<StudentManagementDTO> updateStudent(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody StudentManagementDTO dto) {
        return ResponseEntity.ok(adminService.updateStudent(id, dto, userDetails.getId()));
    }

    @DeleteMapping("/students/{id}")
    public ResponseEntity<Map<String, String>> softDeleteStudent(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        adminService.softDeleteStudent(id, userDetails.getId());
        return ResponseEntity.ok(Map.of("message", "Student deactivated successfully"));
    }

    @GetMapping("/faculty")
    public ResponseEntity<List<FacultyManagementDTO>> getFaculty() {
        return ResponseEntity.ok(adminService.getAllFaculty());
    }

    @PostMapping("/faculty")
    public ResponseEntity<FacultyManagementDTO> createFaculty(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody FacultyManagementDTO dto) {
        return ResponseEntity.ok(adminService.createFaculty(dto, userDetails.getId()));
    }

    @PutMapping("/faculty/{id}")
    public ResponseEntity<FacultyManagementDTO> updateFaculty(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody FacultyManagementDTO dto) {
        return ResponseEntity.ok(adminService.updateFaculty(id, dto, userDetails.getId()));
    }

    @DeleteMapping("/faculty/{id}")
    public ResponseEntity<Map<String, String>> softDeleteFaculty(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        adminService.softDeleteFaculty(id, userDetails.getId());
        return ResponseEntity.ok(Map.of("message", "Faculty member deactivated successfully"));
    }

    @GetMapping("/classes")
    public ResponseEntity<List<ClassEntity>> getClasses() {
        return ResponseEntity.ok(adminService.getAllClasses());
    }

    @GetMapping("/sections")
    public ResponseEntity<List<Section>> getSections(@RequestParam(required = false) Long classId) {
        if (classId != null) {
            return ResponseEntity.ok(adminService.getSectionsByClass(classId));
        }
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/subjects")
    public ResponseEntity<List<Subject>> getSubjects(@RequestParam(required = false) Long classId) {
        if (classId != null) {
            return ResponseEntity.ok(adminService.getSubjectsByClass(classId));
        }
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/assignments")
    public ResponseEntity<List<AssignmentDTO>> getAssignments() {
        return ResponseEntity.ok(adminService.getAllAssignments());
    }

    @PostMapping("/assignments")
    public ResponseEntity<AssignmentDTO> assignFaculty(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AssignmentDTO dto) {
        return ResponseEntity.ok(adminService.assignFaculty(dto, userDetails.getId()));
    }

    @DeleteMapping("/assignments/{id}")
    public ResponseEntity<Map<String, String>> deleteAssignment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        adminService.deleteAssignment(id, userDetails.getId());
        return ResponseEntity.ok(Map.of("message", "Assignment removed successfully"));
    }

    @GetMapping("/attendance")
    public ResponseEntity<List<AttendanceRecordDTO>> getAttendanceRecords(
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(attendanceService.getAttendanceRecords(classId, sectionId, subjectId, startDate, endDate));
    }

    @GetMapping("/complaints")
    public ResponseEntity<List<CorrectionResponseDTO>> getAllComplaints() {
        return ResponseEntity.ok(correctionService.getAllRequests());
    }

    @PutMapping("/complaints/{id}/review")
    public ResponseEntity<CorrectionResponseDTO> reviewComplaint(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody CorrectionReviewDTO reviewDTO) {
        return ResponseEntity.ok(correctionService.reviewRequest(id, reviewDTO, userDetails.getId()));
    }

    @GetMapping("/analytics")
    public ResponseEntity<AnalyticsOverviewDTO> getAnalytics(
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) Long subjectId) {
        return ResponseEntity.ok(adminService.getAnalytics(classId, sectionId, subjectId));
    }

    @GetMapping("/reports/defaulters/pdf")
    public ResponseEntity<byte[]> downloadDefaultersPdf(
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long sectionId) {
        byte[] pdfBytes = reportService.generateDefaultersReportPdf(classId, sectionId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"attend-ease-defaulters-report.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/reports/defaulters/csv")
    public ResponseEntity<byte[]> downloadDefaultersCsv(
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long sectionId) {
        byte[] csvBytes = reportService.generateDefaultersReportCsv(classId, sectionId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"attend-ease-defaulters-report.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvBytes);
    }
}

