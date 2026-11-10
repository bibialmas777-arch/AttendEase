package com.attendease.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecoveryRequest {
    private Long subjectId; // null indicates overall attendance

    @NotNull(message = "Target percentage is required")
    @Min(value = 1, message = "Target percentage must be at least 1%")
    @Max(value = 100, message = "Target percentage cannot exceed 100%")
    private Integer targetPercentage;
}

