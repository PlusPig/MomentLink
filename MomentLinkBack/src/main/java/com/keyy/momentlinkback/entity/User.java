package com.keyy.momentlinkback.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "`user`")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(length = 50)
    private String nickname;

    @Column(unique = true, length = 100)
    private String email;

    private String avatar;

    @Column(columnDefinition = "TINYINT DEFAULT 0")
    private Integer gender; // 0:未知 1:男 2:女

    @Column(length = 200)
    private String signature;

    @Column(columnDefinition = "TINYINT DEFAULT 1")
    private Integer status; // 0:禁用 1:正常

    @Column(columnDefinition = "TINYINT DEFAULT 0")
    private Integer role; // 0:普通用户 1:管理员

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(columnDefinition = "TINYINT DEFAULT 0")
    private Integer deleted; // 逻辑删除

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
