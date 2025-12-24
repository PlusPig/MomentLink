-- 用户表
CREATE TABLE `user` (
                        `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
                        `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
                        `password` VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
                        `nickname` VARCHAR(50) COMMENT '昵称',
                        `email` VARCHAR(100) UNIQUE COMMENT '邮箱',
                        `avatar` VARCHAR(255) COMMENT '头像URL',
                        `gender` TINYINT DEFAULT 0 COMMENT '性别:0未知,1男,2女',
                        `signature` VARCHAR(200) COMMENT '个性签名',
                        `status` TINYINT DEFAULT 1 COMMENT '状态:0禁用,1正常',
                        `role` TINYINT DEFAULT 0 COMMENT '角色:0普通用户,1管理员',
                        `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除:0未删除,1已删除',
                        INDEX `idx_username` (`username`),
                        INDEX `idx_email` (`email`),
                        INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 内容表
CREATE TABLE `content` (
                           `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '内容ID',
                           `user_id` BIGINT NOT NULL COMMENT '发布者ID',
                           `type` TINYINT NOT NULL COMMENT '类型:1图片,2视频,3文本',
                           `content_text` TEXT COMMENT '文本内容',
                           `media_urls` JSON COMMENT '媒体文件URLs(图片/视频)',
                           `tags` VARCHAR(500) COMMENT '标签,逗号分隔',
                           `view_count` INT DEFAULT 0 COMMENT '浏览次数',
                           `like_count` INT DEFAULT 0 COMMENT '点赞数',
                           `comment_count` INT DEFAULT 0 COMMENT '评论数',
                           `avg_rating` DECIMAL(3,2) DEFAULT 0.00 COMMENT '平均评分',
                           `rating_count` INT DEFAULT 0 COMMENT '评分人数',
                           `status` TINYINT DEFAULT 1 COMMENT '状态:0已删除,1正常,2违规',
                           `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
                           INDEX `idx_user_id` (`user_id`),
                           INDEX `idx_type` (`type`),
                           INDEX `idx_created_at` (`created_at`),
                           INDEX `idx_status` (`status`),
                           FULLTEXT INDEX `idx_tags` (`tags`),
                           FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容表';

-- 评分表
CREATE TABLE `rating` (
                          `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评分ID',
                          `content_id` BIGINT NOT NULL COMMENT '内容ID',
                          `user_id` BIGINT NOT NULL COMMENT '评分用户ID',
                          `score` TINYINT NOT NULL COMMENT '评分:1-5',
                          `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          UNIQUE KEY `uk_content_user` (`content_id`, `user_id`),
                          INDEX `idx_content_id` (`content_id`),
                          INDEX `idx_user_id` (`user_id`),
                          FOREIGN KEY (`content_id`) REFERENCES `content`(`id`) ON DELETE CASCADE,
                          FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评分表';

-- 评论表
CREATE TABLE `comment` (
                           `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评论ID',
                           `content_id` BIGINT NOT NULL COMMENT '内容ID',
                           `user_id` BIGINT NOT NULL COMMENT '评论用户ID',
                           `parent_id` BIGINT DEFAULT 0 COMMENT '父评论ID,0表示顶级评论',
                           `reply_to_user_id` BIGINT COMMENT '回复的用户ID',
                           `comment_text` TEXT NOT NULL COMMENT '评论内容',
                           `like_count` INT DEFAULT 0 COMMENT '点赞数',
                           `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
                           INDEX `idx_content_id` (`content_id`),
                           INDEX `idx_user_id` (`user_id`),
                           INDEX `idx_parent_id` (`parent_id`),
                           FOREIGN KEY (`content_id`) REFERENCES `content`(`id`) ON DELETE CASCADE,
                           FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- 好友关系表
CREATE TABLE `friendship` (
                              `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '好友关系ID',
                              `user_id` BIGINT NOT NULL COMMENT '用户ID',
                              `friend_id` BIGINT NOT NULL COMMENT '好友ID',
                              `remark` VARCHAR(50) COMMENT '好友备注',
                              `status` TINYINT DEFAULT 0 COMMENT '状态:0待接受,1已接受,2已拒绝,3已屏蔽',
                              `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                              UNIQUE KEY `uk_user_friend` (`user_id`, `friend_id`),
                              INDEX `idx_user_id` (`user_id`),
                              INDEX `idx_friend_id` (`friend_id`),
                              INDEX `idx_status` (`status`),
                              FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
                              FOREIGN KEY (`friend_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='好友关系表';

-- 通知表
CREATE TABLE `notification` (
                                `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '通知ID',
                                `user_id` BIGINT NOT NULL COMMENT '接收通知的用户ID',
                                `type` TINYINT NOT NULL COMMENT '类型:1好友申请,2点赞,3评论,4系统通知',
                                `sender_id` BIGINT COMMENT '发送者ID',
                                `content_id` BIGINT COMMENT '关联内容ID',
                                `related_id` BIGINT COMMENT '关联ID，用于存储friendshipId等',
                                `message` VARCHAR(500) COMMENT '通知消息',
                                `status` TINYINT DEFAULT 0 COMMENT '状态:0未读,1已读',
                                `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                INDEX `idx_user_id` (`user_id`),
                                INDEX `idx_status` (`status`),
                                INDEX `idx_created_at` (`created_at`),
                                FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- 点赞表
CREATE TABLE `like_record` (
                               `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '点赞ID',
                               `user_id` BIGINT NOT NULL COMMENT '点赞用户ID',
                               `content_id` BIGINT NOT NULL COMMENT '内容ID',
                               `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               UNIQUE KEY `uk_user_content` (`user_id`, `content_id`),
                               INDEX `idx_content_id` (`content_id`),
                               FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
                               FOREIGN KEY (`content_id`) REFERENCES `content`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞记录表';

-- 敏感词表
CREATE TABLE `sensitive_word` (
                                  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '敏感词ID',
                                  `word` VARCHAR(100) NOT NULL UNIQUE COMMENT '敏感词',
                                  `level` TINYINT DEFAULT 1 COMMENT '级别:1轻微,2中等,3严重',
                                  `status` TINYINT DEFAULT 1 COMMENT '状态:0禁用,1启用',
                                  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  INDEX `idx_word` (`word`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='敏感词表';
