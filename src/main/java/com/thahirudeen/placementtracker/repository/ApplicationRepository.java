package com.thahirudeen.placementtracker.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.thahirudeen.placementtracker.entity.Application;
import com.thahirudeen.placementtracker.entity.ApplicationStatus;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

	// Both list queries: newest applied date first, no date last, newest id breaks ties.
	// Written by hand because a method name can't say "nulls last", and Postgres would
	// otherwise put missing dates first when sorting newest first.

	// all applications of one user (for the list page)
	@Query("""
			select a from Application a
			where a.user.id = :userId
			order by a.appliedDate desc nulls last, a.id desc
			""")
	List<Application> findByUserId(@Param("userId") Long userId);

	// filter by status (e.g. show only INTERVIEW)
	@Query("""
			select a from Application a
			where a.user.id = :userId and a.status = :status
			order by a.appliedDate desc nulls last, a.id desc
			""")
	List<Application> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") ApplicationStatus status);

	// dashboard numbers (e.g. how many OFFER)
	long countByUserIdAndStatus(Long userId, ApplicationStatus status);

	// safe lookup: the application must belong to the logged-in user (prevents IDOR)
	Optional<Application> findByIdAndUserId(Long id, Long userId);
}
