package com.stream.video.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ConfigHttpRequest {

    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }
}
