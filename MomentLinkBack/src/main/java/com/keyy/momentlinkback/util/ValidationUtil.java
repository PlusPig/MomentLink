package com.keyy.momentlinkback.util;

import com.keyy.momentlinkback.exception.BadRequestException;

import java.util.regex.Pattern;

public class ValidationUtil {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{4,20}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-zA-Z])(?=.*\\d).{6,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    /**
     * 验证用户名格式
     */
    public static void validateUsername(String username) {
        if (username == null || username.isEmpty()) {
            throw new BadRequestException("用户名不能为空");
        }
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new BadRequestException("用户名必须是4-20位的字母、数字或下划线");
        }
    }

    /**
     * 验证密码格式
     */
    public static void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new BadRequestException("密码不能为空");
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new BadRequestException("密码必须是6-20位,且包含字母和数字");
        }
    }

    /**
     * 验证邮箱格式
     */
    public static void validateEmail(String email) {
        if (email != null && !email.isEmpty() && !EMAIL_PATTERN.matcher(email).matches()) {
            throw new BadRequestException("邮箱格式不正确");
        }
    }

    /**
     * 验证手机号格式
     */
    public static void validatePhone(String phone) {
        if (phone != null && !phone.isEmpty() && !PHONE_PATTERN.matcher(phone).matches()) {
            throw new BadRequestException("手机号格式不正确");
        }
    }

    /**
     * 验证评分范围
     */
    public static void validateRating(int score) {
        if (score < 1 || score > 5) {
            throw new BadRequestException("评分必须在1-5之间");
        }
    }

    /**
     * 验证分页参数
     */
    public static void validatePageParams(int page, int size) {
        if (page < 1) {
            throw new BadRequestException("页码必须大于0");
        }
        if (size < 1 || size > 100) {
            throw new BadRequestException("每页数量必须在1-100之间");
        }
    }

    /**
     * 验证内容文本长度
     */
    public static void validateContentText(String text, int maxLength) {
        if (text != null && text.length() > maxLength) {
            throw new BadRequestException("内容长度不能超过" + maxLength + "字");
        }
    }
}
