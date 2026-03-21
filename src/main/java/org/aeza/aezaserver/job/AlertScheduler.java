package org.aeza.aezaserver.job;

import org.aeza.aezaserver.service.AlertEvaluationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AlertScheduler {
    private final AlertEvaluationService alertEvaluationService;

    public AlertScheduler(AlertEvaluationService alertEvaluationService) {
        this.alertEvaluationService = alertEvaluationService;
    }

    @Scheduled(fixedDelayString = "${alert.scheduler.interval-ms:30000}")
    public void run() {
        alertEvaluationService.evaluateAll();
    }
}
