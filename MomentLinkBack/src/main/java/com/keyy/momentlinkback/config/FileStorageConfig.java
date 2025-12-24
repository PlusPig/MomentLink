package com.keyy.momentlinkback.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "file")
public class FileStorageConfig {

    private String uploadDir = "./uploads";
    private long maxSize = 104857600L; // 100MB
    private List<String> allowedImageTypes = new ArrayList<>();
    private List<String> allowedVideoTypes = new ArrayList<>();

    public FileStorageConfig() {
        // 允许的图片类型
        allowedImageTypes.add("image/jpeg");
        allowedImageTypes.add("image/jpg");
        allowedImageTypes.add("image/png");
        allowedImageTypes.add("image/gif");
        allowedImageTypes.add("image/webp");

        // 允许的视频类型
        allowedVideoTypes.add("video/mp4");
        allowedVideoTypes.add("video/avi");
        allowedVideoTypes.add("video/mov");
        allowedVideoTypes.add("video/wmv");
        allowedVideoTypes.add("video/flv");
    }

    public boolean isImageTypeAllowed(String contentType) {
        return contentType != null && allowedImageTypes.contains(contentType.toLowerCase());
    }

    public boolean isVideoTypeAllowed(String contentType) {
        return contentType != null && allowedVideoTypes.contains(contentType.toLowerCase());
    }
}
