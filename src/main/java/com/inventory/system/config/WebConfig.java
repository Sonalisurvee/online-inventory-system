package com.inventory.system.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Compute absolute path to the upload folder
        String absoluteUploadDir = System.getProperty("user.dir") + "/" + uploadDir;
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + absoluteUploadDir + "/");
    }
}