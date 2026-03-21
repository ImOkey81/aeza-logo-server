package org.aeza.aezaserver.service;

import org.aeza.aezaserver.dto.search.LogSearchItemDto;
import org.aeza.aezaserver.dto.search.SearchResponseDto;
import org.aeza.aezaserver.integration.OpenSearchClientAdapter;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class SearchService {
    private final OpenSearchClientAdapter openSearchClientAdapter;

    public SearchService(OpenSearchClientAdapter openSearchClientAdapter) {
        this.openSearchClientAdapter = openSearchClientAdapter;
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
        OpenSearchClientAdapter.SearchResult result = openSearchClientAdapter.search(
                new SearchCriteria(q, host, service, level, from, to),
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
}
