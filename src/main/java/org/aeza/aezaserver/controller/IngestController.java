package org.aeza.aezaserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.aeza.aezaserver.config.OpenSearchProperties;
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
@Tag(name = "Ingest", description = "Log ingest, heartbeat and health endpoints")
public class IngestController {
    private final IngestService ingestService;
    private final AgentService agentService;
    private final OpenSearchProperties openSearchProperties;

    public IngestController(
            IngestService ingestService,
            AgentService agentService,
            OpenSearchProperties openSearchProperties
    ) {
        this.ingestService = ingestService;
        this.agentService = agentService;
        this.openSearchProperties = openSearchProperties;
    }

    @PostMapping("/ingest/batch")
    @Operation(summary = "Ingest batch of logs", description = "Accepts one agent id and a batch of log events.")
    public IngestAckResponse ingestBatch(@Valid @RequestBody IngestBatchRequest request) {
        return ingestService.ingest(request);
    }

    @PostMapping("/agents/heartbeat")
    @Operation(summary = "Update agent heartbeat", description = "Creates or updates agent status and last seen timestamp.")
    public AgentStatusDto heartbeat(@Valid @RequestBody HeartbeatRequest request) {
        return agentService.updateHeartbeat(request);
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Returns backend status and dependency summary.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Application health",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "status": "UP",
                                              "dependencies": {
                                                "opensearch": "UP",
                                                "postgres": "UP"
                                              }
                                            }
                                            """
                            )
                    )
            )
    })
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "dependencies", Map.of(
                        "opensearch", openSearchProperties.enabled() ? "UP" : "DISABLED",
                        "postgres", "UP"
                )
        );
    }
}
