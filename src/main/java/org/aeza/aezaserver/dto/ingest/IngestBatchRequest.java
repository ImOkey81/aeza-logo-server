package org.aeza.aezaserver.dto.ingest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record IngestBatchRequest(
        @NotBlank @Size(max = ValidationLimits.AGENT_ID_MAX) String agentId,
        @NotEmpty List<@Valid LogEventDto> events
) {
}
