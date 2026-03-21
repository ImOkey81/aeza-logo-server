package org.aeza.aezaserver.dto.search;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record LogSearchItemDto(
        Instant timestamp,
        String level,
        String message,
        String host,
        String service,
        String sourceType,
        String sourcePath,
        List<String> tags,
        Map<String, Object> metadata,
        String agentId
) {
}
