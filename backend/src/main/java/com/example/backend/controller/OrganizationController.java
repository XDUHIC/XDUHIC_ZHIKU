package com.example.backend.controller;

import com.example.backend.entity.OrganizationData;
import com.example.backend.repository.OrganizationDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api")
public class OrganizationController {

    @Autowired
    private OrganizationDataRepository repository;

    @GetMapping("/organization")
    @PreAuthorize("isAuthenticated() and @securityUtils.isHicVerified(authentication)")
    public ResponseEntity<String> getOrganization() {
        OrganizationData latest = repository.findLatest();
        if (latest != null) {
            // 直接返回 JSON 字符串（存储在 data 字段中）
            return ResponseEntity.ok(latest.getData());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
