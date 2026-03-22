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
    private final AgentGroupService agentGroupService;

    public DashboardService(OpenSearchClientAdapter openSearchClientAdapter, AgentGroupService agentGroupService) {
        this.openSearchClientAdapter = openSearchClientAdapter;
        this.agentGroupService = agentGroupService;
    }

    public DashboardSummaryDto summary(Instant from, Instant to, Long groupId) {
        SearchCriteria criteria = criteria(null, from, to, groupId, null, null, null);
        long errors = openSearchClientAdapter.observedCount(criteria("ERROR", from, to, groupId, null, null, null));
        long warnings = openSearchClientAdapter.observedCount(criteria("WARN", from, to, groupId, null, null, null))
                + openSearchClientAdapter.observedCount(criteria("WARNING", from, to, groupId, null, null, null));

        return new DashboardSummaryDto(
                openSearchClientAdapter.observedCount(criteria),
                errors,
                warnings,
                openSearchClientAdapter.uniqueHosts(criteria),
                openSearchClientAdapter.uniqueServices(criteria),
                openSearchClientAdapter.count(criteria(null, from, to, groupId, true, null, null)),
                openSearchClientAdapter.count(criteria(null, from, to, groupId, null, true, null)),
                openSearchClientAdapter.count(criteria(null, from, to, groupId, null, null, true))
        );
    }

    public DashboardTimeseriesDto timeseries(Instant from, Instant to, Long groupId) {
        SearchCriteria criteria = criteria(null, from, to, groupId, null, null, null);
        return new DashboardTimeseriesDto(
                openSearchClientAdapter.timeseriesObserved(criteria).stream()
                        .map(bucket -> new DashboardTimeseriesDto.Bucket(bucket.timestamp(), bucket.errors(), bucket.warnings()))
                        .toList()
        );
    }

    public DashboardTopHostsDto topHosts(Instant from, Instant to, Long groupId, int limit) {
        return new DashboardTopHostsDto(
                openSearchClientAdapter.topHostsObserved(criteria(null, from, to, groupId, null, null, null), limit).entrySet().stream()
                        .map(entry -> new DashboardTopHostsDto.Item(entry.getKey(), entry.getValue()))
                        .toList()
        );
    }

    public DashboardTopServicesDto topServices(Instant from, Instant to, Long groupId, int limit) {
        return new DashboardTopServicesDto(
                openSearchClientAdapter.topServicesObserved(criteria(null, from, to, groupId, null, null, null), limit).entrySet().stream()
                        .map(entry -> new DashboardTopServicesDto.Item(entry.getKey(), entry.getValue()))
                        .toList()
        );
    }

    public DashboardTopPatternsDto topPatterns(Instant from, Instant to, Long groupId, int limit) {
        return new DashboardTopPatternsDto(
                openSearchClientAdapter.topPatterns(criteria(null, from, to, groupId, null, null, null), limit).stream()
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

    private SearchCriteria criteria(
            String level,
            Instant from,
            Instant to,
            Long groupId,
            Boolean aggregated,
            Boolean sampled,
            Boolean burstDetected
    ) {
        return new SearchCriteria(
                null,
                null,
                null,
                level,
                from,
                to,
                null,
                groupId,
                agentGroupService.resolveAgentIds(groupId),
                null,
                aggregated,
                sampled,
                burstDetected
        );
    }
}
