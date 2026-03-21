package org.aeza.aezaserver.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI aezaOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Aeza Server API")
                        .description("REST, WebSocket and SSE API for logs, dashboard, agents and alerts.")
                        .version("v1")
                        .contact(new Contact()
                                .name("Aeza Server")
                                .url("https://github.com/")))
                .servers(List.of(
                        new Server().url("http://194.113.106.38:8087").description("Production server"),
                        new Server().url("http://localhost:8087").description("Local server")
                ));
    }
}
