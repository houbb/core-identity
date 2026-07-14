package com.coreplatform.common.exception;

/**
 * 业务异常 — 所有 core-* 模块统一使用的异常类型。
 *
 * <p>接受 {@link ErrorCode} 接口，使各模块的自定义错误码枚举可以直接传入。</p>
 */
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}