package org.aeza.aezaserver.service;

import org.aeza.aezaserver.dto.agents.AgentGroupCreateRequest;
import org.aeza.aezaserver.dto.agents.AgentGroupDto;
import org.aeza.aezaserver.dto.agents.AgentGroupUpdateRequest;
import org.aeza.aezaserver.dto.agents.AgentStatusDto;
import org.aeza.aezaserver.model.Agent;
import org.aeza.aezaserver.model.AgentGroup;
import org.aeza.aezaserver.repository.AgentGroupRepository;
import org.aeza.aezaserver.repository.AgentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class AgentGroupService {
    private final AgentGroupRepository agentGroupRepository;
    private final AgentRepository agentRepository;
    private final AgentDtoMapper agentDtoMapper;

    public AgentGroupService(
            AgentGroupRepository agentGroupRepository,
            AgentRepository agentRepository,
            AgentDtoMapper agentDtoMapper
    ) {
        this.agentGroupRepository = agentGroupRepository;
        this.agentRepository = agentRepository;
        this.agentDtoMapper = agentDtoMapper;
    }

    @Transactional(readOnly = true)
    public List<AgentGroupDto> listGroups() {
        return agentGroupRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public AgentGroupDto getGroup(Long id) {
        return toDto(requireGroup(id));
    }

    @Transactional
    public AgentGroupDto createGroup(AgentGroupCreateRequest request) {
        ensureUniqueName(request.name(), null);

        AgentGroup group = new AgentGroup();
        group.setName(request.name().trim());
        group.setDescription(trimmedOrNull(request.description()));
        AgentGroup saved = agentGroupRepository.save(group);

        assignAgents(saved, request.agentIds());
        return toDto(saved);
    }

    @Transactional
    public AgentGroupDto updateGroup(Long id, AgentGroupUpdateRequest request) {
        AgentGroup group = requireGroup(id);

        if (hasText(request.name())) {
            ensureUniqueName(request.name(), id);
            group.setName(request.name().trim());
        }
        if (request.description() != null) {
            group.setDescription(trimmedOrNull(request.description()));
        }

        AgentGroup saved = agentGroupRepository.save(group);
        if (request.agentIds() != null) {
            assignAgents(saved, request.agentIds());
            saved.setUpdatedAt(Instant.now());
            saved = agentGroupRepository.save(saved);
        }

        return toDto(saved);
    }

    @Transactional
    public void deleteGroup(Long id) {
        AgentGroup group = requireGroup(id);
        List<Agent> agents = agentRepository.findAllByGroupIdOrderByLastSeenDesc(id);
        for (Agent agent : agents) {
            agent.setGroup(null);
        }
        agentRepository.saveAll(agents);
        agentGroupRepository.delete(group);
    }

    @Transactional(readOnly = true)
    public List<String> resolveAgentIds(Long groupId) {
        if (groupId == null) {
            return null;
        }

        requireGroup(groupId);
        return agentRepository.findAgentIdsByGroupId(groupId);
    }

    private AgentGroupDto toDto(AgentGroup group) {
        List<AgentStatusDto> agents = agentRepository.findAllByGroupIdOrderByLastSeenDesc(group.getId()).stream()
                .map(agentDtoMapper::toStatusDto)
                .toList();
        return new AgentGroupDto(
                group.getId(),
                group.getName(),
                group.getDescription(),
                group.getCreatedAt(),
                group.getUpdatedAt(),
                agents.size(),
                agents
        );
    }

    private void assignAgents(AgentGroup group, List<String> requestedAgentIds) {
        Set<String> desiredAgentIds = requestedAgentIds == null
                ? Set.of()
                : new LinkedHashSet<>(requestedAgentIds.stream().filter(this::hasText).map(String::trim).toList());

        List<Agent> currentAgents = agentRepository.findAllByGroupIdOrderByLastSeenDesc(group.getId());
        for (Agent agent : currentAgents) {
            if (!desiredAgentIds.contains(agent.getAgentId())) {
                agent.setGroup(null);
            }
        }

        List<Agent> desiredAgents = desiredAgentIds.isEmpty()
                ? List.of()
                : agentRepository.findAllByAgentIdInOrderByLastSeenDesc(desiredAgentIds);

        Set<String> foundIds = desiredAgents.stream().map(Agent::getAgentId).collect(java.util.stream.Collectors.toSet());
        List<String> missingIds = desiredAgentIds.stream().filter(id -> !foundIds.contains(id)).toList();
        if (!missingIds.isEmpty()) {
            throw new ResponseStatusException(NOT_FOUND, "agents not found: " + String.join(", ", missingIds));
        }

        for (Agent agent : desiredAgents) {
            agent.setGroup(group);
        }

        agentRepository.saveAll(currentAgents);
        agentRepository.saveAll(desiredAgents);
    }

    private void ensureUniqueName(String rawName, Long currentId) {
        String normalized = rawName.trim();
        agentGroupRepository.findByNameIgnoreCase(normalized)
                .filter(existing -> !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(CONFLICT, "agent group name already exists");
                });
    }

    private AgentGroup requireGroup(Long id) {
        return agentGroupRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "agent group not found"));
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String trimmedOrNull(String value) {
        return hasText(value) ? value.trim() : null;
    }
}
