package com.coreplatform.common.exception;

/**
 * 平台通用错误码 — 仅包含所有 core-* 模块共用的错误码。
 * 业务模块应定义自己的错误码枚举，实现 {@link ErrorCode} 接口。
 */
public enum CommonErrorCode implements ErrorCode {

    VALIDATION_ERROR(30001, "参数校验失败"),
    SYSTEM_ERROR(90001, "系统内部错误");

    private final int code;
    private final String message;

    CommonErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}