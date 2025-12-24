package com.keyy.momentlinkback.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.keyy.momentlinkback.common.Result;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
public class ResponseUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 向响应写入JSON数据
     */
    public static void writeJson(HttpServletResponse response, Result<?> result) {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(result.getCode());

        try (PrintWriter writer = response.getWriter()) {
            writer.write(objectMapper.writeValueAsString(result));
            writer.flush();
        } catch (IOException e) {
            log.error("写入响应失败", e);
        }
    }

    /**
     * 写入成功响应
     */
    public static void writeSuccess(HttpServletResponse response, String message) {
        writeJson(response, Result.success(message));
    }

    /**
     * 写入错误响应
     */
    public static void writeError(HttpServletResponse response, int code, String message) {
        writeJson(response, Result.error(code, message));
    }

    /**
     * 写入未授权响应
     */
    public static void writeUnauthorized(HttpServletResponse response, String message) {
        writeError(response, 401, message);
    }

    /**
     * 写入禁止访问响应
     */
    public static void writeForbidden(HttpServletResponse response, String message) {
        writeError(response, 403, message);
    }
}
