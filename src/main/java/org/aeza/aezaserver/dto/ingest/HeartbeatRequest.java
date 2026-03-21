package org.aeza.aezaserver.dto.ingest;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record HeartbeatRequest(
        @NotBlank String agentId,
        @NotBlank String host,
        @NotBlank String status,
        @Min(0) int bufferedCount
) {
}
