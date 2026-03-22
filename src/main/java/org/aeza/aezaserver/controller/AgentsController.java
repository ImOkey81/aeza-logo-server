package org.aeza.aezaserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.aeza.aezaserver.dto.agents.AgentCreateRequest;
import org.aeza.aezaserver.dto.agents.AgentStatusDto;
import org.aeza.aezaserver.dto.agents.AgentUpdateRequest;
import org.aeza.aezaserver.service.AgentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/agents")
@Tag(name = "Agents", description = "Agent list and details")
public class AgentsController {
    private final AgentService agentService;

    public AgentsController(AgentService agentService) {
        this.agentService = agentService;
    }

    @GetMapping
    @Operation(summary = "List agents")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Agent list",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            [
                                              {
                                                "agentId": "agent-dev-01",
                                                "name": "Main dev agent",
                                                "host": "agent-vm-01",
                                                "hostIp": "158.160.205.79",
                                                "status": "online",
                                                "lastSeen": "2026-03-21T11:24:32.636001841Z",
                                                "bufferedCount": 0,
                                                "groupId": 1,
                                                "groupName": "production"
                                              }
                                            ]
                                            """
                            )
                    )
            )
    })
    public List<AgentStatusDto> list(@RequestParam(required = false) Long groupId) {
        return agentService.listAgents(groupId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get agent details")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Agent details",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "agentId": "agent-dev-01",
                                              "name": "Main dev agent",
                                              "host": "agent-vm-01",
                                              "hostIp": "158.160.205.79",
                                              "status": "online",
                                              "lastSeen": "2026-03-21T11:24:32.636001841Z",
                                              "bufferedCount": 0,
                                              "groupId": 1,
                                              "groupName": "production"
                                            }
                                            """
                            )
                    )
            )
    })
    public AgentStatusDto details(@PathVariable String id) {
        return agentService.getAgent(id);
    }

    @PostMapping
    @Operation(summary = "Create agent")
    public AgentStatusDto create(@Valid @RequestBody AgentCreateRequest request) {
        return agentService.createAgent(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update agent")
    public AgentStatusDto update(@PathVariable String id, @Valid @RequestBody AgentUpdateRequest request) {
        return agentService.updateAgent(id, request);
    }
}
