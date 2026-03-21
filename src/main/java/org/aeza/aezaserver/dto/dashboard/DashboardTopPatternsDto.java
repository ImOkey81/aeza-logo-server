package org.aeza.aezaserver.dto.dashboard;

import java.util.List;

public record DashboardTopPatternsDto(List<Item> items) {
    public record Item(
            String fingerprint,
            String template,
            long count,
            boolean sampled,
            boolean burstDetected
    ) {
    }
}
