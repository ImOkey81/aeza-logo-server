package org.aeza.aezaserver.dto.agents;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.aeza.aezaserver.dto.ingest.ValidationLimits;

public record AgentCreateRequest(
        @NotBlank
        @Size(max = ValidationLimits.AGENT_ID_MAX)
        String agentId,

        @Size(max = 255)
        String name,

        @NotBlank
        @Size(max = 255)
        String host,

        @Size(max = 255)
        String hostIp,

        @Size(max = 32)
        String status,

        @Min(0)
        Integer bufferedCount,

        Long groupId
) {
}
