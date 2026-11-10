package com.attendease.dto;

import com.attendease.model.enums.AttendanceStatus;
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
public class AttendanceRecordDTO {
    private Long id;
    private Long studentId;
    private String studentName;
    private String rollNo;
    private Long classId;
    private String className;
    private Long sectionId;
    private String sectionName;
    private Long subjectId;
    private String subjectName;
    private String subjectCode;
    private LocalDate date;
    private AttendanceStatus status;
    private Long markedById;
    private String markedByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

