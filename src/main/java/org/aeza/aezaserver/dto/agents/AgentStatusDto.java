package org.aeza.aezaserver.dto.agents;

import java.time.Instant;

public record AgentStatusDto(
        String agentId,
        String host,
        String status,
        Instant lastSeen,
        int bufferedCount
) {
}
