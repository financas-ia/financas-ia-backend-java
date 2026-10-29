package com.example.financas.pluggy.config;

import ai.pluggy.client.PluggyClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PluggyConfig {

    @Value("${pluggy.client-id}")
    private String clientId;

    @Value("${pluggy.client-secret}")
    private String clientSecret;

    @Bean
    public PluggyClient pluggyClient() {
        return PluggyClient.builder()
                .clientIdAndSecret(clientId, clientSecret)
                .build();
    }
}
