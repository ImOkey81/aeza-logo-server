package org.aeza.aezaserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.aeza.aezaserver.dto.agents.AgentGroupCreateRequest;
import org.aeza.aezaserver.dto.agents.AgentGroupDto;
import org.aeza.aezaserver.dto.agents.AgentGroupUpdateRequest;
import org.aeza.aezaserver.service.AgentGroupService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("/api/v1/agent-groups")
@Tag(name = "Agent Groups", description = "Manage logical groups of agents")
public class AgentGroupsController {
    private final AgentGroupService agentGroupService;

    public AgentGroupsController(AgentGroupService agentGroupService) {
        this.agentGroupService = agentGroupService;
    }

    @GetMapping
    @Operation(summary = "List agent groups")
    public List<AgentGroupDto> list() {
        return agentGroupService.listGroups();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get agent group details")
    public AgentGroupDto details(@PathVariable Long id) {
        return agentGroupService.getGroup(id);
    }

    @PostMapping
    @Operation(summary = "Create agent group")
    public AgentGroupDto create(@Valid @RequestBody AgentGroupCreateRequest request) {
        return agentGroupService.createGroup(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update agent group")
    public AgentGroupDto update(@PathVariable Long id, @Valid @RequestBody AgentGroupUpdateRequest request) {
        return agentGroupService.updateGroup(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(NO_CONTENT)
    @Operation(summary = "Delete agent group")
    public void delete(@PathVariable Long id) {
        agentGroupService.deleteGroup(id);
    }
}
