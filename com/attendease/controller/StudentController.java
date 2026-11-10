package com.attendease.controller;

import com.attendease.dto.*;
import com.attendease.model.enums.AttendanceStatus;
import com.attendease.security.CustomUserDetails;
import com.attendease.service.AttendanceCorrectionService;
import com.attendease.service.AttendanceService;
import com.attendease.service.NotificationService;
import com.attendease.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
@PreAuthorize("hasRole('STUDENT')")
@RequiredArgsConstructor
public class StudentController {

    private final AttendanceService attendanceService;
    private final AttendanceCorrectionService correctionService;
    private final NotificationService notificationService;
    private final ReportService reportService;

    @GetMapping("/dashboard")
    public ResponseEntity<StudentDashboardDTO> getDashboard(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(attendanceService.getStudentDashboard(userDetails.getId()));
    }

    @GetMapping("/history")
    public ResponseEntity<List<AttendanceRecordDTO>> getHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(attendanceService.getStudentAttendanceHistory(userDetails.getId(), subjectId, startDate, endDate));
    }

    @PostMapping("/recovery")
    public ResponseEntity<RecoveryResponse> calculateRecovery(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody RecoveryRequest request) {
        return ResponseEntity.ok(attendanceService.calculateStudentRecovery(
                userDetails.getId(), request.getSubjectId(), request.getTargetPercentage()
        ));
    }

    @PostMapping(value = "/complaints", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<CorrectionResponseDTO> submitComplaint(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long subjectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam AttendanceStatus currentStatus,
            @RequestParam AttendanceStatus requestedStatus,
            @RequestParam String reason,
            @RequestParam(required = false) Long attendanceId,
            @RequestParam(required = false) MultipartFile document) {

        CorrectionRequestCreateDTO dto = CorrectionRequestCreateDTO.builder()
                .subjectId(subjectId)
                .date(date)
                .currentStatus(currentStatus)
                .requestedStatus(requestedStatus)
                .reason(reason)
                .attendanceId(attendanceId)
                .build();

        return ResponseEntity.ok(correctionService.submitRequest(dto, document, userDetails.getId()));
    }

    @GetMapping("/complaints")
    public ResponseEntity<List<CorrectionResponseDTO>> getComplaints(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(correctionService.getStudentRequests(userDetails.getId()));
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

    @GetMapping("/reports/pdf")
    public ResponseEntity<byte[]> downloadPdfReport(@AuthenticationPrincipal CustomUserDetails userDetails) {
        byte[] pdfBytes = reportService.generateStudentReportPdf(userDetails.getId());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"attend-ease-student-report.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/reports/csv")
    public ResponseEntity<byte[]> downloadCsvReport(@AuthenticationPrincipal CustomUserDetails userDetails) {
        byte[] csvBytes = reportService.generateStudentReportCsv(userDetails.getId());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"attend-ease-student-report.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvBytes);
    }
}

