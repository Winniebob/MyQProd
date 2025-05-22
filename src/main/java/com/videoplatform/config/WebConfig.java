package com.videoplatform.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Любой запрос /uploads/** будет мапиться на файловую систему uploads/
        registry
                .addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Разрешаем фронту обращаться к нашему API и статикам
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000") // тут укажите URL вашего фронта
                .allowedMethods("*")
                .allowCredentials(true);
    }
}