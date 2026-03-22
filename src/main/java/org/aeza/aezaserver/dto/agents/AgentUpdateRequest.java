package org.aeza.aezaserver.dto.agents;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record AgentUpdateRequest(
        @Size(max = 255)
        String name,

        @Size(max = 255)
        String host,

        @Size(max = 255)
        String hostIp,

        @Size(max = 32)
        String status,

        @Min(0)
        Integer bufferedCount,

        Long groupId,

        Boolean clearGroup
) {
}
