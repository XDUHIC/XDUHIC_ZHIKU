package com.example.backend.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.AuthDtos;
import com.example.backend.service.AuthService;
import com.example.backend.service.SecurityAuditService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final SecurityAuditService securityAuditService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid AuthDtos.RegisterReq req, HttpServletRequest request) {
        try {
            authService.register(req);
            securityAuditService.logRegistration(req.getUsername(), request);
            return ResponseEntity.ok(Map.of("code", 0, "message", "OK"));
        } catch (Exception e) {
            securityAuditService.logSuspiciousActivity("REGISTRATION_FAILURE", 
                "Registration failed for username: " + req.getUsername(), request);
            throw e;
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid AuthDtos.LoginReq req, HttpServletRequest request) {
        try {
            Map<String, Object> data = authService.login(req);
            securityAuditService.logLoginSuccess(req.getUsername(), request);
            return ResponseEntity.ok(Map.of("code", 0, "message", "OK", "data", data));
        } catch (Exception e) {
            securityAuditService.logLoginFailure(req.getUsername(), request, e.getMessage());
            throw e;
        }
    }

    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsername(@RequestParam String username) {
        try {
            boolean isAvailable = authService.isUsernameAvailable(username);
            return ResponseEntity.ok(Map.of(
                "code", 0, 
                "message", "OK", 
                "data", Map.of("available", isAvailable)
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "code", 1, 
                "message", e.getMessage(), 
                "data", Map.of("available", false)
            ));
        }
    }

    @PostMapping("/hic-verify")
    public ResponseEntity<?> hicVerify(Authentication authentication, @RequestBody @Valid AuthDtos.HicVerifyReq req, HttpServletRequest request) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of("code", 401, "message", "未登录，无法进行认证"));
        }
        String username = authentication.getName();
        try {
            boolean ok = authService.verifyHic(username, req.getKey());
            if (ok) {
                return ResponseEntity.ok(Map.of("code", 0, "message", "认证成功", "data", Map.of("hic", 1)));
            } else {
                return ResponseEntity.status(400).body(Map.of("code", 400, "message", "认证密钥错误"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("code", 500, "message", "服务器错误，请稍后重试"));
        }
    }
}


