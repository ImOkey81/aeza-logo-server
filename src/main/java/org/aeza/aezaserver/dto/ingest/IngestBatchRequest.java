package org.aeza.aezaserver.dto.ingest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record IngestBatchRequest(
        @NotBlank String agentId,
        @NotEmpty List<@Valid LogEventDto> events
) {
}
