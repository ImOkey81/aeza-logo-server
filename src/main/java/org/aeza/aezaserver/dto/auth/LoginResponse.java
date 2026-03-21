package org.aeza.aezaserver.dto.auth;

public record LoginResponse(String token, String tokenType, long expiresInSeconds, String username, String role) {
}
