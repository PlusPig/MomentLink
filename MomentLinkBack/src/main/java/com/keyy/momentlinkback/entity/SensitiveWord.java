// SensitiveWord.java
package com.keyy.momentlinkback.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "sensitive_word")
public class SensitiveWord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String word;

    @Column(columnDefinition = "TINYINT DEFAULT 1")
    private Integer level = 1; // 1:轻微 2:中等 3:严重

    @Column(columnDefinition = "TINYINT DEFAULT 1")
    private Integer status = 1; // 0:禁用 1:启用

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}