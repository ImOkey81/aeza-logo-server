package org.aeza.aezaserver.dto.alerts;

import java.time.Instant;

public record AlertRuleResponseDto(
        Long id,
        String name,
        boolean enabled,
        String conditionType,
        String query,
        long threshold,
        long windowSeconds,
        long cooldownSeconds,
        String channel,
        String level,
        Instant createdAt,
        Instant updatedAt,
        Instant lastTriggeredAt
) {
}
