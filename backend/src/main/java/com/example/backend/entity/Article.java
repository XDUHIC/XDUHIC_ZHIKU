package com.example.backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Article {
    private Long id;
    private String title;
    private String summary;
    private String coverImage;
    private Integer viewCount;
    private String linkUrl;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime publishTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;

    /** 来源：undergrad-本科，postgrad-研究生 */
    private String source;
}



