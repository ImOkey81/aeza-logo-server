package org.aeza.aezaserver.service;

import org.aeza.aezaserver.dto.agents.AgentStatusDto;
import org.aeza.aezaserver.dto.ingest.HeartbeatRequest;
import org.aeza.aezaserver.model.Agent;
import org.aeza.aezaserver.repository.AgentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class AgentService {
    private final AgentRepository agentRepository;
    private final LiveStreamService liveStreamService;

    public AgentService(AgentRepository agentRepository, LiveStreamService liveStreamService) {
        this.agentRepository = agentRepository;
        this.liveStreamService = liveStreamService;
    }

    @Transactional
    public AgentStatusDto updateHeartbeat(HeartbeatRequest request) {
        Agent agent = agentRepository.findById(request.agentId()).orElseGet(Agent::new);
        agent.setAgentId(request.agentId());
        agent.setHost(request.host());
        agent.setStatus(request.status());
        agent.setLastSeen(Instant.now());
        agent.setBufferedCount(request.bufferedCount());

        Agent updated = agentRepository.save(agent);
        AgentStatusDto dto = toDto(updated);
        liveStreamService.publish("agent_status", dto);
        return dto;
    }

    @Transactional(readOnly = true)
    public List<AgentStatusDto> listAgents() {
        return agentRepository.findAllByOrderByLastSeenDesc().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public AgentStatusDto getAgent(String id) {
        return agentRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "agent not found"));
    }

    private AgentStatusDto toDto(Agent agent) {
        return new AgentStatusDto(
                agent.getAgentId(),
                agent.getHost(),
                agent.getStatus(),
                agent.getLastSeen(),
                agent.getBufferedCount()
        );
    }
}
