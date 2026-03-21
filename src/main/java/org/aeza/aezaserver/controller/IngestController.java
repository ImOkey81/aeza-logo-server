package org.aeza.aezaserver.controller;

import jakarta.validation.Valid;
import org.aeza.aezaserver.dto.agents.AgentStatusDto;
import org.aeza.aezaserver.dto.ingest.HeartbeatRequest;
import org.aeza.aezaserver.dto.ingest.IngestAckResponse;
import org.aeza.aezaserver.dto.ingest.IngestBatchRequest;
import org.aeza.aezaserver.service.AgentService;
import org.aeza.aezaserver.service.IngestService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/api/v1")
public class IngestController {
    private final IngestService ingestService;
    private final AgentService agentService;
    private final String sharedToken;

    public IngestController(
            IngestService ingestService,
            AgentService agentService,
            @Value("${agent.shared-token}") String sharedToken
    ) {
        this.ingestService = ingestService;
        this.agentService = agentService;
        this.sharedToken = sharedToken;
    }

    @PostMapping("/ingest/batch")
    public IngestAckResponse ingestBatch(
            @RequestHeader(name = "X-Agent-Token", required = false) String token,
            @Valid @RequestBody IngestBatchRequest request
    ) {
        verifyAgentToken(token);
        return ingestService.ingest(request);
    }

    @PostMapping("/agents/heartbeat")
    public AgentStatusDto heartbeat(
            @RequestHeader(name = "X-Agent-Token", required = false) String token,
            @Valid @RequestBody HeartbeatRequest request
    ) {
        verifyAgentToken(token);
        return agentService.updateHeartbeat(request);
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "UP", "dependencies", Map.of("opensearch", "UP", "postgres", "MVP_IN_MEMORY"));
    }

    private void verifyAgentToken(String token) {
        if (token == null || !token.equals(sharedToken)) {
            throw new ResponseStatusException(UNAUTHORIZED, "invalid agent token");
        }
    }
}
