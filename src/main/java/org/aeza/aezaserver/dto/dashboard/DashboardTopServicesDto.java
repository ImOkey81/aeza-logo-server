package org.aeza.aezaserver.dto.dashboard;

import java.util.List;

public record DashboardTopServicesDto(List<Item> items) {
    public record Item(String service, long count) {
    }
}
