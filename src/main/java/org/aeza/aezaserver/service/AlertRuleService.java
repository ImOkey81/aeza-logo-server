package org.aeza.aezaserver.service;

import org.aeza.aezaserver.dto.alerts.AlertHistoryItemDto;
import org.aeza.aezaserver.dto.alerts.AlertRuleCreateRequest;
import org.aeza.aezaserver.dto.alerts.AlertRuleResponseDto;
import org.aeza.aezaserver.model.AlertRule;
import org.aeza.aezaserver.repository.AlertEventRepository;
import org.aeza.aezaserver.repository.AlertRuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class AlertRuleService {
    private static final Set<String> SUPPORTED_TYPES = Set.of("count_gt", "contains_pattern", "error_rate_gt");

    private final AlertRuleRepository alertRuleRepository;
    private final AlertEventRepository alertEventRepository;

    public AlertRuleService(AlertRuleRepository alertRuleRepository, AlertEventRepository alertEventRepository) {
        this.alertRuleRepository = alertRuleRepository;
        this.alertEventRepository = alertEventRepository;
    }

    @Transactional(readOnly = true)
    public List<AlertRuleResponseDto> findAll() {
        return alertRuleRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional
    public AlertRuleResponseDto create(AlertRuleCreateRequest request) {
        validate(request);
        Instant now = Instant.now();

        AlertRule rule = new AlertRule();
        rule.setName(request.name());
        rule.setEnabled(request.enabled());
        rule.setConditionType(request.conditionType());
        rule.setQuery(request.query());
        rule.setThreshold(request.threshold());
        rule.setWindowSeconds(request.windowSeconds());
        rule.setCooldownSeconds(request.cooldownSeconds());
        rule.setChannel(request.channel());
        rule.setLevel(request.level());
        rule.setCreatedAt(now);
        rule.setUpdatedAt(now);

        return toDto(alertRuleRepository.save(rule));
    }

    @Transactional
    public AlertRuleResponseDto update(Long id, AlertRuleCreateRequest request) {
        validate(request);
        AlertRule rule = alertRuleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "rule not found"));

        rule.setName(request.name());
        rule.setEnabled(request.enabled());
        rule.setConditionType(request.conditionType());
        rule.setQuery(request.query());
        rule.setThreshold(request.threshold());
        rule.setWindowSeconds(request.windowSeconds());
        rule.setCooldownSeconds(request.cooldownSeconds());
        rule.setChannel(request.channel());
        rule.setLevel(request.level());
        rule.setUpdatedAt(Instant.now());

        return toDto(alertRuleRepository.save(rule));
    }

    @Transactional
    public void delete(Long id) {
        if (!alertRuleRepository.existsById(id)) {
            throw new ResponseStatusException(NOT_FOUND, "rule not found");
        }
        alertRuleRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<AlertHistoryItemDto> history() {
        return alertEventRepository.findAllByOrderByTriggeredAtDesc().stream()
                .map(event -> new AlertHistoryItemDto(
                        event.getId(),
                        event.getRuleId(),
                        event.getRuleName(),
                        event.getTriggeredAt(),
                        event.getMessage(),
                        event.getChannel(),
                        event.getValue(),
                        event.getThreshold()
                ))
                .toList();
    }

    private AlertRuleResponseDto toDto(AlertRule rule) {
        return new AlertRuleResponseDto(
                rule.getId(),
                rule.getName(),
                rule.isEnabled(),
                rule.getConditionType(),
                rule.getQuery(),
                rule.getThreshold(),
                rule.getWindowSeconds(),
                rule.getCooldownSeconds(),
                rule.getChannel(),
                rule.getLevel(),
                rule.getCreatedAt(),
                rule.getUpdatedAt(),
                rule.getLastTriggeredAt()
        );
    }

    private void validate(AlertRuleCreateRequest request) {
        if (!SUPPORTED_TYPES.contains(request.conditionType())) {
            throw new ResponseStatusException(BAD_REQUEST, "unsupported conditionType");
        }
    }
}
