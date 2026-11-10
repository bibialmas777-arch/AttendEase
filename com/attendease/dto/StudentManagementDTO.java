package com.attendease.dto;

import jakarta.validation.constraints.Email;
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
public class StudentManagementDTO {
    private Long id;
    private Long userId;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Valid email is required")
    private String email;

    private String password;

    @NotBlank(message = "Roll number is required")
    private String rollNo;

    @NotNull(message = "Class is required")
    private Long classId;
    private String className;

    @NotNull(message = "Section is required")
    private Long sectionId;
    private String sectionName;

    private String phone;
    private boolean active = true;
}

