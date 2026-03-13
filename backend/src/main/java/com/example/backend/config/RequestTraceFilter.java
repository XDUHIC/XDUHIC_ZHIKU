package com.example.backend.config;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestTraceFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RequestTraceFilter.class);
    private static final String TRACE_ID = "traceId";
    private static final String TRACE_HEADER = "X-Trace-Id";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri != null && (uri.endsWith("/favicon.ico") || uri.endsWith("/robots.txt"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String traceId = resolveTraceId(request);
        MDC.put(TRACE_ID, traceId);
        response.setHeader(TRACE_HEADER, traceId);

        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            logger.error("Unhandled request exception: method={}, uri={}, query={}, ip={}",
                    request.getMethod(), request.getRequestURI(), request.getQueryString(), resolveClientIp(request), ex);
            throw ex;
        } finally {
            long durationMs = System.currentTimeMillis() - start;
            int status = response.getStatus();
            String method = request.getMethod();
            String uri = request.getRequestURI();

            if (status >= 500) {
                logger.error("Request completed with server error: method={}, uri={}, status={}, durationMs={}, ip={}",
                        method, uri, status, durationMs, resolveClientIp(request));
            } else if (status >= 400) {
                logger.warn("Request completed with client error: method={}, uri={}, status={}, durationMs={}, ip={}",
                        method, uri, status, durationMs, resolveClientIp(request));
            }

            MDC.remove(TRACE_ID);
        }
    }

    private String resolveTraceId(HttpServletRequest request) {
        String traceId = request.getHeader(TRACE_HEADER);
        if (StringUtils.hasText(traceId)) {
            return traceId.trim();
        }
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(xRealIp)) {
            return xRealIp.trim();
        }

        return request.getRemoteAddr();
    }
}
