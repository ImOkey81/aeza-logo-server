package org.aeza.aezaserver.controller;

import jakarta.validation.Valid;
import org.aeza.aezaserver.dto.auth.LoginRequest;
import org.aeza.aezaserver.dto.auth.LoginResponse;
import org.aeza.aezaserver.dto.auth.MeResponse;
import org.aeza.aezaserver.service.AuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/auth/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public MeResponse me(@RequestHeader(name = "Authorization", required = false) String authorization) {
        return authService.me(authorization);
    }
}
