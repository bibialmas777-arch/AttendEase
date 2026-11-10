package com.attendease.dto;

import com.attendease.model.enums.AttendanceStatus;
import com.attendease.model.enums.CorrectionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorrectionResponseDTO {
    private Long id;
    private Long studentId;
    private String studentName;
    private String rollNo;
    private String className;
    private String sectionName;
    private Long subjectId;
    private String subjectName;
    private String subjectCode;
    private Long attendanceId;
    private LocalDate date;
    private AttendanceStatus currentStatus;
    private AttendanceStatus requestedStatus;
    private String reason;
    private String documentPath;
    private CorrectionStatus status;
    private String facultyRemarks;
    private Long reviewedById;
    private String reviewedByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

