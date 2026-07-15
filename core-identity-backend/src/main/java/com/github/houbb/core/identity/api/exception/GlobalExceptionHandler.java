package com.github.houbb.core.identity.api.exception;

import com.github.houbb.core.identity.api.response.ErrorResponse;
import com.github.houbb.core.identity.application.service.AuthServiceImpl;
import com.github.houbb.core.identity.application.service.InternalTokenServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler returning RFC 7807 Problem Details.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InternalTokenServiceImpl.AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthException(InternalTokenServiceImpl.AuthenticationException e) {
        ErrorResponse error = ErrorResponse.of(
                401,
                "Authentication failed",
                e.getMessage(),
                "IDENTITY_INTERNAL_CLIENT_INVALID",
                ""
        );
        return ResponseEntity.status(401).body(error);
    }

    @ExceptionHandler(AuthServiceImpl.AuthException.class)
    public ResponseEntity<ErrorResponse> handleIdentityAuthException(AuthServiceImpl.AuthException e) {
        int status = switch (e.getErrorCode()) {
            case "IDENTITY_INVALID_CREDENTIALS" -> 401;
            case "IDENTITY_ACCOUNT_LOCKED", "IDENTITY_ACCOUNT_DISABLED" -> 403;
            case "IDENTITY_EMAIL_NOT_VERIFIED" -> 403;
            case "IDENTITY_EMAIL_VERIFICATION_INVALID" -> 400;
            case "IDENTITY_EMAIL_VERIFICATION_EXPIRED" -> 410;
            case "IDENTITY_EMAIL_ALREADY_REGISTERED" -> 409;
            case "IDENTITY_RATE_LIMITED" -> 429;
            case "IDENTITY_PASSWORD_RESET_INVALID" -> 400;
            case "IDENTITY_PASSWORD_RESET_EXPIRED" -> 410;
            case "IDENTITY_IDEMPOTENCY_CONFLICT" -> 409;
            case "IDENTITY_PASSWORD_MUST_CHANGE" -> 403;
            case "IDENTITY_INVALID_REQUEST" -> 400;
            case "IDENTITY_CURRENT_PASSWORD_INVALID" -> 400;
            case "IDENTITY_PASSWORD_POLICY_VIOLATION" -> 400;
            case "IDENTITY_USER_NOT_FOUND" -> 404;
            default -> 500;
        };
        ErrorResponse error = ErrorResponse.of(status, "Identity error", e.getMessage(), e.getErrorCode(), "");
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        String detail = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b).orElse("Validation failed");
        ErrorResponse error = ErrorResponse.of(400, "Validation error", detail, "IDENTITY_VALIDATION_ERROR", "");
        return ResponseEntity.status(400).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException e) {
        ErrorResponse error = ErrorResponse.of(
                400,
                "Bad request",
                e.getMessage(),
                "IDENTITY_CONFIGURATION_INVALID",
                ""
        );
        return ResponseEntity.status(400).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception e) {
        log.error("Unhandled exception", e);
        ErrorResponse error = ErrorResponse.of(
                500,
                "Internal error",
                "An unexpected error occurred",
                "IDENTITY_INTERNAL_ERROR",
                ""
        );
        return ResponseEntity.status(500).body(error);
    }
}