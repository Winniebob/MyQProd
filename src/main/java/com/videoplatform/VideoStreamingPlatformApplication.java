package com.videoplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class VideoStreamingPlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(VideoStreamingPlatformApplication.class, args);
    }
}