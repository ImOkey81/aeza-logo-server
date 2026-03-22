package org.aeza.aezaserver.dto.agents;

import java.time.Instant;
import java.util.List;

public record AgentGroupDto(
        Long id,
        String name,
        String description,
        Instant createdAt,
        Instant updatedAt,
        long agentCount,
        List<AgentStatusDto> agents
) {
}
