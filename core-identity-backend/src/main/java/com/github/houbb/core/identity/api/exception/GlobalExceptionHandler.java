package com.github.houbb.core.identity.api.exception;

import com.github.houbb.core.identity.api.response.ErrorResponse;
import com.github.houbb.core.identity.application.service.*;
import com.github.houbb.core.identity.application.service.AuthServiceImpl;
import com.github.houbb.core.identity.application.service.InternalTokenServiceImpl;
import com.github.houbb.core.identity.application.service.OAuthAuthorizationService;
import com.github.houbb.core.identity.application.service.OAuthClientService;
import com.github.houbb.core.identity.application.service.OAuthTokenService;
import com.github.houbb.core.identity.api.publicapi.controller.OrganizationController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

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
            case "IDENTITY_SESSION_INVALID" -> 401;
            default -> 500;
        };
        ErrorResponse error = ErrorResponse.of(status, "Identity error", e.getMessage(), e.getErrorCode(), "");
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(RoleServiceImpl.ServiceException.class)
    public ResponseEntity<ErrorResponse> handleRoleServiceException(RoleServiceImpl.ServiceException e) {
        int status = switch (e.getErrorCode()) {
            case "IDENTITY_ROLE_NOT_FOUND" -> 404;
            case "IDENTITY_ROLE_NAME_CONFLICT" -> 409;
            case "IDENTITY_ROLE_PROTECTED" -> 403;
            case "IDENTITY_ROLE_IN_USE" -> 409;
            case "IDENTITY_ROLE_ORGANIZATION_MISMATCH" -> 403;
            case "IDENTITY_PERMISSION_NOT_FOUND" -> 404;
            case "IDENTITY_PERMISSION_DEPRECATED" -> 400;
            case "IDENTITY_PERMISSION_DENIED" -> 403;
            case "IDENTITY_ROLE_NAME_REQUIRED" -> 400;
            default -> 500;
        };
        ErrorResponse error = ErrorResponse.of(status, "Role error", e.getMessage(), e.getErrorCode(), "");
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(OrganizationServiceImpl.ServiceException.class)
    public ResponseEntity<ErrorResponse> handleOrgServiceException(OrganizationServiceImpl.ServiceException e) {
        int status = switch (e.getErrorCode()) {
            case "IDENTITY_ORGANIZATION_NOT_FOUND" -> 404;
            case "IDENTITY_ORGANIZATION_NOT_ACTIVE" -> 403;
            case "IDENTITY_MEMBERSHIP_NOT_FOUND" -> 404;
            case "IDENTITY_MEMBERSHIP_NOT_ACTIVE" -> 403;
            case "IDENTITY_OWNERSHIP_TRANSFER_INVALID" -> 403;
            case "IDENTITY_PERSONAL_ORGANIZATION_IMMUTABLE" -> 403;
            case "IDENTITY_OWNER_CANNOT_LEAVE" -> 403;
            case "IDENTITY_OWNER_CANNOT_BE_REMOVED" -> 403;
            case "IDENTITY_MEMBER_ROLE_REQUIRED" -> 400;
            default -> 500;
        };
        ErrorResponse error = ErrorResponse.of(status, "Organization error", e.getMessage(), e.getErrorCode(), "");
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(InvitationServiceImpl.ServiceException.class)
    public ResponseEntity<ErrorResponse> handleInvitationServiceException(InvitationServiceImpl.ServiceException e) {
        int status = switch (e.getErrorCode()) {
            case "IDENTITY_INVITATION_NOT_FOUND" -> 404;
            case "IDENTITY_INVITATION_EXPIRED" -> 410;
            case "IDENTITY_INVITATION_ALREADY_ACCEPTED" -> 409;
            case "IDENTITY_INVITATION_EMAIL_MISMATCH" -> 403;
            case "IDENTITY_MEMBER_ALREADY_EXISTS" -> 409;
            default -> 500;
        };
        ErrorResponse error = ErrorResponse.of(status, "Invitation error", e.getMessage(), e.getErrorCode(), "");
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(OrganizationController.AuthException.class)
    public ResponseEntity<ErrorResponse> handleOrgControllerAuthException(OrganizationController.AuthException e) {
        ErrorResponse error = ErrorResponse.of(401, "Authentication required", e.getMessage(), e.getErrorCode(), "");
        return ResponseEntity.status(401).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        String detail = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b).orElse("Validation failed");
        ErrorResponse error = ErrorResponse.of(400, "Validation error", detail, "IDENTITY_VALIDATION_ERROR", "");
        return ResponseEntity.status(400).body(error);
    }

    // === OAuth exception handlers returning RFC 6749 error format ===

    @ExceptionHandler(OAuthAuthorizationService.OAuthException.class)
    public ResponseEntity<Map<String, Object>> handleOAuthException(OAuthAuthorizationService.OAuthException e) {
        Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("error", "invalid_grant");
        body.put("error_description", e.getMessage());
        return ResponseEntity.status(400).body(body);
    }

    @ExceptionHandler(OAuthClientService.OAuthClientNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleOAuthClientNotFound(OAuthClientService.OAuthClientNotFoundException e) {
        Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("error", "invalid_client");
        body.put("error_description", e.getMessage());
        return ResponseEntity.status(401).body(body);
    }

    @ExceptionHandler(OAuthTokenService.TokenValidationException.class)
    public ResponseEntity<Map<String, Object>> handleTokenValidation(OAuthTokenService.TokenValidationException e) {
        Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("error", "invalid_token");
        body.put("error_description", e.getMessage());
        return ResponseEntity.status(401).body(body);
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