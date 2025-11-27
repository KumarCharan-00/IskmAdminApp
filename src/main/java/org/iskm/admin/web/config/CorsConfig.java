package org.iskm.admin.web.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class CorsConfig {

    @Value("${allowed.endpoints.list}") // optional property, empty by default
    private String allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        var allowedOriginsList = Arrays.stream((allowedOrigins == null ? "" : allowedOrigins).split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        log.info("AllowedOrigins: {}, AllowedOriginList: {}", allowedOrigins, allowedOriginsList);

        configuration.addAllowedOrigin(allowedOrigins);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
