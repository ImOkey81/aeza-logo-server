package org.aeza.aezaserver.service;

import org.aeza.aezaserver.dto.dashboard.DashboardSummaryDto;
import org.aeza.aezaserver.dto.dashboard.DashboardTimeseriesDto;
import org.aeza.aezaserver.dto.dashboard.DashboardTopHostsDto;
import org.aeza.aezaserver.dto.dashboard.DashboardTopPatternsDto;
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
        long errors = openSearchClientAdapter.observedCount(new SearchCriteria(null, null, null, "ERROR", from, to));
        long warnings = openSearchClientAdapter.observedCount(new SearchCriteria(null, null, null, "WARN", from, to))
                + openSearchClientAdapter.observedCount(new SearchCriteria(null, null, null, "WARNING", from, to));

        return new DashboardSummaryDto(
                openSearchClientAdapter.observedCount(criteria),
                errors,
                warnings,
                openSearchClientAdapter.uniqueHosts(criteria),
                openSearchClientAdapter.uniqueServices(criteria),
                openSearchClientAdapter.count(new SearchCriteria(null, null, null, null, from, to, null, null, true, null, null)),
                openSearchClientAdapter.count(new SearchCriteria(null, null, null, null, from, to, null, null, null, true, null)),
                openSearchClientAdapter.count(new SearchCriteria(null, null, null, null, from, to, null, null, null, null, true))
        );
    }

    public DashboardTimeseriesDto timeseries(Instant from, Instant to) {
        SearchCriteria criteria = new SearchCriteria(null, null, null, null, from, to);
        return new DashboardTimeseriesDto(
                openSearchClientAdapter.timeseriesObserved(criteria).stream()
                        .map(bucket -> new DashboardTimeseriesDto.Bucket(bucket.timestamp(), bucket.errors(), bucket.warnings()))
                        .toList()
        );
    }

    public DashboardTopHostsDto topHosts(Instant from, Instant to, int limit) {
        return new DashboardTopHostsDto(
                openSearchClientAdapter.topHostsObserved(new SearchCriteria(null, null, null, null, from, to), limit).entrySet().stream()
                        .map(entry -> new DashboardTopHostsDto.Item(entry.getKey(), entry.getValue()))
                        .toList()
        );
    }

    public DashboardTopServicesDto topServices(Instant from, Instant to, int limit) {
        return new DashboardTopServicesDto(
                openSearchClientAdapter.topServicesObserved(new SearchCriteria(null, null, null, null, from, to), limit).entrySet().stream()
                        .map(entry -> new DashboardTopServicesDto.Item(entry.getKey(), entry.getValue()))
                        .toList()
        );
    }

    public DashboardTopPatternsDto topPatterns(Instant from, Instant to, int limit) {
        return new DashboardTopPatternsDto(
                openSearchClientAdapter.topPatterns(new SearchCriteria(null, null, null, null, from, to), limit).stream()
                        .map(item -> new DashboardTopPatternsDto.Item(
                                item.fingerprint(),
                                item.template(),
                                item.count(),
                                item.sampled(),
                                item.burstDetected()
                        ))
                        .toList()
        );
    }
}
