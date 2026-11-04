package com.thahirudeen.placementtracker.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.thahirudeen.placementtracker.dto.ApplicationRequest;
import com.thahirudeen.placementtracker.dto.ApplicationResponse;
import com.thahirudeen.placementtracker.dto.ApplicationStatsResponse;
import com.thahirudeen.placementtracker.entity.ApplicationStatus;
import com.thahirudeen.placementtracker.service.ApplicationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// The user id always comes from the verified token, never from the request
@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

	private final ApplicationService applicationService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ApplicationResponse create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ApplicationRequest req) {
		return applicationService.create(userId(jwt), req);
	}

	@GetMapping
	public List<ApplicationResponse> list(@AuthenticationPrincipal Jwt jwt,
			@RequestParam(required = false) ApplicationStatus status) {
		return applicationService.list(userId(jwt), status);
	}

	@GetMapping("/{id}")
	public ApplicationResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
		return applicationService.get(userId(jwt), id);
	}

	// Spring prefers an exact path over a {variable}, so this wins over /{id} for GET /stats
	@GetMapping("/stats")
	public ApplicationStatsResponse stats(@AuthenticationPrincipal Jwt jwt) {
		return applicationService.stats(userId(jwt));
	}

	@PutMapping("/{id}")
	public ApplicationResponse update(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
			@Valid @RequestBody ApplicationRequest req) {
		return applicationService.update(userId(jwt), id, req);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
		applicationService.delete(userId(jwt), id);
	}

	private Long userId(Jwt jwt) {
		return Long.valueOf(jwt.getSubject());
	}
}
