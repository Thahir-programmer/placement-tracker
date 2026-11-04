package com.thahirudeen.placementtracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.thahirudeen.placementtracker.entity.ApplicationStatus;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Has no id or user field, so a client can't choose either one
@Getter
@Setter
@NoArgsConstructor
public class ApplicationRequest {

	@NotBlank
	@Size(max = 255)
	private String companyName;

	@NotBlank
	@Size(max = 255)
	private String role;

	// Matches the numeric(5,2) column, so too-large values get a 400 rather than a database error
	@DecimalMin("0.00")
	@Digits(integer = 3, fraction = 2)
	private BigDecimal packageLpa;

	private LocalDate appliedDate;

	@Size(max = 500)
	private String jobLink;

	// Optional: create defaults to APPLIED, update keeps the current status
	private ApplicationStatus status;

	private String notes;
}
