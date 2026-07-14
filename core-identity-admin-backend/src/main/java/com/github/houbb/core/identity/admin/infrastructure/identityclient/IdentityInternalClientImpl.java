package com.github.houbb.core.identity.admin.infrastructure.identityclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Default implementation of IdentityInternalClient using RestTemplate.
 * Handles service token acquisition and caching.
 */
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
        try {
            String token = getServiceToken();
            ResponseEntity<Map> response = restTemplate.exchange(
                    baseUrl + "/internal/v1/identity/system/info",
                    HttpMethod.GET,
                    new HttpEntity<>(authHeaders(token)),
                    Map.class
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to get system info from Identity Backend: {}", e.getMessage());
            return Map.of("error", e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getHealthInfo() {
        try {
            String token = getServiceToken();
            ResponseEntity<Map> response = restTemplate.exchange(
                    baseUrl + "/internal/v1/identity/system/health",
                    HttpMethod.GET,
                    new HttpEntity<>(authHeaders(token)),
                    Map.class
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to get health info from Identity Backend: {}", e.getMessage());
            return Map.of("status", "UNAVAILABLE", "error", e.getMessage());
        }
    }

    @Override
    public boolean isReachable() {
        try {
            getServiceToken();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String getServiceToken() {
        // Check cache
        if (cachedToken != null && System.currentTimeMillis() < tokenExpiry - 60_000) {
            return cachedToken;
        }

        tokenLock.lock();
        try {
            // Double-check inside lock
            if (cachedToken != null && System.currentTimeMillis() < tokenExpiry - 60_000) {
                return cachedToken;
            }

            // Request new token
            Map<String, String> body = new java.util.HashMap<>();
            body.put("client_id", clientId);
            body.put("client_secret", clientSecret);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            ResponseEntity<Map> response = restTemplate.exchange(
                    baseUrl + "/internal/v1/identity/service-tokens",
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    Map.class
            );

            if (response.getBody() == null) {
                throw new RuntimeException("Empty token response");
            }

            cachedToken = (String) response.getBody().get("accessToken");
            Long expiresIn = ((Number) response.getBody().get("expiresIn")).longValue();
            tokenExpiry = System.currentTimeMillis() + expiresIn * 1000;

            log.info("Service token obtained, expires in {}s", expiresIn);
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