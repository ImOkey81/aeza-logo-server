package org.aeza.aezaserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.aeza.aezaserver.dto.alerts.AlertHistoryItemDto;
import org.aeza.aezaserver.dto.alerts.AlertRuleCreateRequest;
import org.aeza.aezaserver.dto.alerts.AlertRuleResponseDto;
import org.aeza.aezaserver.service.AlertRuleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/alerts")
@Tag(name = "Alerts", description = "Alert rules and alert history")
public class AlertsController {
    private final AlertRuleService alertRuleService;

    public AlertsController(AlertRuleService alertRuleService) {
        this.alertRuleService = alertRuleService;
    }

    @GetMapping("/rules")
    @Operation(summary = "List alert rules")
    public List<AlertRuleResponseDto> rules() {
        return alertRuleService.findAll();
    }

    @PostMapping("/rules")
    @Operation(summary = "Create alert rule")
    public AlertRuleResponseDto create(@Valid @RequestBody AlertRuleCreateRequest request) {
        return alertRuleService.create(request);
    }

    @PutMapping("/rules/{id}")
    @Operation(summary = "Update alert rule")
    public AlertRuleResponseDto update(
            @PathVariable Long id,
            @Valid @RequestBody AlertRuleCreateRequest request
    ) {
        return alertRuleService.update(id, request);
    }

    @DeleteMapping("/rules/{id}")
    @Operation(summary = "Delete alert rule")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        alertRuleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/history")
    @Operation(summary = "List alert history")
    public List<AlertHistoryItemDto> history() {
        return alertRuleService.history();
    }
}
