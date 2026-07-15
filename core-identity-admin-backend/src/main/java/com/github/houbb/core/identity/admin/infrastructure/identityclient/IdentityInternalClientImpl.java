package com.github.houbb.core.identity.admin.infrastructure.identityclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class IdentityInternalClientImpl implements IdentityInternalClient {

    private static final Logger log = LoggerFactory.getLogger(IdentityInternalClientImpl.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String clientId;
    private final String clientSecret;

    private volatile String cachedToken;
    private volatile long tokenExpiry = 0;
    private final ReentrantLock tokenLock = new ReentrantLock();

    public IdentityInternalClientImpl(
            @Value("${core.identity.base-url}") String baseUrl,
            @Value("${core.internal-client.client-id}") String clientId,
            @Value("${core.internal-client.client-secret}") String clientSecret) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Override
    public Map<String, Object> getSystemInfo() {
        return get(baseUrl + "/internal/v1/identity/system/info", Map.class);
    }

    @Override
    public Map<String, Object> getHealthInfo() {
        return get(baseUrl + "/internal/v1/identity/system/health", Map.class);
    }

    @Override
    public boolean isReachable() {
        try { getServiceToken(); return true; } catch (Exception e) { return false; }
    }

    // Admin auth
    @Override
    public Map<String, Object> adminLogin(String email, String password) {
        Map<String, String> body = Map.of("email", email, "password", password);
        return post(baseUrl + "/internal/v1/identity/admin-auth/login", body, Map.class);
    }

    @Override
    public Map<String, Object> adminIntrospect(String token) {
        return post(baseUrl + "/internal/v1/identity/admin-auth/introspect",
                Map.of("token", token), Map.class);
    }

    @Override
    public void adminLogout(String token) {
        post(baseUrl + "/internal/v1/identity/admin-auth/logout",
                Map.of("token", token), Map.class);
    }

    // User management
    @Override
    public Map<String, Object> listUsers(int page, int size, String status, String email) {
        return get(baseUrl + "/internal/v1/identity/users?page=" + page + "&size=" + size
                + (status != null ? "&status=" + status : "")
                + (email != null ? "&email=" + email : ""), Map.class);
    }

    @Override
    public Map<String, Object> createUser(Map<String, String> body) {
        return post(baseUrl + "/internal/v1/identity/users", body, Map.class);
    }

    @Override
    public Map<String, Object> getUserDetail(String userId) {
        return get(baseUrl + "/internal/v1/identity/users/" + userId, Map.class);
    }

    @Override
    public Map<String, Object> disableUser(String userId, Map<String, String> body) {
        return post(baseUrl + "/internal/v1/identity/users/" + userId + "/disable", body, Map.class);
    }

    @Override
    public Map<String, Object> enableUser(String userId) {
        return post(baseUrl + "/internal/v1/identity/users/" + userId + "/enable",
                Collections.emptyMap(), Map.class);
    }

    @Override
    public Map<String, Object> revokeSessions(String userId) {
        return post(baseUrl + "/internal/v1/identity/users/" + userId + "/revoke-sessions",
                Collections.emptyMap(), Map.class);
    }

    @Override
    public Map<String, Object> resendVerification(String userId) {
        return post(baseUrl + "/internal/v1/identity/users/" + userId + "/resend-verification",
                Collections.emptyMap(), Map.class);
    }

    @Override
    public Map<String, Object> sendPasswordReset(String userId) {
        return post(baseUrl + "/internal/v1/identity/users/" + userId + "/send-password-reset",
                Collections.emptyMap(), Map.class);
    }

    @Override
    public Map<String, Object> getLoginAttempts(String userId) {
        return get(baseUrl + "/internal/v1/identity/users/" + userId + "/login-attempts", Map.class);
    }

    @SuppressWarnings("unchecked")
    private <T> T get(String url, Class<T> responseType) {
        try {
            String token = getServiceToken();
            ResponseEntity<T> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(authHeaders(token)), responseType);
            return response.getBody();
        } catch (Exception e) {
            log.error("GET {} failed: {}", url, e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T post(String url, Object body, Class<T> responseType) {
        try {
            String token = getServiceToken();
            HttpHeaders headers = authHeaders(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            ResponseEntity<T> response = restTemplate.exchange(
                    url, HttpMethod.POST, new HttpEntity<>(body, headers), responseType);
            return response.getBody();
        } catch (Exception e) {
            log.error("POST {} failed: {}", url, e.getMessage());
            throw new RuntimeException("Backend call failed: " + e.getMessage(), e);
        }
    }

    private String getServiceToken() {
        if (cachedToken != null && System.currentTimeMillis() < tokenExpiry - 60_000) {
            return cachedToken;
        }

        tokenLock.lock();
        try {
            if (cachedToken != null && System.currentTimeMillis() < tokenExpiry - 60_000) {
                return cachedToken;
            }

            Map<String, String> body = Map.of("client_id", clientId, "client_secret", clientSecret);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            ResponseEntity<Map> response = restTemplate.exchange(
                    baseUrl + "/internal/v1/identity/service-tokens",
                    HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);

            if (response.getBody() == null) throw new RuntimeException("Empty token response");

            cachedToken = (String) response.getBody().get("accessToken");
            Number expiresIn = (Number) response.getBody().get("expiresIn");
            tokenExpiry = System.currentTimeMillis() + (expiresIn != null ? expiresIn.longValue() * 1000 : 600_000);
            return cachedToken;
        } finally {
            tokenLock.unlock();
        }
    }

    private HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.set("X-Request-ID", java.util.UUID.randomUUID().toString());
        return headers;
    }
}