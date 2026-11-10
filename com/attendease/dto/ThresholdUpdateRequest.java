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
public class ThresholdUpdateRequest {
    @NotNull(message = "Attendance threshold is required")
    @Min(value = 40, message = "Threshold cannot be below 40%")
    @Max(value = 95, message = "Threshold cannot exceed 95%")
    private Double threshold;
}

