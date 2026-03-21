package org.aeza.aezaserver.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(OpenSearchProperties.class)
public class OpenSearchConfig {

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    public RestClient openSearchRestClient(OpenSearchProperties properties) {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl(properties.url());

        if (StringUtils.hasText(properties.username())) {
            builder.defaultHeader(
                    HttpHeaders.AUTHORIZATION,
                    "Basic " + java.util.Base64.getEncoder()
                            .encodeToString((properties.username() + ":" + (properties.password() == null ? "" : properties.password()))
                                    .getBytes(java.nio.charset.StandardCharsets.UTF_8))
            );
        }

        return builder.build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return JsonMapper.builder()
                .findAndAddModules()
                .build();
    }
}
