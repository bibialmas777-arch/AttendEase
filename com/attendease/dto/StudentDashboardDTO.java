package com.attendease.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentDashboardDTO {
    private Long studentId;
    private String name;
    private String rollNo;
    private String className;
    private String sectionName;
    private long overallConducted;
    private long overallAttended;
    private long overallMissed;
    private double overallPercentage;
    private String overallStatus; // GREEN, YELLOW, RED
    private double systemThreshold;
    private long pendingComplaintsCount;
    private long unreadNotificationsCount;
    private boolean warningBanner;
    private String warningMessage;
    private List<SubjectAttendanceStatsDTO> subjectStats;
}

