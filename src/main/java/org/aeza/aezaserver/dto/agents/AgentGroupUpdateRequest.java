package org.aeza.aezaserver.dto.agents;

import jakarta.validation.constraints.Size;
import org.aeza.aezaserver.dto.ingest.ValidationLimits;

import java.util.List;

public record AgentGroupUpdateRequest(
        @Size(max = 255)
        String name,

        @Size(max = 1024)
        String description,

        List<@Size(max = ValidationLimits.AGENT_ID_MAX) String> agentIds
) {
}
