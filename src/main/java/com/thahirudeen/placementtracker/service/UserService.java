package com.thahirudeen.placementtracker.service;

import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.thahirudeen.placementtracker.dto.LoginRequest;
import com.thahirudeen.placementtracker.dto.LoginResponse;
import com.thahirudeen.placementtracker.dto.RegisterRequest;
import com.thahirudeen.placementtracker.dto.UserResponse;
import com.thahirudeen.placementtracker.entity.User;
import com.thahirudeen.placementtracker.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

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

	public LoginResponse login(LoginRequest req) {
		// Same error for both failures, so nobody can use login to find out which emails exist
		User user = userRepository.findByEmail(req.getEmail())
				.filter(found -> passwordEncoder.matches(req.getPassword(), found.getPassword()))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

		return new LoginResponse(jwtService.generateToken(user));
	}

	public UserResponse getUserById(Long id) {
		// A valid token can outlive its user, for example after the account is deleted
		User user = userRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User no longer exists"));

		return new UserResponse(user.getId(), user.getName(), user.getEmail());
	}
}
