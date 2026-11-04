package com.thahirudeen.placementtracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.thahirudeen.placementtracker.entity.Application;
import com.thahirudeen.placementtracker.entity.ApplicationStatus;

public record ApplicationResponse(
		Long id,
		String companyName,
		String role,
		BigDecimal packageLpa,
		LocalDate appliedDate,
		String jobLink,
		ApplicationStatus status,
		String notes) {

	public static ApplicationResponse from(Application application) {
		return new ApplicationResponse(
				application.getId(),
				application.getCompanyName(),
				application.getRole(),
				application.getPackageLpa(),
				application.getAppliedDate(),
				application.getJobLink(),
				application.getStatus(),
				application.getNotes());
	}
}
