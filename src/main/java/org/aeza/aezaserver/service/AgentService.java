package org.aeza.aezaserver.service;

import org.aeza.aezaserver.dto.agents.AgentStatusDto;
import org.aeza.aezaserver.dto.agents.AgentCreateRequest;
import org.aeza.aezaserver.dto.agents.AgentUpdateRequest;
import org.aeza.aezaserver.dto.ingest.HeartbeatRequest;
import org.aeza.aezaserver.model.Agent;
import org.aeza.aezaserver.model.AgentGroup;
import org.aeza.aezaserver.repository.AgentGroupRepository;
import org.aeza.aezaserver.repository.AgentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class AgentService {
    private final AgentRepository agentRepository;
    private final AgentGroupRepository agentGroupRepository;
    private final AgentDtoMapper agentDtoMapper;
    private final LiveStreamService liveStreamService;

    public AgentService(
            AgentRepository agentRepository,
            AgentGroupRepository agentGroupRepository,
            AgentDtoMapper agentDtoMapper,
            LiveStreamService liveStreamService
    ) {
        this.agentRepository = agentRepository;
        this.agentGroupRepository = agentGroupRepository;
        this.agentDtoMapper = agentDtoMapper;
        this.liveStreamService = liveStreamService;
    }

    @Transactional
    public AgentStatusDto updateHeartbeat(HeartbeatRequest request) {
        Agent agent = agentRepository.findById(request.agentId()).orElseGet(Agent::new);
        agent.setAgentId(request.agentId());
        if (hasText(request.agentName())) {
            agent.setDisplayName(request.agentName().trim());
        } else if (!hasText(agent.getDisplayName())) {
            agent.setDisplayName(request.agentId());
        }
        agent.setHost(request.host());
        if (hasText(request.hostIp())) {
            agent.setHostIp(request.hostIp().trim());
        }
        agent.setStatus(request.status());
        agent.setLastSeen(Instant.now());
        agent.setBufferedCount(request.bufferedCount());

        Agent updated = agentRepository.save(agent);
        AgentStatusDto dto = agentDtoMapper.toStatusDto(updated);
        liveStreamService.publish("agent_status", dto);
        return dto;
    }

    @Transactional(readOnly = true)
    public List<AgentStatusDto> listAgents(Long groupId) {
        List<Agent> agents = groupId == null
                ? agentRepository.findAllByOrderByLastSeenDesc()
                : agentRepository.findAllByGroupIdOrderByLastSeenDesc(requireGroup(groupId).getId());
        return agents.stream().map(agentDtoMapper::toStatusDto).toList();
    }

    @Transactional(readOnly = true)
    public AgentStatusDto getAgent(String id) {
        return agentRepository.findById(id)
                .map(agentDtoMapper::toStatusDto)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "agent not found"));
    }

    @Transactional
    public AgentStatusDto createAgent(AgentCreateRequest request) {
        if (agentRepository.existsById(request.agentId())) {
            throw new ResponseStatusException(CONFLICT, "agent already exists");
        }

        Agent agent = new Agent();
        agent.setAgentId(request.agentId());
        agent.setDisplayName(hasText(request.name()) ? request.name().trim() : request.agentId());
        agent.setHost(request.host().trim());
        agent.setHostIp(trimmedOrNull(request.hostIp()));
        agent.setStatus(hasText(request.status()) ? request.status().trim() : "offline");
        agent.setLastSeen(Instant.now());
        agent.setBufferedCount(request.bufferedCount() != null ? request.bufferedCount() : 0);
        agent.setGroup(request.groupId() == null ? null : requireGroup(request.groupId()));

        return agentDtoMapper.toStatusDto(agentRepository.save(agent));
    }

    @Transactional
    public AgentStatusDto updateAgent(String id, AgentUpdateRequest request) {
        Agent agent = agentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "agent not found"));

        if (hasText(request.name())) {
            agent.setDisplayName(request.name().trim());
        }
        if (hasText(request.host())) {
            agent.setHost(request.host().trim());
        }
        if (request.hostIp() != null) {
            agent.setHostIp(trimmedOrNull(request.hostIp()));
        }
        if (hasText(request.status())) {
            agent.setStatus(request.status().trim());
        }
        if (request.bufferedCount() != null) {
            agent.setBufferedCount(request.bufferedCount());
        }
        if (Boolean.TRUE.equals(request.clearGroup())) {
            agent.setGroup(null);
        } else if (request.groupId() != null) {
            agent.setGroup(requireGroup(request.groupId()));
        }

        return agentDtoMapper.toStatusDto(agentRepository.save(agent));
    }

    private AgentGroup requireGroup(Long groupId) {
        return agentGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "agent group not found"));
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String trimmedOrNull(String value) {
        return hasText(value) ? value.trim() : null;
    }
}
