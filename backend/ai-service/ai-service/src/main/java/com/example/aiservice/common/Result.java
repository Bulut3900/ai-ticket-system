package com.example.aiservice.common;

import lombok.Data;

/**
 * 统一返回结果
 */
@Data
public class Result<T> {

    private Integer code;     // 状态码：200成功，其他失败
    private String message;   // 提示信息
    private T data;           // 返回数据

    public Result() {
    }

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // 成功，带数据
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    // 成功，不带数据
    public static <T> Result<T> success() {
        return new Result<>(200, "success", null);
    }

    // 失败
    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }

    // 失败，自定义状态码
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }
}