package org.aeza.aezaserver.dto.dashboard;

public record DashboardSummaryDto(
        long totalLogs,
        long errors,
        long warnings,
        long hosts,
        long services,
        long aggregatedEvents,
        long sampledEvents,
        long burstEvents
) {
}
