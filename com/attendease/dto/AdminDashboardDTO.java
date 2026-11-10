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
public class AdminDashboardDTO {
    private long totalStudents;
    private long totalFaculty;
    private long totalClasses;
    private long totalSubjects;
    private double overallAttendancePercentage;
    private long studentsBelowThresholdCount;
    private long pendingComplaintsCount;
    private double systemThreshold;
    private List<Map<String, Object>> recentAudits;
    private List<Map<String, Object>> defaultersSummary;
}

