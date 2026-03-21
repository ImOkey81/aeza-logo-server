package org.aeza.aezaserver.dto.alerts;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record AlertRuleCreateRequest(
        @NotBlank String name,
        boolean enabled,
        @NotBlank String conditionType,
        String query,
        @Min(1) long threshold,
        @Min(10) long windowSeconds,
        @Min(0) long cooldownSeconds,
        @NotBlank String channel,
        String level
) {
}
