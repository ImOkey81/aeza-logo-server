package org.aeza.aezaserver.service;

import org.aeza.aezaserver.dto.agents.AgentStatusDto;
import org.aeza.aezaserver.model.Agent;
import org.aeza.aezaserver.model.AgentGroup;
import org.springframework.stereotype.Component;

@Component
public class AgentDtoMapper {

    public AgentStatusDto toStatusDto(Agent agent) {
        AgentGroup group = agent.getGroup();
        return new AgentStatusDto(
                agent.getAgentId(),
                hasText(agent.getDisplayName()) ? agent.getDisplayName() : agent.getAgentId(),
                agent.getHost(),
                agent.getHostIp(),
                agent.getStatus(),
                agent.getLastSeen(),
                agent.getBufferedCount(),
                group != null ? group.getId() : null,
                group != null ? group.getName() : null
        );
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
