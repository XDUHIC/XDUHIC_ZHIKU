package com.example.backend.config;

import com.example.backend.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JwtAuthenticationFilter jwtFilter,
                                           CorsConfigurationSource corsConfigurationSource) throws Exception {
        http
                // 1. 基础安全配置
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 2. CSP 配置
                .headers(headers -> headers
                        .frameOptions(frame -> frame.deny()) // 修正: Spring Security 6.1+ 推荐写法
                        .contentTypeOptions(contentType -> contentType.disable()) // 保持兼容
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives(
                                        "default-src 'self'; " +
                                                // 允许 marked.js 从 jsdelivr 加载
                                                "script-src 'self' 'unsafe-inline' 'unsafe-eval' https://cdn.jsdelivr.net; " +
                                                // 允许样式加载
                                                "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net; " +
                                                "img-src 'self' data: https:; " +
                                                "font-src 'self' data:; " +
                                                "connect-src 'self' *; " +
                                                "frame-ancestors 'none'; " +
                                                "base-uri 'self'; " +
                                                "form-action 'self';"
                                )
                        )
                )

                // 3. 路径权限控制
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/api/oauth/**", "/api/portal/**", "/api/knowledge/**", "/files/**", "/images/**", "/uploads/**", "/error", "/favicon.ico").permitAll()
                        .requestMatchers(HttpMethod.GET, "/", "/api/projects", "/api/projects/**", "/api/tools", "/api/tools/**", "/api/articles", "/api/articles/**", "/api/senior-shares", "/api/senior-shares/**", "/api/resources", "/api/resources/**", "/api/competitions", "/api/competitions/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/projects/*/view", "/api/resources/*/view", "/api/competitions/*/view", "/api/tools/*/view", "/api/articles/*/view", "/api/senior-shares/*/view").permitAll()
                        .requestMatchers("/api/upload/**").authenticated()
                        .anyRequest().authenticated()
                )

                // 4. 添加过滤器
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}