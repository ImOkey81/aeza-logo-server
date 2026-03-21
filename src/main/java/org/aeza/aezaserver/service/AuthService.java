package org.aeza.aezaserver.service;

import jakarta.annotation.PostConstruct;
import org.aeza.aezaserver.dto.auth.LoginRequest;
import org.aeza.aezaserver.dto.auth.LoginResponse;
import org.aeza.aezaserver.dto.auth.MeResponse;
import org.aeza.aezaserver.model.User;
import org.aeza.aezaserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
public class AuthService {
    private final String adminUsername;
    private final String adminPassword;
    private final long ttlSeconds;
    private final UserRepository userRepository;
    private final Map<String, SessionData> sessions = new ConcurrentHashMap<>();

    public AuthService(
            @Value("${security.admin.username:admin}") String adminUsername,
            @Value("${security.admin.password:admin123}") String adminPassword,
            @Value("${auth.token.ttl-seconds:86400}") long ttlSeconds,
            UserRepository userRepository
    ) {
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
        this.ttlSeconds = ttlSeconds;
        this.userRepository = userRepository;
    }

    @PostConstruct
    @Transactional
    void initAdmin() {
        if (userRepository.findByUsername(adminUsername).isPresent()) {
            return;
        }
        User user = new User();
        user.setUsername(adminUsername);
        user.setPasswordHash(adminPassword);
        user.setRole("ADMIN");
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .filter(found -> found.getPasswordHash().equals(request.password()))
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "invalid credentials"));

        String token = UUID.randomUUID().toString();
        sessions.put(token, new SessionData(user.getId(), Instant.now().plusSeconds(ttlSeconds)));
        return new LoginResponse(token, "Bearer", ttlSeconds, user.getUsername(), user.getRole());
    }

    @Transactional(readOnly = true)
    public MeResponse me(String authHeader) {
        User user = resolveUser(authHeader);
        return new MeResponse(user.getId(), user.getUsername(), user.getRole());
    }

    @Transactional(readOnly = true)
    public User resolveUser(String authHeader) {
        String token = parseBearerToken(authHeader);
        SessionData session = sessions.get(token);
        if (session == null || Instant.now().isAfter(session.expiresAt())) {
            sessions.remove(token);
            throw new ResponseStatusException(UNAUTHORIZED, "invalid token");
        }
        return userRepository.findById(session.userId())
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "user not found"));
    }

    public void requireAuth(String authHeader) {
        resolveUser(authHeader);
    }

    private String parseBearerToken(String authHeader) {
        if (authHeader == null || authHeader.isBlank()) {
            throw new ResponseStatusException(UNAUTHORIZED, "missing authorization header");
        }
        if (!authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(UNAUTHORIZED, "invalid authorization header");
        }
        return authHeader.substring("Bearer ".length());
    }

    private record SessionData(Long userId, Instant expiresAt) {
    }
}
