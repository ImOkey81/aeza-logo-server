package org.aeza.aezaserver.dto.ingest;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record HeartbeatRequest(
        @NotBlank @Size(max = ValidationLimits.AGENT_ID_MAX) String agentId,
        @JsonProperty("hostName") @NotBlank @Size(max = ValidationLimits.HOST_MAX) String host,
        String hostIp,
        @NotBlank @Size(max = 32) String status,
        @Min(0) int bufferedCount,
        Long lastSequence,
        List<String> watchedSources
) {
}
