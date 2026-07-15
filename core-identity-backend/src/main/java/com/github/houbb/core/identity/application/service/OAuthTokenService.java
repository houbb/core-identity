package com.github.houbb.core.identity.application.service;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * OAuth JWT Token service — RS256 asymmetric signing.
 * Replaces the old HMAC-based InternalTokenService.
 */
public class OAuthTokenService {

    private final SigningKeyManager keyManager;
    private final String issuer;

    public OAuthTokenService(SigningKeyManager keyManager, String issuer) {
        this.keyManager = keyManager;
        this.issuer = issuer;
    }

    /**
     * Issue an OAuth Access Token (JWT).
     */
    public String issueAccessToken(String subject, String subjectType, String audience,
                                   String clientId, String organizationId,
                                   String scope, int ttlSeconds) {
        return issueToken(subject, subjectType, audience, clientId, organizationId, scope, ttlSeconds, "access_token");
    }

    /**
     * Issue an ID Token (JWT).
     */
    public String issueIdToken(String subject, String audience, String nonce,
                               long authTime, int ttlSeconds) {
        PrivateKey privateKey = keyManager.getActivePrivateKey();
        String kid = keyManager.getActiveKid();
        Instant now = Instant.now();

        return io.jsonwebtoken.Jwts.builder()
                .header().keyId(kid).and()
                .subject(subject)
                .issuer(issuer)
                .audience().add(audience).and()
                .claim("nonce", nonce)
                .claim("auth_time", authTime)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(ttlSeconds)))
                .signWith(privateKey)
                .compact();
    }

    /**
     * Issue a service-to-service token (replaces InternalTokenService).
     */
    public String issueServiceToken(String clientId, String scope, int ttlSeconds) {
        PrivateKey privateKey = keyManager.getActivePrivateKey();
        String kid = keyManager.getActiveKid();
        Instant now = Instant.now();

        return io.jsonwebtoken.Jwts.builder()
                .header().keyId(kid).and()
                .subject(clientId)
                .claim("subject_type", "service")
                .claim("type", "service")
                .claim("scope", scope)
                .audience().add(issuer).and()
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(ttlSeconds)))
                .signWith(privateKey)
                .compact();
    }

    /**
     * Validate a JWT and return parsed claims.
     */
    public TokenClaims validateToken(String token) {
        String kid = extractKidWithoutValidation(token);
        PublicKey publicKey;
        if (kid != null) {
            publicKey = keyManager.getPublicKeyByKid(kid);
        } else {
            // Fallback: try the first active key
            var keys = keyManager.getJwksKeys();
            publicKey = keys.isEmpty() ? null : keyManager.getPublicKeyByKid(keys.get(0).getKeyId());
        }

        if (publicKey == null) {
            throw new TokenValidationException("No suitable verification key found");
        }

        try {
            var claims = io.jsonwebtoken.Jwts.parser()
                    .verifyWith(publicKey)
                    .requireIssuer(issuer)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return new TokenClaims(
                    claims.getSubject(),
                    claims.get("subject_type", String.class),
                    claims.getAudience(),
                    claims.get("client_id", String.class),
                    claims.get("organization_id", String.class),
                    claims.get("scope", String.class),
                    claims.getId(),
                    claims.getIssuedAt() != null ? claims.getIssuedAt().getTime() / 1000 : 0,
                    claims.getExpiration() != null ? claims.getExpiration().getTime() / 1000 : 0
            );
        } catch (Exception e) {
            throw new TokenValidationException("Token validation failed: " + e.getMessage());
        }
    }

    /**
     * Get public keys for JWKS endpoint.
     */
    public java.util.List<java.util.Map<String, Object>> getJwksKeysAsMap() {
        var keys = keyManager.getJwksKeys();
        java.util.List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
        for (var k : keys) {
            try {
                PublicKey pubKey = keyManager.getPublicKeyByKid(k.getKeyId());
                if (pubKey instanceof java.security.interfaces.RSAPublicKey rsa) {
                    java.util.Map<String, Object> jwk = new java.util.LinkedHashMap<>();
                    jwk.put("kty", "RSA");
                    jwk.put("kid", k.getKeyId());
                    jwk.put("alg", k.getAlgorithm());
                    jwk.put("use", "sig");
                    jwk.put("n", base64UrlEncode(rsa.getModulus().toByteArray()));
                    jwk.put("e", base64UrlEncode(rsa.getPublicExponent().toByteArray()));
                    result.add(jwk);
                }
            } catch (Exception ignored) {
            }
        }
        return result;
    }

    // === Private helpers ===

    private String issueToken(String subject, String subjectType, String audience,
                              String clientId, String organizationId,
                              String scope, int ttlSeconds, String tokenType) {
        PrivateKey privateKey = keyManager.getActivePrivateKey();
        String kid = keyManager.getActiveKid();
        Instant now = Instant.now();
        String jti = UUID.randomUUID().toString();

        return io.jsonwebtoken.Jwts.builder()
                .header().keyId(kid).and()
                .id(jti)
                .subject(subject)
                .claim("subject_type", subjectType)
                .audience().add(audience).and()
                .claim("client_id", clientId)
                .claim("organization_id", organizationId)
                .claim("scope", scope)
                .claim("type", tokenType)
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(ttlSeconds)))
                .signWith(privateKey)
                .compact();
    }

    private String extractKidWithoutValidation(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return null;
            byte[] headerBytes = java.util.Base64.getUrlDecoder().decode(parts[0]);
            String headerJson = new String(headerBytes, java.nio.charset.StandardCharsets.UTF_8);
            // Simple JSON parse for kid
            int kidIdx = headerJson.indexOf("\"kid\"");
            if (kidIdx < 0) return null;
            int colonIdx = headerJson.indexOf(":", kidIdx);
            int startQuote = headerJson.indexOf("\"", colonIdx + 1);
            int endQuote = headerJson.indexOf("\"", startQuote + 1);
            if (startQuote < 0 || endQuote < 0) return null;
            return headerJson.substring(startQuote + 1, endQuote);
        } catch (Exception e) {
            return null;
        }
    }

    private String base64UrlEncode(byte[] data) {
        // Strip leading zero byte if present (Java BigInteger encoding quirk)
        int offset = 0;
        if (data.length > 0 && data[0] == 0) {
            byte[] stripped = new byte[data.length - 1];
            System.arraycopy(data, 1, stripped, 0, stripped.length);
            data = stripped;
        }
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    // === Inner types ===

    public record TokenClaims(
            String subject,
            String subjectType,
            java.util.Set<String> audience,
            String clientId,
            String organizationId,
            String scope,
            String jti,
            long iat,
            long exp
    ) {
    }

    public static class TokenValidationException extends RuntimeException {
        public TokenValidationException(String message) {
            super(message);
        }
    }
}