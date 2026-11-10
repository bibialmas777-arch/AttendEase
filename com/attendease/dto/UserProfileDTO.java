package com.attendease.dto;

import com.attendease.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDTO {
    private Long id;
    private Long userId;
    private String name;
    private String email;
    private Role role;
    private String rollNo;
    private Long classId;
    private String className;
    private Long sectionId;
    private String sectionName;
    private String department;
    private String phone;
    private boolean active;
}

