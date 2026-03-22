package org.aeza.aezaserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.aeza.aezaserver.dto.dashboard.DashboardSummaryDto;
import org.aeza.aezaserver.dto.dashboard.DashboardTimeseriesDto;
import org.aeza.aezaserver.dto.dashboard.DashboardTopHostsDto;
import org.aeza.aezaserver.dto.dashboard.DashboardTopPatternsDto;
import org.aeza.aezaserver.dto.dashboard.DashboardTopServicesDto;
import org.aeza.aezaserver.service.DashboardService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard", description = "Dashboard widgets and aggregations")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    @Operation(summary = "Get summary metrics", description = "Returns totals for logs, errors, warnings, hosts, services and agent-side aggregation markers.")
    public DashboardSummaryDto summary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) Long groupId
    ) {
        return dashboardService.summary(from, to, groupId);
    }

    @GetMapping("/timeseries")
    @Operation(summary = "Get timeseries", description = "Returns time buckets for errors and warnings using observed occurrences from agent metadata.")
    public DashboardTimeseriesDto timeseries(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) Long groupId
    ) {
        return dashboardService.timeseries(from, to, groupId);
    }

    @GetMapping("/top-hosts")
    @Operation(summary = "Get top hosts", description = "Returns hosts sorted by observed log volume.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Top hosts response",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "items": [
                                                {
                                                  "host": "agent-vm-01",
                                                  "count": 153
                                                },
                                                {
                                                  "host": "agent-vm-02",
                                                  "count": 41
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            )
    })
    public DashboardTopHostsDto topHosts(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) Long groupId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return dashboardService.topHosts(from, to, groupId, limit);
    }

    @GetMapping("/top-services")
    @Operation(summary = "Get top services", description = "Returns services sorted by observed log volume.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Top services response",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "items": [
                                                {
                                                  "service": "syslog",
                                                  "count": 153
                                                },
                                                {
                                                  "service": "nginx",
                                                  "count": 41
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            )
    })
    public DashboardTopServicesDto topServices(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) Long groupId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return dashboardService.topServices(from, to, groupId, limit);
    }

    @GetMapping("/top-patterns")
    @Operation(summary = "Get top patterns", description = "Returns noisy patterns grouped by agent fingerprint and message template.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Top patterns response",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "items": [
                                                {
                                                  "fingerprint": "d5c98f0a8be1d4ac",
                                                  "template": "error request <num> failed",
                                                  "count": 96,
                                                  "sampled": true,
                                                  "burstDetected": true
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            )
    })
    public DashboardTopPatternsDto topPatterns(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) Long groupId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return dashboardService.topPatterns(from, to, groupId, limit);
    }
}
