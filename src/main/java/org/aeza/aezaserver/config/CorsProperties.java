package org.aeza.aezaserver.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "cors")
public class CorsProperties {
    private List<String> allowedOriginPatterns = new ArrayList<>(List.of("*"));

    public List<String> getAllowedOriginPatterns() {
        return allowedOriginPatterns;
    }

    public void setAllowedOriginPatterns(List<String> allowedOriginPatterns) {
        if (allowedOriginPatterns == null || allowedOriginPatterns.isEmpty()) {
            this.allowedOriginPatterns = new ArrayList<>(List.of("*"));
            return;
        }

        this.allowedOriginPatterns = new ArrayList<>(allowedOriginPatterns);
    }
}
