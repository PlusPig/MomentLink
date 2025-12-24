package com.keyy.momentlinkback.common;

public class Constants {

    // 用户状态
    public static final int USER_STATUS_DISABLED = 0;
    public static final int USER_STATUS_NORMAL = 1;

    // 用户角色
    public static final int USER_ROLE_USER = 0;
    public static final int USER_ROLE_ADMIN = 1;

    // 内容类型
    public static final int CONTENT_TYPE_IMAGE = 1;
    public static final int CONTENT_TYPE_VIDEO = 2;
    public static final int CONTENT_TYPE_TEXT = 3;

    // 内容状态
    public static final int CONTENT_STATUS_DELETED = 0;
    public static final int CONTENT_STATUS_NORMAL = 1;
    public static final int CONTENT_STATUS_VIOLATION = 2;

    // 好友状态
    public static final int FRIEND_STATUS_PENDING = 0;
    public static final int FRIEND_STATUS_ACCEPTED = 1;
    public static final int FRIEND_STATUS_REJECTED = 2;
    public static final int FRIEND_STATUS_BLOCKED = 3;

    // 通知类型
    public static final int NOTIFICATION_TYPE_FRIEND_REQUEST = 1;
    public static final int NOTIFICATION_TYPE_LIKE = 2;
    public static final int NOTIFICATION_TYPE_COMMENT = 3;
    public static final int NOTIFICATION_TYPE_SYSTEM = 4;

    // 通知状态
    public static final int NOTIFICATION_STATUS_UNREAD = 0;
    public static final int NOTIFICATION_STATUS_READ = 1;

    // 敏感词级别
    public static final int SENSITIVE_WORD_LEVEL_LOW = 1;
    public static final int SENSITIVE_WORD_LEVEL_MEDIUM = 2;
    public static final int SENSITIVE_WORD_LEVEL_HIGH = 3;

    // 文件相关
    public static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB
    public static final String[] ALLOWED_IMAGE_TYPES = {"jpg", "jpeg", "png", "gif", "webp"};
    public static final String[] ALLOWED_VIDEO_TYPES = {"mp4", "avi", "mov", "wmv", "flv"};

    // 分页默认值
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    // JWT相关
    public static final String JWT_HEADER = "Authorization";
    public static final String JWT_PREFIX = "Bearer ";
    public static final long JWT_EXPIRATION = 7 * 24 * 60 * 60 * 1000; // 7天

    // Redis Key前缀
    public static final String REDIS_KEY_USER = "user:";
    public static final String REDIS_KEY_CONTENT = "content:";
    public static final String REDIS_KEY_TOKEN = "token:";
    public static final String REDIS_KEY_CAPTCHA = "captcha:";

    // 缓存过期时间(秒)
    public static final int CACHE_EXPIRE_HOUR = 60 * 60;
    public static final int CACHE_EXPIRE_DAY = 24 * 60 * 60;
    public static final int CACHE_EXPIRE_WEEK = 7 * 24 * 60 * 60;
}
