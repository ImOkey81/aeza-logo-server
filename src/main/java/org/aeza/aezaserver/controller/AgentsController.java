package org.aeza.aezaserver.controller;

import org.aeza.aezaserver.dto.agents.AgentStatusDto;
import org.aeza.aezaserver.service.AgentService;
import org.aeza.aezaserver.service.AuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/agents")
public class AgentsController {
    private final AgentService agentService;
    private final AuthService authService;

    public AgentsController(AgentService agentService, AuthService authService) {
        this.agentService = agentService;
        this.authService = authService;
    }

    @GetMapping
    public List<AgentStatusDto> list(@RequestHeader(name = "Authorization", required = false) String authorization) {
        authService.requireAuth(authorization);
        return agentService.listAgents();
    }

    @GetMapping("/{id}")
    public AgentStatusDto details(
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @PathVariable String id
    ) {
        authService.requireAuth(authorization);
        return agentService.getAgent(id);
    }
}
