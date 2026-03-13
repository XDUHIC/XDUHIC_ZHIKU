package com.example.backend.controller;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.AuthDtos;
import com.example.backend.exception.BusinessException;
import com.example.backend.service.AuthService;
import com.example.backend.service.SecurityAuditService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;
    private final SecurityAuditService securityAuditService;



    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid AuthDtos.RegisterReq req, HttpServletRequest request) {
        try {
            if (!verifyStudent(req.getStudentId())) {
                // 学号验证失败，直接返回错误
                securityAuditService.logSuspiciousActivity("REGISTRATION_FAILURE",
                        "学号验证失败: " + req.getStudentId(), request);

                return ResponseEntity.badRequest().body(Map.of(
                        "code", 400,
                        "message", "学号格式不正确，请检查后重试"
                ));
            }
            authService.register(req);
            securityAuditService.logRegistration(req.getUsername(), request);
            return ResponseEntity.ok(Map.of("code", 0, "message", "OK"));
        } catch (Exception e) {
            securityAuditService.logSuspiciousActivity("REGISTRATION_FAILURE",
                    "Registration failed for username: " + req.getUsername(), request);
            if (e instanceof BusinessException) {
                log.warn("Register failed. username={}, studentId={}, reason={}",
                        req.getUsername(), req.getStudentId(), e.getMessage());
            } else {
                log.error("Register failed unexpectedly. username={}, studentId={}",
                        req.getUsername(), req.getStudentId(), e);
            }
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
            if (e instanceof BusinessException) {
                log.warn("Login failed. username={}, reason={}", req.getUsername(), e.getMessage());
            } else {
                log.error("Login failed unexpectedly. username={}", req.getUsername(), e);
            }
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

    //一个字母开头，然后有11位数字
    // 12位代表哪一届的，34位为所在院系，56位为专业代码，78位为班级，9~11位为个人代码
    private static final int MIN_YEAR = 17;
    private static final Set<String> VALID_COLLEGE_CODES = new HashSet<>();
    static {
        // 初始化学院代码
        VALID_COLLEGE_CODES.add("01"); // 通信工程学院
        VALID_COLLEGE_CODES.add("02"); // 电子工程学院
        VALID_COLLEGE_CODES.add("03"); // 计算机科学与技术学院
        VALID_COLLEGE_CODES.add("04"); // 机电工程学院
        VALID_COLLEGE_CODES.add("05"); // 物理学院
        VALID_COLLEGE_CODES.add("06"); // 光电工程学院
        VALID_COLLEGE_CODES.add("13"); // 空间科学与技术学院
        VALID_COLLEGE_CODES.add("14"); // 先进材料与纳米科技学院
        VALID_COLLEGE_CODES.add("15"); // 网络与信息安全学院
        VALID_COLLEGE_CODES.add("16"); // 人工智能学院
        VALID_COLLEGE_CODES.add("17"); // 信息力学与感知工程学院
        VALID_COLLEGE_CODES.add("18"); // 体育部
        VALID_COLLEGE_CODES.add("30"); // 额外代码1
        VALID_COLLEGE_CODES.add("71"); // 额外代码2
        VALID_COLLEGE_CODES.add("72"); // 额外代码3
        VALID_COLLEGE_CODES.add("81"); // 额外代码4
        VALID_COLLEGE_CODES.add("82"); // 额外代码5
    }

    public boolean verifyStudent(String studentId) {
        // 0. 去除可能的空格
        studentId = studentId.trim();
        // 2. 检查长度是否为11位
        if (studentId.length() != 11) {
            log.warn("学号长度不正确: {} (期望11位)", studentId.length());
            return false;
        }
        // 3. 检查是否全为数字
        if (!studentId.matches("\\d{11}")) {
            log.warn("学号包含非数字字符: {}", studentId);
            return false;
        }
        try {
            // 4. 验证入学年份（前4位）
            String yearStr = studentId.substring(0, 2);
            int year = Integer.parseInt(yearStr);
            int currentYear = LocalDate.now().getYear()%100;

            // 1.检查年份范围：最小年份 到 当前年份+1（允许预录取）
            if (year < MIN_YEAR || year > currentYear + 1) {
                log.warn("入学年份超出范围: {} (允许范围: {}-{})",
                        year, MIN_YEAR, currentYear + 1);
                return false;
            }
            //2.校验三四位：院系
            // 6. 验证学院代码
            if (!VALID_COLLEGE_CODES.contains(studentId.substring(2,4))) {
                return false;
            }
        }catch (NumberFormatException e) {
            log.error("学号解析失败: {}", studentId, e);
            return false;
        } catch (Exception e) {
            log.error("学号验证异常: {}", studentId, e);
            return false;
        }
        return true;
    }








//    @PostMapping("/register")
//    public ResponseEntity<?> register(@RequestBody @Valid AuthDtos.RegisterReq req, HttpServletRequest request) {
//        try {
//            authService.register(req);
//            securityAuditService.logRegistration(req.getUsername(), request);
//            return ResponseEntity.ok(Map.of("code", 0, "message", "OK"));
//        } catch (Exception e) {
//            securityAuditService.logSuspiciousActivity("REGISTRATION_FAILURE",
//                "Registration failed for username: " + req.getUsername(), request);
//            throw e;
//        }
//    }
//
//    @PostMapping("/login")
//    public ResponseEntity<?> login(@RequestBody @Valid AuthDtos.LoginReq req, HttpServletRequest request) {
//        try {
//            Map<String, Object> data = authService.login(req);
//            securityAuditService.logLoginSuccess(req.getUsername(), request);
//            return ResponseEntity.ok(Map.of("code", 0, "message", "OK", "data", data));
//        } catch (Exception e) {
//            securityAuditService.logLoginFailure(req.getUsername(), request, e.getMessage());
//            throw e;
//        }
//    }
//
//    @GetMapping("/check-username")
//    public ResponseEntity<?> checkUsername(@RequestParam String username) {
//        try {
//            boolean isAvailable = authService.isUsernameAvailable(username);
//            return ResponseEntity.ok(Map.of(
//                "code", 0,
//                "message", "OK",
//                "data", Map.of("available", isAvailable)
//            ));
//        } catch (Exception e) {
//            return ResponseEntity.ok(Map.of(
//                "code", 1,
//                "message", e.getMessage(),
//                "data", Map.of("available", false)
//            ));
//        }
//    }

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


