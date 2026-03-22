package org.aeza.aezaserver.dto.ingest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record LogEventDto(
        @NotNull Instant timestamp,
        @NotBlank @Size(max = ValidationLimits.LEVEL_MAX) String level,
        @NotBlank String message,
        @NotBlank @Size(max = ValidationLimits.HOST_MAX) String host,
        @NotBlank @Size(max = ValidationLimits.SERVICE_MAX) String service,
        @Size(max = ValidationLimits.SOURCE_TYPE_MAX) String sourceType,
        @Size(max = ValidationLimits.SOURCE_PATH_MAX) String sourcePath,
        List<String> tags,
        Map<String, Object> metadata,
        @Size(max = ValidationLimits.AGENT_ID_MAX) String agentId
) {
}
