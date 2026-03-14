package com.example.backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@MapperScan("com.example.backend.mapper")
@EnableTransactionManagement
public class BackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @Bean
    public CommandLineRunner dbMigrate(JdbcTemplate jdbc) {
        return args -> {
            try {
                jdbc.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS college VARCHAR(100)");
            } catch (Exception ignored) {
            }
            try {
                jdbc.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS bio TEXT");
            } catch (Exception ignored) {
            }
            try {
                jdbc.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS hic TINYINT DEFAULT 0 COMMENT 'HIC认证状态：0-未认证，1-已认证'");
            } catch (Exception ignored) {
            }
            // articles.source：兼容 MySQL 5.7（无 IF NOT EXISTS 时先查 information_schema）
            try {
                Integer hasSource = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'articles' AND COLUMN_NAME = 'source'",
                        Integer.class);
                if (hasSource == null || hasSource == 0) {
                    jdbc.execute("ALTER TABLE articles ADD COLUMN source VARCHAR(20) DEFAULT NULL COMMENT 'undergrad/postgrad'");
                }
            } catch (Exception e) {
                try {
                    jdbc.execute("ALTER TABLE articles ADD COLUMN IF NOT EXISTS source VARCHAR(20) DEFAULT NULL COMMENT 'undergrad/postgrad'");
                } catch (Exception ignored) {
                }
            }
            try {
                jdbc.execute("CREATE TABLE IF NOT EXISTS comments (" +
                        "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                        "resource_id BIGINT NOT NULL," +
                        "user_id BIGINT NOT NULL," +
                        "parent_id BIGINT DEFAULT NULL," +
                        "content TEXT NOT NULL," +
                        "created_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
                        "KEY idx_resource_id (resource_id)," +
                        "KEY idx_user_id (user_id)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资源评论表'");
            } catch (Exception ignored) {
            }
            // resources.content_url：兼容 MySQL 5.7
            try {
                Integer hasContentUrl = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'resources' AND COLUMN_NAME = 'content_url'",
                        Integer.class);
                if (hasContentUrl == null || hasContentUrl == 0) {
                    jdbc.execute("ALTER TABLE resources ADD COLUMN content_url VARCHAR(500) DEFAULT NULL COMMENT 'Markdown内容路径'");
                }
            } catch (Exception e) {
                try {
                    jdbc.execute("ALTER TABLE resources ADD COLUMN IF NOT EXISTS content_url VARCHAR(500) DEFAULT NULL COMMENT 'Markdown内容路径'");
                } catch (Exception ignored) {
                }
            }
            // resources.source：本科/研究生标签（undergrad/postgrad）
            try {
                Integer hasResourceSource = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'resources' AND COLUMN_NAME = 'source'",
                        Integer.class);
                if (hasResourceSource == null || hasResourceSource == 0) {
                    jdbc.execute("ALTER TABLE resources ADD COLUMN source VARCHAR(20) DEFAULT NULL COMMENT 'undergrad-本科/postgrad-研究生'");
                }
            } catch (Exception e) {
                try {
                    jdbc.execute("ALTER TABLE resources ADD COLUMN IF NOT EXISTS source VARCHAR(20) DEFAULT NULL COMMENT 'undergrad-本科/postgrad-研究生'");
                } catch (Exception ignored) {
                }
            }
            try {
                jdbc.execute("CREATE TABLE IF NOT EXISTS points (" +
                        "user_id BIGINT NOT NULL PRIMARY KEY," +
                        "balance INT NOT NULL DEFAULT 0," +
                        "updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                        "KEY idx_balance (balance)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户积分余额表'");
            } catch (Exception ignored) {
            }
            try {
                jdbc.execute("CREATE TABLE IF NOT EXISTS point_logs (" +
                        "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                        "user_id BIGINT NOT NULL," +
                        "amount INT NOT NULL," +
                        "reason VARCHAR(200) DEFAULT NULL," +
                        "ref_type VARCHAR(64) DEFAULT NULL," +
                        "ref_id BIGINT DEFAULT NULL," +
                        "created_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
                        "KEY idx_user_id (user_id)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分流水表'");
            } catch (Exception ignored) {
            }
            try {
                jdbc.execute("CREATE TABLE IF NOT EXISTS resource_likes (" +
                        "resource_id BIGINT NOT NULL," +
                        "user_id BIGINT NOT NULL," +
                        "created_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
                        "PRIMARY KEY (resource_id, user_id)," +
                        "KEY idx_resource_id (resource_id)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资源点赞表'");
            } catch (Exception ignored) {
            }
            // shares 表扩展：tags、author_id
            try {
                Integer hasTags = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'shares' AND COLUMN_NAME = 'tags'", Integer.class);
                if (hasTags == null || hasTags == 0)
                    jdbc.execute("ALTER TABLE shares ADD COLUMN tags VARCHAR(255) DEFAULT NULL COMMENT '标签，逗号分隔'");
            } catch (Exception ignored) {
            }
            try {
                Integer hasAuthorId = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'shares' AND COLUMN_NAME = 'author_id'", Integer.class);
                if (hasAuthorId == null || hasAuthorId == 0)
                    jdbc.execute("ALTER TABLE shares ADD COLUMN author_id BIGINT DEFAULT NULL COMMENT '作者ID'");
            } catch (Exception ignored) {
            }
            // 答疑解惑：问题表
            try {
                jdbc.execute("CREATE TABLE IF NOT EXISTS qna_questions (" +
                        "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                        "user_id BIGINT NOT NULL COMMENT '提问者ID'," +
                        "title VARCHAR(300) NOT NULL COMMENT '问题标题'," +
                        "content TEXT NOT NULL COMMENT '问题内容'," +
                        "tags VARCHAR(255) DEFAULT NULL COMMENT '标签，逗号分隔'," +
                        "view_count INT DEFAULT 0," +
                        "answer_count INT DEFAULT 0," +
                        "accepted_answer_id BIGINT DEFAULT NULL COMMENT '采纳的回答ID'," +
                        "target_type VARCHAR(30) DEFAULT NULL COMMENT '讨论挂载类型，如share/project'," +
                        "target_id BIGINT DEFAULT NULL COMMENT '讨论挂载对象ID'," +
                        "created_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
                        "KEY idx_user_id (user_id)," +
                        "KEY idx_created_at (created_at)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='答疑解惑-问题表'");
            } catch (Exception ignored) {
            }
            // qna_questions 目标挂载字段（用于将讨论绑定到分享/项目等）
            try {
                Integer hasTargetType = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qna_questions' AND COLUMN_NAME = 'target_type'",
                        Integer.class);
                if (hasTargetType == null || hasTargetType == 0) {
                    jdbc.execute("ALTER TABLE qna_questions ADD COLUMN target_type VARCHAR(30) DEFAULT NULL COMMENT '讨论挂载类型，如share/project'");
                }
            } catch (Exception ignored) {
            }
            try {
                Integer hasTargetId = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qna_questions' AND COLUMN_NAME = 'target_id'",
                        Integer.class);
                if (hasTargetId == null || hasTargetId == 0) {
                    jdbc.execute("ALTER TABLE qna_questions ADD COLUMN target_id BIGINT DEFAULT NULL COMMENT '讨论挂载对象ID'");
                }
            } catch (Exception ignored) {
            }
            // 答疑解惑：回答表
            try {
                jdbc.execute("CREATE TABLE IF NOT EXISTS qna_answers (" +
                        "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                        "question_id BIGINT NOT NULL," +
                        "user_id BIGINT NOT NULL," +
                        "content TEXT NOT NULL," +
                        "like_count INT DEFAULT 0," +
                        "is_accepted TINYINT DEFAULT 0 COMMENT '是否被采纳'," +
                        "created_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
                        "KEY idx_question_id (question_id)," +
                        "KEY idx_user_id (user_id)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='答疑解惑-回答表'");
            } catch (Exception ignored) {
            }
            // 回答点赞表（用于积分奖励）
            try {
                jdbc.execute("CREATE TABLE IF NOT EXISTS qna_answer_likes (" +
                        "answer_id BIGINT NOT NULL," +
                        "user_id BIGINT NOT NULL," +
                        "created_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
                        "PRIMARY KEY (answer_id, user_id)," +
                        "KEY idx_answer_id (answer_id)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='回答点赞表'");
            } catch (Exception ignored) {
            }
        };
    }
}
