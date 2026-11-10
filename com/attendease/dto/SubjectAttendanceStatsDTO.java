package com.attendease.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubjectAttendanceStatsDTO {
    private Long subjectId;
    private String subjectName;
    private String subjectCode;
    private Integer credits;
    private long conducted;
    private long attended;
    private long missed;
    private double percentage;
    private String statusColor; // GREEN, YELLOW, RED
    private int consecutiveNeeded;
    private int safeMisses;
}

