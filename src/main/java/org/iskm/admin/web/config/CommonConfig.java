package org.iskm.admin.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class CommonConfig {
    
    @Bean
    public ObjectMapper mapper() {
        return new ObjectMapper();
    }
}
