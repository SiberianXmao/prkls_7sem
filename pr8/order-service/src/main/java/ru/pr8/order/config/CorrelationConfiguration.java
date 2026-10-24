package ru.pr8.order.config;

import feign.RequestInterceptor;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Configuration
public class CorrelationConfiguration {
    public static final String HEADER = "X-Request-ID";

    @Bean
    RequestInterceptor correlationFeignInterceptor() {
        return template -> {
            String requestId = MDC.get("requestId");
            if (requestId != null) {
                template.header(HEADER, requestId);
            }
        };
    }

    @Bean
    OncePerRequestFilter correlationFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                    throws ServletException, IOException {
                String requestId = request.getHeader(HEADER);
                if (requestId == null || requestId.isBlank()) {
                    requestId = UUID.randomUUID().toString();
                }
                MDC.put("requestId", requestId);
                response.setHeader(HEADER, requestId);
                try {
                    chain.doFilter(request, response);
                } finally {
                    MDC.remove("requestId");
                }
            }
        };
    }
}
