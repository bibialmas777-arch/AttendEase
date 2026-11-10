package com.attendease.dto;

import com.attendease.model.enums.AttendanceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorrectionRequestCreateDTO {
    private Long attendanceId;

    @NotNull(message = "Subject is required")
    private Long subjectId;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "Current status is required")
    private AttendanceStatus currentStatus;

    @NotNull(message = "Requested status is required")
    private AttendanceStatus requestedStatus;

    @NotBlank(message = "Reason for grievance is required")
    private String reason;
}

