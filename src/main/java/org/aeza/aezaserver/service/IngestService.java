package org.aeza.aezaserver.service;

import org.aeza.aezaserver.dto.ingest.IngestAckResponse;
import org.aeza.aezaserver.dto.ingest.IngestBatchRequest;
import org.aeza.aezaserver.dto.search.LogSearchItemDto;
import org.aeza.aezaserver.integration.OpenSearchClientAdapter;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class IngestService {
    private final OpenSearchClientAdapter openSearchClientAdapter;
    private final LiveStreamService liveStreamService;

    public IngestService(OpenSearchClientAdapter openSearchClientAdapter, LiveStreamService liveStreamService) {
        this.openSearchClientAdapter = openSearchClientAdapter;
        this.liveStreamService = liveStreamService;
    }

    public IngestAckResponse ingest(IngestBatchRequest request) {
        openSearchClientAdapter.bulkIndex(request.agentId(), request.events());

        request.events().forEach(event -> liveStreamService.publish("log_event", new LogSearchItemDto(
                event.timestamp(),
                event.level(),
                event.message(),
                event.host(),
                event.service(),
                event.sourceType(),
                event.sourcePath(),
                event.tags(),
                event.metadata(),
                event.agentId() == null || event.agentId().isBlank() ? request.agentId() : event.agentId()
        )));

        return new IngestAckResponse("ok", request.events().size(), UUID.randomUUID().toString());
    }
}
