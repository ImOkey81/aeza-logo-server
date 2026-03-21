package org.aeza.aezaserver.controller;

import org.aeza.aezaserver.dto.dashboard.DashboardSummaryDto;
import org.aeza.aezaserver.dto.dashboard.DashboardTimeseriesDto;
import org.aeza.aezaserver.dto.dashboard.DashboardTopHostsDto;
import org.aeza.aezaserver.dto.dashboard.DashboardTopServicesDto;
import org.aeza.aezaserver.service.AuthService;
import org.aeza.aezaserver.service.DashboardService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;
    private final AuthService authService;

    public DashboardController(DashboardService dashboardService, AuthService authService) {
        this.dashboardService = dashboardService;
        this.authService = authService;
    }

    @GetMapping("/summary")
    public DashboardSummaryDto summary(
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        authService.requireAuth(authorization);
        return dashboardService.summary(from, to);
    }

    @GetMapping("/timeseries")
    public DashboardTimeseriesDto timeseries(
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        authService.requireAuth(authorization);
        return dashboardService.timeseries(from, to);
    }

    @GetMapping("/top-hosts")
    public DashboardTopHostsDto topHosts(
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "10") int limit
    ) {
        authService.requireAuth(authorization);
        return dashboardService.topHosts(from, to, limit);
    }

    @GetMapping("/top-services")
    public DashboardTopServicesDto topServices(
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "10") int limit
    ) {
        authService.requireAuth(authorization);
        return dashboardService.topServices(from, to, limit);
    }
}
