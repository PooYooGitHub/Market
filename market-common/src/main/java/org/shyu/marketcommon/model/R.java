package org.shyu.marketcommon.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结果封装类
 */
@Data
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 时间戳
     */
    private Long timestamp;

    public R() {
        this.timestamp = System.currentTimeMillis();
    }

    public R(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 成功响应（无数据）
     */
    public static <T> R<T> success() {
        return new R<>(200, "成功", null);
    }

    /**
     * 成功响应（有数据）
     */
    public static <T> R<T> success(T data) {
        return new R<>(200, "成功", data);
    }

    /**
     * 成功响应（自定义消息）
     */
    public static <T> R<T> success(String message, T data) {
        return new R<>(200, message, data);
    }

    /**
     * 失败响应
     */
    public static <T> R<T> error(String message) {
        return new R<>(500, message, null);
    }

    /**
     * 失败响应（自定义错误码）
     */
    public static <T> R<T> error(Integer code, String message) {
        return new R<>(code, message, null);
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return this.code != null && this.code == 200;
    }
}