package org.aeza.aezaserver.dto.dashboard;

import java.time.Instant;
import java.util.List;

public record DashboardTimeseriesDto(List<Bucket> buckets) {
    public record Bucket(Instant timestamp, long errors, long warnings) {
    }
}
