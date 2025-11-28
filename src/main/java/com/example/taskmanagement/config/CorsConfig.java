package com.example.taskmanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Allow requests from localhost on various ports
        config.setAllowedOrigins(List.of(
            "http://localhost:4200",
            "http://localhost:3000",
            "http://localhost:8080",
            "http://127.0.0.1:4200",
            "http://127.0.0.1:3000",
            "http://127.0.0.1:8080"
        ));

        // Allow all HTTP methods (GET, POST, PUT, DELETE, etc.)
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Allow all headers
        config.setAllowedHeaders(Arrays.asList("*"));

        // Allow credentials for SSE
        config.setAllowCredentials(true);

        // Expose headers for SSE
        config.setExposedHeaders(Arrays.asList(
            "Content-Type",
            "Cache-Control",
            "X-Requested-With"
        ));

        // Max age for preflight requests
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}

