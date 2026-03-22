package org.aeza.aezaserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.aeza.aezaserver.dto.search.SearchResponseDto;
import org.aeza.aezaserver.service.LiveStreamService;
import org.aeza.aezaserver.service.SearchService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Search", description = "Search and realtime stream endpoints")
public class SearchController {
    private final SearchService searchService;
    private final LiveStreamService liveStreamService;

    public SearchController(SearchService searchService, LiveStreamService liveStreamService) {
        this.searchService = searchService;
        this.liveStreamService = liveStreamService;
    }

    @GetMapping("/logs/search")
    @Operation(summary = "Search logs", description = "Supports full-text search and filtering by host, service, level, agentId and agent-side metadata like fingerprint, aggregated, sampled and burstDetected.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Search result",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "items": [
                                                {
                                                  "timestamp": "2026-03-21T11:33:50Z",
                                                  "level": "ERROR",
                                                  "message": "database failed 2",
                                                  "host": "agent-vm-01",
                                                  "service": "nginx",
                                                  "sourceType": "file",
                                                  "sourcePath": "/var/log/nginx/error.log",
                                                  "tags": ["prod", "agent"],
                                                  "metadata": {
                                                    "fingerprint": "d5c98f0a8be1d4ac",
                                                    "messageTemplate": "database failed <num>",
                                                    "occurrences": 12,
                                                    "aggregated": true,
                                                    "burstDetected": true
                                                  },
                                                  "agentId": "agent-dev-01"
                                                }
                                              ],
                                              "total": 1,
                                              "aggregations": {
                                                "hosts": {
                                                  "agent-vm-01": 1
                                                },
                                                "services": {
                                                  "nginx": 1
                                                },
                                                "levels": {
                                                  "ERROR": 1
                                                },
                                                "fingerprints": {
                                                  "d5c98f0a8be1d4ac": 1
                                                }
                                              }
                                            }
                                            """
                            )
                    )
            )
    })
    public SearchResponseDto search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String host,
            @RequestParam(required = false) String service,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String agentId,
            @RequestParam(required = false) String fingerprint,
            @RequestParam(required = false) Boolean aggregated,
            @RequestParam(required = false) Boolean sampled,
            @RequestParam(required = false) Boolean burstDetected,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return searchService.search(q, host, service, level, from, to, agentId, fingerprint, aggregated, sampled, burstDetected, page, size);
    }

    @GetMapping("/live/sse")
    @Operation(summary = "Open SSE stream", description = "Server-sent events stream for log_event, agent_status and alert_triggered.")
    public SseEmitter live() {
        return liveStreamService.subscribe();
    }
}
