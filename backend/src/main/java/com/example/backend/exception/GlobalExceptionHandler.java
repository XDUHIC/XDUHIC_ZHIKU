package com.example.backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<?> handleBusiness(BusinessException ex, HttpServletRequest request) {
        int code = ex.getCode() > 0 ? ex.getCode() : 400;
        HttpStatus status = HttpStatus.resolve(code);
        if (status == null) {
            status = HttpStatus.BAD_REQUEST;
        }

        String path = request != null ? request.getRequestURI() : "N/A";
        if (status.is5xxServerError()) {
            logger.error("Business exception: path={}, code={}, message={}", path, code, ex.getMessage(), ex);
        } else {
            logger.warn("Business exception: path={}, code={}, message={}", path, code, ex.getMessage());
        }

        return ResponseEntity.status(status)
                .body(Map.of("code", code, "message", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValid(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        if (msg.isEmpty()) {
            msg = "参数校验失败";
        }
        logger.warn("参数校验失败: {}", msg);
        return ResponseEntity.badRequest().body(Map.of("code", 400, "message", msg));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraint(ConstraintViolationException ex) {
        logger.warn("约束校验失败: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(Map.of("code", 400, "message", "参数约束校验失败"));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentials(BadCredentialsException ex) {
        logger.warn("认证失败: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("code", 401, "message", "用户名或密码错误"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(AccessDeniedException ex) {
        logger.warn("访问被拒绝: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("code", 403, "message", "访问被拒绝"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        logger.warn("非法参数: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(Map.of("code", 400, "message", "请求参数错误"));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<?> handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest request) {
        String path = request != null ? request.getRequestURI() : ex.getResourcePath();
        logger.warn("Static resource not found: method={}, path={}",
                request != null ? request.getMethod() : "N/A", path);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("code", 404, "message", "Resource not found"));
    }
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<?> handleNoHandlerFound(NoHandlerFoundException ex, HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path != null && (path.endsWith("favicon.ico") || path.endsWith("robots.txt"))) {
            logger.debug("未映射请求（可忽略）: {}", path);
        } else {
            logger.warn("未映射请求: {} {}", request.getMethod(), path);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("code", 404, "message", "接口不存在"));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<?> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        String path = request != null ? request.getRequestURI() : "N/A";
        logger.warn("Method not supported: method={}, path={}, supported={}",
                ex.getMethod(), path, ex.getSupportedHttpMethods());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(Map.of("code", 405, "message", "请求方法不被支持"));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntime(RuntimeException ex) {
        logger.error("运行时异常", ex);
        // 不暴露具体的异常信息，只返回通用错误消息
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("code", 500, "message", "服务器内部错误"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneral(Exception ex) {
        logger.error("未处理的异常", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("code", 500, "message", "服务器内部错误"));
    }
}