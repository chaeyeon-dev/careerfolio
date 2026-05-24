package com.careerfolio.careerfolio.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        registry.addResourceHandler("/uploads/editor/**")
                .addResourceLocations("file:///C:/careerfolio/uploads/editor/");

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:///C:/careerfolio/uploads/");
    }
}
