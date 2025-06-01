package com.videoplatform.config;

import com.videoplatform.util.JwtUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    @Value("${app.hls.jwtSecret}")
    private String jwtSecret;

    @Value("${app.hls.jwtExpirationMillis}")
    private long jwtExpirationMillis;

    @Bean
    public JwtUtils jwtUtils() {
        return new JwtUtils(jwtSecret, jwtExpirationMillis);
    }
}