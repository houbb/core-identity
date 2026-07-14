package com.github.houbb.core.identity.api.exception;

import com.github.houbb.core.identity.api.response.ErrorResponse;
import com.github.houbb.core.identity.application.service.InternalTokenServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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