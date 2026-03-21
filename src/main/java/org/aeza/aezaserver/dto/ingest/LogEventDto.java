package org.aeza.aezaserver.dto.ingest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record LogEventDto(
        @NotNull Instant timestamp,
        @NotBlank String level,
        @NotBlank String message,
        @NotBlank String host,
        @NotBlank String service,
        String sourceType,
        String sourcePath,
        List<String> tags,
        Map<String, Object> metadata,
        String agentId
) {
}
