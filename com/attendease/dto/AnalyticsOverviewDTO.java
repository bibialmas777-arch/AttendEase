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
public class AnalyticsOverviewDTO {
    private double overallPercentage;
    private long totalPresent;
    private long totalAbsent;
    private long totalRecords;
    private List<String> monthlyTrendLabels;
    private List<Double> monthlyTrendPercentages;
    private List<String> subjectComparisonLabels;
    private List<Double> subjectComparisonPercentages;
    private List<Map<String, Object>> defaultersList;
}

