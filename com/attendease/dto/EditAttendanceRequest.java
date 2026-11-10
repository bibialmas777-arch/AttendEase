package com.attendease.dto;

import com.attendease.model.enums.AttendanceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EditAttendanceRequest {
    @NotNull(message = "Attendance ID is required")
    private Long attendanceId;

    @NotNull(message = "New status is required")
    private AttendanceStatus status;

    @NotBlank(message = "Reason for modification is required for audit logs")
    private String reason;
}

