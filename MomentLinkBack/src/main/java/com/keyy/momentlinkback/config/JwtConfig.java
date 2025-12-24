package com.keyy.momentlinkback.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
    private String secret = "YourSecretKeyMustBeLongEnoughForHS256Algorithm"; // 至少32字符
    private Long expiration = 86400000L; // 24小时(毫秒)
    private String header = "Authorization";
    private String prefix = "Bearer ";
}
