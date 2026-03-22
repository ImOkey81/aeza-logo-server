package org.aeza.aezaserver.service;

import org.aeza.aezaserver.dto.search.LogSearchItemDto;
import org.aeza.aezaserver.dto.search.SearchResponseDto;
import org.aeza.aezaserver.integration.OpenSearchClientAdapter;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class SearchService {
    private final OpenSearchClientAdapter openSearchClientAdapter;
    private final AgentGroupService agentGroupService;
    private final AgentService agentService;

    public SearchService(
            OpenSearchClientAdapter openSearchClientAdapter,
            AgentGroupService agentGroupService,
            AgentService agentService
    ) {
        this.openSearchClientAdapter = openSearchClientAdapter;
        this.agentGroupService = agentGroupService;
        this.agentService = agentService;
    }

    public SearchResponseDto search(
            String q,
            String host,
            String service,
            String level,
            Instant from,
            Instant to,
            int page,
            int size
    ) {
        return search(q, host, service, level, from, to, null, null, null, null, null, null, null, page, size);
    }

    public SearchResponseDto search(
            String q,
            String host,
            String service,
            String level,
            Instant from,
            Instant to,
            String agentId,
            String agentName,
            Long groupId,
            String fingerprint,
            Boolean aggregated,
            Boolean sampled,
            Boolean burstDetected,
            int page,
            int size
    ) {
        java.util.List<String> resolvedAgentIds = mergeAgentIds(
                agentGroupService.resolveAgentIds(groupId),
                agentService.resolveAgentIdsByName(agentName)
        );
        SearchCriteria criteria = new SearchCriteria(
                q,
                host,
                service,
                level,
                from,
                to,
                agentId,
                agentName,
                groupId,
                resolvedAgentIds,
                fingerprint,
                aggregated,
                sampled,
                burstDetected
        );
        OpenSearchClientAdapter.SearchResult result = openSearchClientAdapter.search(
                criteria,
                page,
                size
        );

        return new SearchResponseDto(
                result.items().stream()
                        .map(item -> new LogSearchItemDto(
                                item.timestamp(),
                                item.level(),
                                item.message(),
                                item.host(),
                                item.service(),
                                item.sourceType(),
                                item.sourcePath(),
                                item.tags(),
                                item.metadata(),
                                item.agentId()
                        ))
                        .toList(),
                result.total(),
                result.aggregations()
        );
    }

    private java.util.List<String> mergeAgentIds(java.util.List<String> left, java.util.List<String> right) {
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }
        java.util.LinkedHashSet<String> intersection = new java.util.LinkedHashSet<>(left);
        intersection.retainAll(right);
        return java.util.List.copyOf(intersection);
    }
}
