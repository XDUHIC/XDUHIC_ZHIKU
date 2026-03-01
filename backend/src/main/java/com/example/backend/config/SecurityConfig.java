package com.example.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.http.HttpMethod;


import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import com.example.backend.security.JwtAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
//整个后端最重要的安全配置文件，决定了哪些接口可以公开访问，哪些需要登录
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JwtAuthenticationFilter jwtFilter,
                                           CorsConfigurationSource corsConfigurationSource) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers
                        .frameOptions().deny()
                        .contentTypeOptions().and()
                        // 禁用HSTS配置，让Nginx处理SSL安全
                        // .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                        //         .maxAgeInSeconds(31536000)
                        //         .includeSubDomains(true))
                        .and())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/knowledge/resources/*/comments").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/knowledge/resources/*/like").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/knowledge/resources/*/like").authenticated()
                        .requestMatchers("/api/auth/**", "/api/oauth/**", "/api/portal/**", "/api/knowledge/**", "/files/**", "/images/**", "/uploads/**", "/error", "/favicon.ico").permitAll()
                        .requestMatchers(HttpMethod.GET, "/", "/api/projects", "/api/projects/**", "/api/tools", "/api/tools/**", "/api/articles", "/api/articles/**", "/api/senior-shares", "/api/senior-shares/**", "/api/resources", "/api/resources/**", "/api/competitions", "/api/competitions/**", "/api/qna", "/api/qna/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/projects/*/view", "/api/resources/*/view", "/api/competitions/*/view", "/api/tools/*/view", "/api/articles/*/view", "/api/senior-shares/*/view", "/api/qna/questions/*/view").permitAll()
                        .requestMatchers("/api/upload/**").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}


