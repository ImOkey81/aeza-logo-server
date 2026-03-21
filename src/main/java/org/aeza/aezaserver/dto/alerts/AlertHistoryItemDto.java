package org.aeza.aezaserver.dto.alerts;

import java.time.Instant;

public record AlertHistoryItemDto(
        Long id,
        Long ruleId,
        String ruleName,
        Instant triggeredAt,
        String message,
        String channel,
        long value,
        long threshold
) {
}
