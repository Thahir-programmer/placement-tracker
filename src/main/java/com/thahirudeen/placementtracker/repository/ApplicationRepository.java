package com.thahirudeen.placementtracker.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thahirudeen.placementtracker.entity.Application;
import com.thahirudeen.placementtracker.entity.ApplicationStatus;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

	// all applications of one user (for the list page)
	List<Application> findByUserId(Long userId);

	// filter by status (e.g. show only INTERVIEW)
	List<Application> findByUserIdAndStatus(Long userId, ApplicationStatus status);

	// dashboard numbers (e.g. how many OFFER)
	long countByUserIdAndStatus(Long userId, ApplicationStatus status);

	// safe lookup: the application must belong to the logged-in user (prevents IDOR)
	Optional<Application> findByIdAndUserId(Long id, Long userId);
}
