package com.thahirudeen.placementtracker.service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.thahirudeen.placementtracker.dto.ApplicationRequest;
import com.thahirudeen.placementtracker.dto.ApplicationResponse;
import com.thahirudeen.placementtracker.dto.ApplicationStatsResponse;
import com.thahirudeen.placementtracker.entity.Application;
import com.thahirudeen.placementtracker.entity.ApplicationStatus;
import com.thahirudeen.placementtracker.entity.User;
import com.thahirudeen.placementtracker.repository.ApplicationRepository;
import com.thahirudeen.placementtracker.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApplicationService {

	private final ApplicationRepository applicationRepository;
	private final UserRepository userRepository;

	public ApplicationResponse create(Long userId, ApplicationRequest req) {
		// A valid token can outlive its user, for example after the account is deleted
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User no longer exists"));

		Application application = new Application();
		application.setUser(user);
		copyFields(req, application);
		application.setStatus(req.getStatus() != null ? req.getStatus() : ApplicationStatus.APPLIED);

		return ApplicationResponse.from(applicationRepository.save(application));
	}

	public List<ApplicationResponse> list(Long userId, ApplicationStatus status) {
		List<Application> applications = (status == null)
				? applicationRepository.findByUserId(userId)
				: applicationRepository.findByUserIdAndStatus(userId, status);

		return applications.stream().map(ApplicationResponse::from).toList();
	}

	public ApplicationStatsResponse stats(Long userId) {
		// Loops over the enum so every status appears, including those with 0 applications
		Map<ApplicationStatus, Long> byStatus = new EnumMap<>(ApplicationStatus.class);
		long total = 0;
		for (ApplicationStatus status : ApplicationStatus.values()) {
			long count = applicationRepository.countByUserIdAndStatus(userId, status);
			byStatus.put(status, count);
			total += count;
		}

		return new ApplicationStatsResponse(total, byStatus);
	}

	public ApplicationResponse get(Long userId, Long id) {
		return ApplicationResponse.from(findOwned(userId, id));
	}

	public ApplicationResponse update(Long userId, Long id, ApplicationRequest req) {
		Application application = findOwned(userId, id);
		copyFields(req, application);
		if (req.getStatus() != null) {
			application.setStatus(req.getStatus());
		}

		return ApplicationResponse.from(applicationRepository.save(application));
	}

	public void delete(Long userId, Long id) {
		applicationRepository.delete(findOwned(userId, id));
	}

	// Prevents IDOR. Someone else's application gives the same 404 as one that doesn't exist,
	// so a caller can't even tell whether that id is in use.
	private Application findOwned(Long userId, Long id) {
		return applicationRepository.findByIdAndUserId(id, userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));
	}

	private void copyFields(ApplicationRequest req, Application application) {
		application.setCompanyName(req.getCompanyName());
		application.setRole(req.getRole());
		application.setPackageLpa(req.getPackageLpa());
		application.setAppliedDate(req.getAppliedDate());
		application.setJobLink(req.getJobLink());
		application.setNotes(req.getNotes());
	}
}
