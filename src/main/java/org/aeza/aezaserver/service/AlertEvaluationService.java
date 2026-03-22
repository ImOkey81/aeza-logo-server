package org.aeza.aezaserver.service;

import org.aeza.aezaserver.dto.dashboard.DashboardSummaryDto;
import org.aeza.aezaserver.integration.TelegramClient;
import org.aeza.aezaserver.integration.WebhookClient;
import org.aeza.aezaserver.model.AlertEvent;
import org.aeza.aezaserver.model.AlertRule;
import org.aeza.aezaserver.repository.AlertEventRepository;
import org.aeza.aezaserver.repository.AlertRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Service
public class AlertEvaluationService {
    private static final Logger log = LoggerFactory.getLogger(AlertEvaluationService.class);

    private final AlertRuleRepository alertRuleRepository;
    private final AlertEventRepository alertEventRepository;
    private final DashboardService dashboardService;
    private final SearchService searchService;
    private final TelegramClient telegramClient;
    private final WebhookClient webhookClient;
    private final LiveStreamService liveStreamService;

    public AlertEvaluationService(
            AlertRuleRepository alertRuleRepository,
            AlertEventRepository alertEventRepository,
            DashboardService dashboardService,
            SearchService searchService,
            TelegramClient telegramClient,
            WebhookClient webhookClient,
            LiveStreamService liveStreamService
    ) {
        this.alertRuleRepository = alertRuleRepository;
        this.alertEventRepository = alertEventRepository;
        this.dashboardService = dashboardService;
        this.searchService = searchService;
        this.telegramClient = telegramClient;
        this.webhookClient = webhookClient;
        this.liveStreamService = liveStreamService;
    }

    @Transactional
    public void evaluateAll() {
        List<AlertRule> rules = alertRuleRepository.findByEnabledTrueOrderByIdAsc();
        log.info("Evaluating {} enabled alert rules", rules.size());
        for (AlertRule rule : rules) {
            evaluateRule(rule);
        }
    }

    private void evaluateRule(AlertRule rule) {
        Instant now = Instant.now();
        if (rule.getLastTriggeredAt() != null && now.isBefore(rule.getLastTriggeredAt().plusSeconds(rule.getCooldownSeconds()))) {
            log.info("Skipping rule '{}' because cooldown is active", rule.getName());
            return;
        }

        Instant from = now.minusSeconds(rule.getWindowSeconds());
        long observed;
        boolean triggered;

        switch (rule.getConditionType()) {
            case "count_gt", "contains_pattern" -> {
                observed = searchService.search(rule.getQuery(), null, null, rule.getLevel(), from, now, 0, 1).total();
                triggered = observed > rule.getThreshold();
            }
            case "error_rate_gt" -> {
                DashboardSummaryDto summary = dashboardService.summary(from, now, null);
                observed = summary.totalLogs() == 0 ? 0 : Math.round((summary.errors() * 100.0) / summary.totalLogs());
                triggered = observed > rule.getThreshold();
            }
            default -> {
                log.warn("Skipping rule '{}' with unsupported conditionType '{}'", rule.getName(), rule.getConditionType());
                return;
            }
        }

        log.info(
                "Rule '{}' evaluated: observed={}, threshold={}, triggered={}, channel={}",
                rule.getName(),
                observed,
                rule.getThreshold(),
                triggered,
                rule.getChannel()
        );

        if (!triggered) {
            return;
        }

        String message = String.format(
                Locale.ROOT,
                "Alert '%s' triggered: value=%d threshold=%d window=%ds",
                rule.getName(),
                observed,
                rule.getThreshold(),
                rule.getWindowSeconds()
        );

        AlertEvent event = new AlertEvent();
        event.setRuleId(rule.getId());
        event.setRuleName(rule.getName());
        event.setTriggeredAt(now);
        event.setMessage(message);
        event.setChannel(rule.getChannel());
        event.setValue(observed);
        event.setThreshold(rule.getThreshold());
        AlertEvent savedEvent = alertEventRepository.save(event);
        log.info("Rule '{}' triggered, alert event id={}", rule.getName(), savedEvent.getId());

        rule.setLastTriggeredAt(now);
        rule.setUpdatedAt(Instant.now());
        alertRuleRepository.save(rule);

        if ("telegram".equalsIgnoreCase(rule.getChannel())) {
            telegramClient.send(message);
        }
        if ("webhook".equalsIgnoreCase(rule.getChannel())) {
            webhookClient.send("alert_triggered", message);
        }

        liveStreamService.publish("alert_triggered", savedEvent);
    }
}
