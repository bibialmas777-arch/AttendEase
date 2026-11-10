package com.attendease.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentDTO {
    private Long id;

    @NotNull(message = "Faculty ID is required")
    private Long facultyId;
    private String facultyName;

    @NotNull(message = "Subject ID is required")
    private Long subjectId;
    private String subjectName;
    private String subjectCode;

    @NotNull(message = "Section ID is required")
    private Long sectionId;
    private String sectionName;
    private String className;
}

