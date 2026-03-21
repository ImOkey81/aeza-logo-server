package org.aeza.aezaserver.dto.dashboard;

import java.util.List;

public record DashboardTopHostsDto(List<Item> items) {
    public record Item(String host, long count) {
    }
}
