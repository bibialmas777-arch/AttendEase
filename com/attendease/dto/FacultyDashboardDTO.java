package com.attendease.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacultyDashboardDTO {
    private Long facultyId;
    private String name;
    private String department;
    private int assignedClassesCount;
    private int assignedSubjectsCount;
    private long totalStudents;
    private long todayAttendanceConducted;
    private long pendingCorrectionRequests;
    private List<Map<String, Object>> assignments;
    private List<Map<String, Object>> recentActivities;
}

