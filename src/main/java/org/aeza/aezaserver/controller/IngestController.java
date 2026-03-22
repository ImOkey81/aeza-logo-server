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
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Batch of logs from agent",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "agentId": "agent-dev-01",
                                      "sequence": 1774104589716,
                                      "events": [
                                        {
                                          "timestamp": "2026-03-21T11:40:00Z",
                                          "level": "ERROR",
                                          "message": "database timeout",
                                          "host": "agent-vm-01",
                                          "service": "nginx",
                                          "sourceType": "file",
                                          "sourcePath": "/var/log/nginx/error.log",
                                          "tags": ["prod", "agent"],
                                          "metadata": {
                                            "env": "prod",
                                            "component": "collector"
                                          }
                                        }
                                      ]
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Ingest accepted",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "status": "ok",
                                              "accepted": 1,
                                              "requestId": "27aa717b-688f-458c-954f-f8fa8d8cbd01"

                                            }
                                            """
                            )
                    )
            )
    })
    public IngestAckResponse ingestBatch(@Valid @RequestBody IngestBatchRequest request) {
        return ingestService.ingest(request);
    }

    @PostMapping("/agents/heartbeat")
    @Operation(summary = "Update agent heartbeat", description = "Creates or updates agent status and last seen timestamp.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Agent heartbeat payload",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "agentId": "agent-dev-01",
                                      "agentName": "Main dev agent",
                                      "hostName": "agent-vm-01",
                                      "hostIp": "194.113.106.38",
                                      "status": "online",
                                      "bufferedCount": 0,
                                      "lastSequence": 1774104589716,
                                      "watchedSources": [
                                        "/var/log/nginx/error.log",
                                        "/var/log/syslog"
                                      ]
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Agent status updated",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "agentId": "agent-dev-01",
                                              "name": "Main dev agent",
                                              "host": "agent-vm-01",
                                              "hostIp": "194.113.106.38",
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
