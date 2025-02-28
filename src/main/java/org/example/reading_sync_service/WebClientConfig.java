package org.example.reading_sync_service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.CodecConfigurer;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .codecs(this::configureCodecs)
                .build();
    }

    private void configureCodecs(CodecConfigurer configurer) {
        // Configure maximum in-memory size for response body to 10MB (adjust as needed)
        configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024);  // 10MB
    }
}
