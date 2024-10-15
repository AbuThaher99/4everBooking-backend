package org.example.GraduationProject.WebApi.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve images from ProfileImages directory
        registry.addResourceHandler("/ProfileImages/**")
                .addResourceLocations("file:uploads/ProfileImages/");

        // Serve HallImage from static directory
        registry.addResourceHandler("/HallImage/**")
                .addResourceLocations("file:uploads/HallImage/");

        // Serve the PDF file from the static directory
        registry.addResourceHandler("/Pdf/**")
                .addResourceLocations("file:uploads/Pdf/");

        // Serve the chart from the static directory
        registry.addResourceHandler("/Chart/**")
                .addResourceLocations("file:uploads/Chart/");

        registry.addResourceHandler("/proofHalls/**")
                .addResourceLocations("file:uploads/proofHalls/");
    }
}
