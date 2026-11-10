package com.attendease.controller;

import com.attendease.dto.AuthResponse;
import com.attendease.dto.LoginRequest;
import com.attendease.dto.UserProfileDTO;
import com.attendease.exception.BadRequestException;
import com.attendease.model.entity.Faculty;
import com.attendease.model.entity.Student;
import com.attendease.model.entity.User;
import com.attendease.model.enums.Role;
import com.attendease.repository.FacultyRepository;
import com.attendease.repository.StudentRepository;
import com.attendease.repository.UserRepository;
import com.attendease.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            // Strict Role Validation: Ensure user has the selected role
            if (userDetails.getRole() != loginRequest.getRole()) {
                throw new BadRequestException(String.format(
                        "Access denied. This account has role '%s', but you attempted to login as '%s'. Please select the correct portal tab.",
                        userDetails.getRole(), loginRequest.getRole()
                ));
            }

            SecurityContext securityContext = SecurityContextHolder.getContext();
            securityContext.setAuthentication(authentication);

            // Create HTTP Session
            HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);

            String redirectUrl = switch (userDetails.getRole()) {
                case STUDENT -> "/pages/student/dashboard.html";
                case FACULTY -> "/pages/faculty/dashboard.html";
                case ADMIN -> "/pages/admin/dashboard.html";
            };

            return ResponseEntity.ok(AuthResponse.builder()
                    .id(userDetails.getId())
                    .name(userDetails.getName())
                    .email(userDetails.getEmail())
                    .role(userDetails.getRole())
                    .redirectUrl(redirectUrl)
                    .message("Login successful")
                    .build());
        } catch (BadCredentialsException ex) {
            throw new BadRequestException("Invalid email or password");
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileDTO> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        User user = userRepository.findById(userDetails.getId()).orElseThrow();
        UserProfileDTO.UserProfileDTOBuilder builder = UserProfileDTO.builder()
                .id(user.getId())
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .active(user.isActive());

        if (user.getRole() == Role.STUDENT) {
            studentRepository.findByUserIdWithDetails(user.getId()).ifPresent(s -> {
                builder.rollNo(s.getRollNo())
                        .classId(s.getClassEntity().getId())
                        .className(s.getClassEntity().getName())
                        .sectionId(s.getSection().getId())
                        .sectionName(s.getSection().getName())
                        .phone(s.getPhone());
            });
        } else if (user.getRole() == Role.FACULTY) {
            facultyRepository.findByUserId(user.getId()).ifPresent(f -> {
                builder.department(f.getDepartment())
                        .phone(f.getPhone());
            });
        }

        return ResponseEntity.ok(builder.build());
    }
}

