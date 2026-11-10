package com.attendease.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final SettingService settingService;

    public String getStatusColor(double percentage) {
        double threshold = settingService.getAttendanceThreshold();
        return getStatusColor(percentage, threshold);
    }

    public String getStatusColor(double percentage, double threshold) {
        if (percentage >= (threshold + 10.0)) {
            return "GREEN";
        } else if (percentage >= threshold) {
            return "YELLOW";
        } else {
            return "RED";
        }
    }

    public boolean isBelowThreshold(double percentage) {
        return percentage < settingService.getAttendanceThreshold();
    }

    public boolean isBelowThreshold(double percentage, double threshold) {
        return percentage < threshold;
    }
}

