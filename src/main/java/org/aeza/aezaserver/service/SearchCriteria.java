package org.aeza.aezaserver.service;

import java.time.Instant;

public record SearchCriteria(
        String q,
        String host,
        String service,
        String level,
        Instant from,
        Instant to
) {
}
