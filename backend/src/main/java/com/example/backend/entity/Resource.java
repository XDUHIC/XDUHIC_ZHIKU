package com.example.backend.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Resource {
    private Long id;
    private String title;
    private String description;
    private String category; // software, hardware, embed, ai, other
    private String type; // document, video, project, other
    private String resourceUrl;
    private Integer eyeCount;
    private Integer hic; // HIC认证要求：0-不需要认证，1-需要认证

    private LocalDateTime createdAt;
}