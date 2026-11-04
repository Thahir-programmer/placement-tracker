package com.thahirudeen.placementtracker.dto;

import java.util.Map;

import com.thahirudeen.placementtracker.entity.ApplicationStatus;

public record ApplicationStatsResponse(long total, Map<ApplicationStatus, Long> byStatus) {
}
