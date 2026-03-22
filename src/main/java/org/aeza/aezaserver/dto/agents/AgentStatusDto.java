package org.aeza.aezaserver.dto.agents;

import java.time.Instant;

public record AgentStatusDto(
        String agentId,
        String name,
        String host,
        String hostIp,
        String status,
        Instant lastSeen,
        int bufferedCount,
        Long groupId,
        String groupName
) {
}
