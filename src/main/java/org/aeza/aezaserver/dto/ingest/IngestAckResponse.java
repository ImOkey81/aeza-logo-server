package org.aeza.aezaserver.dto.ingest;

public record IngestAckResponse(
        String status,
        int accepted,
        String requestId
) {
}
