package org.aeza.aezaserver.dto.search;

import java.util.List;
import java.util.Map;

public record SearchResponseDto(
        List<LogSearchItemDto> items,
        long total,
        Map<String, Object> aggregations
) {
}
