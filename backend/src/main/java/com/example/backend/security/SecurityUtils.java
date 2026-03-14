package com.example.backend.security;

import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("securityUtils")
public class SecurityUtils {
    private static final Logger logger = LoggerFactory.getLogger(SecurityUtils.class);

    @Autowired
    private UserRepository userRepository;

    public boolean isHicVerified(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.info("Authentication is null or not authenticated");
            return false;
        }
        String username = authentication.getName();
        logger.info("Checking HIC verification for user: {}", username);
        try {
            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                logger.warn("User not found: {}", username);
                return false;
            }
            Integer hic = user.getHic();
            logger.info("User {} hic value: {}", username, hic);
            return hic != null && hic == 1;
        } catch (Exception e) {
            logger.error("Error while checking HIC verification for user: {}", username, e);
            // 返回 false 而不是让异常继续传播，避免 500
            return false;
        }
    }
}
