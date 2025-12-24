package com.keyy.momentlinkback.exception;

import com.keyy.momentlinkback.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<?> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.error("资源未找到异常: {}", ex.getMessage());
        return Result.error(404, ex.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleBadRequestException(BadRequestException ex) {
        log.error("请求参数错误: {}", ex.getMessage());
        return Result.error(400, ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<?> handleUnauthorizedException(UnauthorizedException ex) {
        log.error("未授权访问: {}", ex.getMessage());
        return Result.error(401, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        StringBuilder errorMessage = new StringBuilder("参数校验失败：");
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(fieldName, message);
            errorMessage.append(fieldName).append(" ").append(message).append("; ");
        });
        log.error("参数校验失败: {}", errors);
        // 返回详细的错误信息，将字段错误合并到 message 中
        String finalMessage = errorMessage.toString().trim();
        if (finalMessage.endsWith(";")) {
            finalMessage = finalMessage.substring(0, finalMessage.length() - 1);
        }
        return Result.error(400, finalMessage, errors);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleException(Exception ex) {
        log.error("系统异常", ex);
        return Result.error(500, "系统异常: " + ex.getMessage());
    }
}