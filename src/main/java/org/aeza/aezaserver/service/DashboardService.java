package org.aeza.aezaserver.service;

import org.aeza.aezaserver.dto.dashboard.DashboardSummaryDto;
import org.aeza.aezaserver.dto.dashboard.DashboardTimeseriesDto;
import org.aeza.aezaserver.dto.dashboard.DashboardTopHostsDto;
import org.aeza.aezaserver.dto.dashboard.DashboardTopServicesDto;
import org.aeza.aezaserver.integration.OpenSearchClientAdapter;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class DashboardService {
    private final OpenSearchClientAdapter openSearchClientAdapter;

    public DashboardService(OpenSearchClientAdapter openSearchClientAdapter) {
        this.openSearchClientAdapter = openSearchClientAdapter;
    }

    public DashboardSummaryDto summary(Instant from, Instant to) {
        SearchCriteria criteria = new SearchCriteria(null, null, null, null, from, to);
        long errors = openSearchClientAdapter.count(new SearchCriteria(null, null, null, "ERROR", from, to));
        long warnings = openSearchClientAdapter.count(new SearchCriteria(null, null, null, "WARN", from, to))
                + openSearchClientAdapter.count(new SearchCriteria(null, null, null, "WARNING", from, to));

        return new DashboardSummaryDto(
                openSearchClientAdapter.count(criteria),
                errors,
                warnings,
                openSearchClientAdapter.uniqueHosts(criteria),
                openSearchClientAdapter.uniqueServices(criteria)
        );
    }

    public DashboardTimeseriesDto timeseries(Instant from, Instant to) {
        SearchCriteria criteria = new SearchCriteria(null, null, null, null, from, to);
        return new DashboardTimeseriesDto(
                openSearchClientAdapter.timeseries(criteria).stream()
                        .map(bucket -> new DashboardTimeseriesDto.Bucket(bucket.timestamp(), bucket.errors(), bucket.warnings()))
                        .toList()
        );
    }

    public DashboardTopHostsDto topHosts(Instant from, Instant to, int limit) {
        return new DashboardTopHostsDto(
                openSearchClientAdapter.topHosts(new SearchCriteria(null, null, null, null, from, to), limit).entrySet().stream()
                        .map(entry -> new DashboardTopHostsDto.Item(entry.getKey(), entry.getValue()))
                        .toList()
        );
    }

    public DashboardTopServicesDto topServices(Instant from, Instant to, int limit) {
        return new DashboardTopServicesDto(
                openSearchClientAdapter.topServices(new SearchCriteria(null, null, null, null, from, to), limit).entrySet().stream()
                        .map(entry -> new DashboardTopServicesDto.Item(entry.getKey(), entry.getValue()))
                        .toList()
        );
    }
}
