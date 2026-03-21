package org.aeza.aezaserver.controller;

import jakarta.validation.Valid;
import org.aeza.aezaserver.dto.agents.AgentStatusDto;
import org.aeza.aezaserver.dto.ingest.HeartbeatRequest;
import org.aeza.aezaserver.dto.ingest.IngestAckResponse;
import org.aeza.aezaserver.dto.ingest.IngestBatchRequest;
import org.aeza.aezaserver.service.AgentService;
import org.aeza.aezaserver.service.IngestService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class IngestController {
    private final IngestService ingestService;
    private final AgentService agentService;

    public IngestController(
            IngestService ingestService,
            AgentService agentService
    ) {
        this.ingestService = ingestService;
        this.agentService = agentService;
    }

    @PostMapping("/ingest/batch")
    public IngestAckResponse ingestBatch(@Valid @RequestBody IngestBatchRequest request) {
        return ingestService.ingest(request);
    }

    @PostMapping("/agents/heartbeat")
    public AgentStatusDto heartbeat(@Valid @RequestBody HeartbeatRequest request) {
        return agentService.updateHeartbeat(request);
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "UP", "dependencies", Map.of("opensearch", "UP", "postgres", "MVP_IN_MEMORY"));
    }
}
