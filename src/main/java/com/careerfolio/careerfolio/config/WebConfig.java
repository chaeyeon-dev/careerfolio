package com.careerfolio.careerfolio.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // 브라우저에서 /uploads/** 요청 시
        // 실제 PC의 C:/careerfolio/uploads/ 폴더의 파일을 응답함
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:///C:/careerfolio/uploads/");
    }
}
