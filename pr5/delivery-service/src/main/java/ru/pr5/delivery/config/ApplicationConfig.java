package ru.pr5.delivery.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.time.Clock;

@Configuration
public class ApplicationConfig {
    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    RestClient orderRestClient(
            RestClient.Builder builder,
            @Value("${clients.order-service.base-url}") String baseUrl) {
        return builder.baseUrl(baseUrl).build();
    }
}
