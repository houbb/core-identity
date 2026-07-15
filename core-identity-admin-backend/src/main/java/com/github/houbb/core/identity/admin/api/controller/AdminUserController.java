package com.github.houbb.core.identity.admin.api.controller;

import com.github.houbb.core.identity.admin.infrastructure.identityclient.IdentityInternalClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Admin User Management Controller — BFF proxy to Identity Backend Internal API.
 */
@RestController
@RequestMapping("/admin-api/v1/identity/users")
public class AdminUserController {

    private final IdentityInternalClient identityClient;

    public AdminUserController(IdentityInternalClient identityClient) {
        this.identityClient = identityClient;
    }

    @GetMapping
    public ResponseEntity<?> listUsers(@RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "20") int size,
                                        @RequestParam(required = false) String status,
                                        @RequestParam(required = false) String email) {
        return ResponseEntity.ok(identityClient.listUsers(page, size, status, email));
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(identityClient.createUser(body));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserDetail(@PathVariable String userId) {
        return ResponseEntity.ok(identityClient.getUserDetail(userId));
    }

    @PostMapping("/{userId}/disable")
    public ResponseEntity<?> disableUser(@PathVariable String userId, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(identityClient.disableUser(userId, body));
    }

    @PostMapping("/{userId}/enable")
    public ResponseEntity<?> enableUser(@PathVariable String userId) {
        return ResponseEntity.ok(identityClient.enableUser(userId));
    }

    @PostMapping("/{userId}/revoke-sessions")
    public ResponseEntity<?> revokeSessions(@PathVariable String userId) {
        return ResponseEntity.ok(identityClient.revokeSessions(userId));
    }

    @PostMapping("/{userId}/resend-verification")
    public ResponseEntity<?> resendVerification(@PathVariable String userId) {
        return ResponseEntity.ok(identityClient.resendVerification(userId));
    }

    @PostMapping("/{userId}/send-password-reset")
    public ResponseEntity<?> sendPasswordReset(@PathVariable String userId) {
        return ResponseEntity.ok(identityClient.sendPasswordReset(userId));
    }

    @GetMapping("/{userId}/login-attempts")
    public ResponseEntity<?> getLoginAttempts(@PathVariable String userId) {
        return ResponseEntity.ok(identityClient.getLoginAttempts(userId));
    }
}