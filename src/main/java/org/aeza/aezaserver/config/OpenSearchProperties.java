package org.aeza.aezaserver.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "opensearch")
public record OpenSearchProperties(
        boolean enabled,
        String url,
        String username,
        String password,
        String indexPrefix
) {
}
