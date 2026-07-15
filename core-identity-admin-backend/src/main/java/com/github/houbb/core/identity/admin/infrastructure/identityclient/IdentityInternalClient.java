package com.github.houbb.core.identity.admin.infrastructure.identityclient;

import java.util.Map;

/**
 * HTTP client for calling Identity Backend Internal API.
 * Generated from OpenAPI contract, thin wrapper over HTTP.
 */
public interface IdentityInternalClient {

    Map<String, Object> getSystemInfo();

    Map<String, Object> getHealthInfo();

    boolean isReachable();

    // Admin auth
    Map<String, Object> adminLogin(String email, String password);
    Map<String, Object> adminIntrospect(String token);
    void adminLogout(String token);

    // User management
    Map<String, Object> listUsers(int page, int size, String status, String email);
    Map<String, Object> createUser(Map<String, String> body);
    Map<String, Object> getUserDetail(String userId);
    Map<String, Object> disableUser(String userId, Map<String, String> body);
    Map<String, Object> enableUser(String userId);
    Map<String, Object> revokeSessions(String userId);
    Map<String, Object> resendVerification(String userId);
    Map<String, Object> sendPasswordReset(String userId);
    Map<String, Object> getLoginAttempts(String userId);
}