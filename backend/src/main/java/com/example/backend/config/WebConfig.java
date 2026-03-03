package com.example.backend.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 *
 * 修复说明：
 * 1. 移除了通配符 "*"，改为明确指定允许的源
 * 2. 使用 allowedOriginPatterns 支持端口通配符
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.file.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // API 接口的 CORS 配置
        registry.addMapping("/api/**")
                .allowedOriginPatterns(
                        "http://localhost:*",      // 允许 localhost 的所有端口
                        "http://127.0.0.1:*",      // 允许 127.0.0.1 的所有端口
                        "https://www.xduhic.top",  // 生产环境
                        "https://xduhic.top"       // 生产环境
                )
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("X-Auth-Error", "X-Auth-Error-Detail", "Authorization")
                .allowCredentials(true)
                .maxAge(3600);

        // 文件访问的 CORS 配置
        registry.addMapping("/files/**")
                .allowedOriginPatterns(
                        "http://localhost:*",
                        "http://127.0.0.1:*",
                        "https://www.xduhic.top",
                        "https://xduhic.top"
                )
                .allowedMethods("GET")
                .allowedHeaders("*")
                .maxAge(86400);

        registry.addMapping("/uploads/**")
                .allowedOriginPatterns(
                        "http://localhost:*",
                        "http://127.0.0.1:*",
                        "https://www.xduhic.top",
                        "https://xduhic.top"
                )
                .allowedMethods("GET")
                .allowedHeaders("*")
                .maxAge(86400);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        // 支持多种文件访问路径
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + uploadPath.toString() + "/")
                .setCachePeriod(3600);
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath.toString() + "/")
                .setCachePeriod(0); // 禁用缓存，确保文件更新立即生效
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + uploadPath.toString() + "/images/")
                .setCachePeriod(3600);
        registry.addResourceHandler("/documents/**")
                .addResourceLocations("file:" + uploadPath.toString() + "/documents/")
                .setCachePeriod(3600);
        registry.addResourceHandler("/resources/**")
                .addResourceLocations("file:" + uploadPath.toString() + "/resources/")
                .setCachePeriod(3600);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}