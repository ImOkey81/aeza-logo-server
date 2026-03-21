package org.aeza.aezaserver.service;

import org.aeza.aezaserver.dto.ingest.IngestAckResponse;
import org.aeza.aezaserver.dto.ingest.IngestBatchRequest;
import org.aeza.aezaserver.dto.search.LogSearchItemDto;
import org.aeza.aezaserver.integration.OpenSearchClientAdapter;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
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

        request.events().forEach(event -> {
            LogSearchItemDto dto = new LogSearchItemDto(
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
            );
            liveStreamService.publish("log_event", toLivePayload(dto));
        });

        return new IngestAckResponse("ok", request.events().size(), UUID.randomUUID().toString());
    }

    private Map<String, Object> toLivePayload(LogSearchItemDto dto) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("timestamp", dto.timestamp() == null ? null : dto.timestamp().toString());
        payload.put("level", dto.level());
        payload.put("message", dto.message());
        payload.put("host", dto.host());
        payload.put("service", dto.service());
        payload.put("sourceType", dto.sourceType());
        payload.put("sourcePath", dto.sourcePath());
        payload.put("tags", dto.tags());
        payload.put("metadata", dto.metadata());
        payload.put("agentId", dto.agentId());
        return payload;
    }
}
