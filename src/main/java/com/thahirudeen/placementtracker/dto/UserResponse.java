package com.thahirudeen.placementtracker.dto;

// Deliberately has no password field, so it can never be sent to the client
public record UserResponse(Long id, String name, String email) {
}
