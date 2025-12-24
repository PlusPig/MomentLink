package com.keyy.momentlinkback.service;

import com.keyy.momentlinkback.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${file.max-size:104857600}") // 100MB
    private long maxSize;

    public String storeFile(MultipartFile file, String folder) {
        // 验证文件
        validateFile(file);

        // 获取原始文件名
        String originalFilename = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "");

        try {
            // 检查文件名
            if (originalFilename.contains("..")) {
                throw new BadRequestException("文件名包含非法字符");
            }

            // 生成新文件名
            String extension = getFileExtension(originalFilename);
            String newFilename = UUID.randomUUID().toString() + extension;

            // 创建目录
            Path targetLocation = Paths.get(uploadDir, folder, newFilename);
            Files.createDirectories(targetLocation.getParent());

            // 保存文件
            Files.copy(file.getInputStream(), targetLocation,
                    StandardCopyOption.REPLACE_EXISTING);

            log.info("文件保存成功: {}", targetLocation);

            // 返回相对路径
            return "/uploads/" + folder + "/" + newFilename;

        } catch (IOException ex) {
            log.error("文件保存失败", ex);
            throw new BadRequestException("文件保存失败: " + ex.getMessage());
        }
    }

    public void deleteFile(String filePath) {
        try {
            Path path = Paths.get(uploadDir, filePath.replace("/uploads/", ""));
            Files.deleteIfExists(path);
            log.info("文件删除成功: {}", path);
        } catch (IOException ex) {
            log.error("文件删除失败", ex);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("文件不能为空");
        }

        if (file.getSize() > maxSize) {
            throw new BadRequestException("文件大小超过限制");
        }

        String contentType = file.getContentType();
        if (contentType == null ||
                (!contentType.startsWith("image/") &&
                        !contentType.startsWith("video/"))) {
            throw new BadRequestException("只支持图片和视频文件");
        }
    }

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex > 0 ? filename.substring(dotIndex) : "";
    }
}
