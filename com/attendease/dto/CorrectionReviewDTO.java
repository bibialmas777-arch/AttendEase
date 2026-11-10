package com.attendease.dto;

import com.attendease.model.enums.CorrectionStatus;
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
public class CorrectionReviewDTO {
    @NotNull(message = "Review status is required")
    private CorrectionStatus status;

    @NotBlank(message = "Faculty remarks are required")
    private String remarks;
}

