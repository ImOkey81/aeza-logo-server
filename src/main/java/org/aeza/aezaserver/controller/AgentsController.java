package org.aeza.aezaserver.controller;

import org.aeza.aezaserver.dto.agents.AgentStatusDto;
import org.aeza.aezaserver.service.AgentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/agents")
public class AgentsController {
    private final AgentService agentService;

    public AgentsController(AgentService agentService) {
        this.agentService = agentService;
    }

    @GetMapping
    public List<AgentStatusDto> list() {
        return agentService.listAgents();
    }

    @GetMapping("/{id}")
    public AgentStatusDto details(@PathVariable String id) {
        return agentService.getAgent(id);
    }
}
