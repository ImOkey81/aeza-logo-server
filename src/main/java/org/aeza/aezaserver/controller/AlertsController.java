package org.aeza.aezaserver.controller;

import jakarta.validation.Valid;
import org.aeza.aezaserver.dto.alerts.AlertHistoryItemDto;
import org.aeza.aezaserver.dto.alerts.AlertRuleCreateRequest;
import org.aeza.aezaserver.dto.alerts.AlertRuleResponseDto;
import org.aeza.aezaserver.service.AlertRuleService;
import org.aeza.aezaserver.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/alerts")
public class AlertsController {
    private final AlertRuleService alertRuleService;
    private final AuthService authService;

    public AlertsController(AlertRuleService alertRuleService, AuthService authService) {
        this.alertRuleService = alertRuleService;
        this.authService = authService;
    }

    @GetMapping("/rules")
    public List<AlertRuleResponseDto> rules(@RequestHeader(name = "Authorization", required = false) String authorization) {
        authService.requireAuth(authorization);
        return alertRuleService.findAll();
    }

    @PostMapping("/rules")
    public AlertRuleResponseDto create(
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @Valid @RequestBody AlertRuleCreateRequest request
    ) {
        authService.requireAuth(authorization);
        return alertRuleService.create(request);
    }

    @PutMapping("/rules/{id}")
    public AlertRuleResponseDto update(
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @PathVariable Long id,
            @Valid @RequestBody AlertRuleCreateRequest request
    ) {
        authService.requireAuth(authorization);
        return alertRuleService.update(id, request);
    }

    @DeleteMapping("/rules/{id}")
    public ResponseEntity<Void> delete(
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @PathVariable Long id
    ) {
        authService.requireAuth(authorization);
        alertRuleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/history")
    public List<AlertHistoryItemDto> history(@RequestHeader(name = "Authorization", required = false) String authorization) {
        authService.requireAuth(authorization);
        return alertRuleService.history();
    }
}
