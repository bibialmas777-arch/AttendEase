package com.attendease.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecoveryResponse {
    private Long subjectId;
    private String subjectName;
    private long conductedClasses;
    private long attendedClasses;
    private double currentPercentage;
    private int targetPercentage;
    private int consecutiveNeeded;
    private int safeMisses;
    private double expectedPercentageAfterConsecutive;
    private boolean reachable;
    private String statusColor; // GREEN, YELLOW, RED
    private String message;
}

