package com.thahirudeen.placementtracker.service;

import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.thahirudeen.placementtracker.dto.RegisterRequest;
import com.thahirudeen.placementtracker.entity.User;
import com.thahirudeen.placementtracker.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public String register(RegisterRequest req) {
		// BCrypt's limit is 72 bytes; @Size only counts characters
		if (req.getPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is too long");
		}

		if (userRepository.existsByEmail(req.getEmail())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
		}

		User user = new User();
		user.setName(req.getName());
		user.setEmail(req.getEmail());
		user.setPassword(passwordEncoder.encode(req.getPassword()));
		userRepository.save(user);

		return "User registered successfully";
	}
}
