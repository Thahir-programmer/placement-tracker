package com.thahirudeen.placementtracker.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.thahirudeen.placementtracker.dto.LoginRequest;
import com.thahirudeen.placementtracker.dto.LoginResponse;
import com.thahirudeen.placementtracker.dto.RegisterRequest;
import com.thahirudeen.placementtracker.dto.UserResponse;
import com.thahirudeen.placementtracker.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final UserService userService;

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	public String register(@Valid @RequestBody RegisterRequest req) {
		return userService.register(req);
	}

	@PostMapping("/login")
	public LoginResponse login(@Valid @RequestBody LoginRequest req) {
		return userService.login(req);
	}

	@GetMapping("/me")
	public UserResponse me(@AuthenticationPrincipal Jwt jwt) {
		return userService.getUserById(Long.valueOf(jwt.getSubject()));
	}
}
