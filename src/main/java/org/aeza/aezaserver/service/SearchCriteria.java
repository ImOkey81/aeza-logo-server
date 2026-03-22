package org.aeza.aezaserver.service;

import java.time.Instant;
import java.util.List;

public record SearchCriteria(
        String q,
        String host,
        String service,
        String level,
        Instant from,
        Instant to,
        String agentId,
        String agentName,
        Long groupId,
        List<String> agentIds,
        String fingerprint,
        Boolean aggregated,
        Boolean sampled,
        Boolean burstDetected
) {
    public SearchCriteria(
            String q,
            String host,
            String service,
            String level,
            Instant from,
            Instant to
    ) {
        this(q, host, service, level, from, to, null, null, null, null, null, null, null, null);
    }
}
