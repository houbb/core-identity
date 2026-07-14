package com.coreplatform.common.exception;

/**
 * 错误码接口 — 所有模块的错误码枚举必须实现此接口。
 *
 * <p>core-common 提供 {@link CommonErrorCode} 通用错误码，
 * 各业务模块（如 core-identity）通过实现此接口定义自己的业务错误码。</p>
 *
 * <pre>
 * public enum IdentityErrorCode implements ErrorCode {
 *     USER_NOT_FOUND(10001, "用户不存在"),
 *     AUTH_INVALID_CREDENTIALS(20001, "用户名或密码错误");
 *     // ...
 * }
 * </pre>
 */
public interface ErrorCode {

    int getCode();

    String getMessage();
}