package com.coreplatform.identity.exception;

public enum ErrorCode {
    // User errors (1xxxx)
    USER_NOT_FOUND(10001, "用户不存在"),
    USERNAME_ALREADY_EXISTS(10002, "用户名已存在"),
    EMAIL_ALREADY_EXISTS(10003, "邮箱已存在"),
    USER_ACCOUNT_NOT_FOUND(10004, "用户账号不存在"),

    // Auth errors (2xxxx)
    AUTH_INVALID_CREDENTIALS(20001, "用户名或密码错误"),
    AUTH_TOKEN_EXPIRED(20002, "令牌已过期"),
    AUTH_TOKEN_INVALID(20003, "无效的令牌"),
    AUTH_REFRESH_TOKEN_EXPIRED(20004, "刷新令牌已过期"),
    AUTH_REFRESH_TOKEN_REVOKED(20005, "刷新令牌已被撤销"),
    AUTH_ACCOUNT_DISABLED(20006, "账号已被禁用"),
    AUTH_ACCOUNT_LOCKED(20007, "账号已被锁定"),

    // Validation errors (3xxxx)
    VALIDATION_ERROR(30001, "参数校验失败"),
    PASSWORD_TOO_WEAK(30002, "密码强度不足"),

    // System errors (9xxxx)
    SYSTEM_ERROR(90001, "系统内部错误");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() { return code; }
    public String getMessage() { return message; }
}