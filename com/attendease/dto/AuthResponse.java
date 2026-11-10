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
public class AuthResponse {
    private Long id;
    private String name;
    private String email;
    private Role role;
    private String redirectUrl;
    private String message;
}

