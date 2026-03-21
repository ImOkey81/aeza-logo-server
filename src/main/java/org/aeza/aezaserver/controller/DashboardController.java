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
    @Operation(summary = "Get summary metrics", description = "Returns totals for logs, errors, warnings, hosts and services.")
    public DashboardSummaryDto summary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        return dashboardService.summary(from, to);
    }

    @GetMapping("/timeseries")
    @Operation(summary = "Get timeseries", description = "Returns time buckets for errors and warnings.")
    public DashboardTimeseriesDto timeseries(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        return dashboardService.timeseries(from, to);
    }

    @GetMapping("/top-hosts")
    @Operation(summary = "Get top hosts", description = "Returns hosts sorted by log count.")
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
                                                  "host": "srv-1",
                                                  "count": 53
                                                },
                                                {
                                                  "host": "srv-2",
                                                  "count": 17
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
            @RequestParam(defaultValue = "10") int limit
    ) {
        return dashboardService.topHosts(from, to, limit);
    }

    @GetMapping("/top-services")
    @Operation(summary = "Get top services", description = "Returns services sorted by log count.")
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
                                                  "service": "backend",
                                                  "count": 53
                                                },
                                                {
                                                  "service": "postgres",
                                                  "count": 11
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
            @RequestParam(defaultValue = "10") int limit
    ) {
        return dashboardService.topServices(from, to, limit);
    }
}
