package com.example.aiservice.common;

/**
 * 自定义业务异常
 * 业务出错时直接 throw new BusinessException("xxx")，会被全局异常处理器捕获
 */
public class BusinessException extends RuntimeException {

    private Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}