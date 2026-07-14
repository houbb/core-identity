package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.domain.InternalClient;
import com.github.houbb.core.identity.application.port.InternalClientRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;
import java.util.HexFormat;

/**
 * Default implementation of InternalTokenService using JWT.
 */
public class InternalTokenServiceImpl implements InternalTokenService {

    private static final Logger log = LoggerFactory.getLogger(InternalTokenServiceImpl.class);

    private final InternalClientRepository clientRepository;
    private final SecretKey signingKey;
    private final String issuer;
    private final int tokenTtlSeconds;

    public InternalTokenServiceImpl(InternalClientRepository clientRepository,
                                    String signingKey, String issuer, int tokenTtlSeconds) {
        this.clientRepository = clientRepository;
        this.signingKey = Keys.hmacShaKeyFor(signingKey.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.tokenTtlSeconds = tokenTtlSeconds;
    }

    @Override
    public String issueToken(String clientId, String clientSecret) {
        // Find client
        InternalClient client = clientRepository.findByClientId(clientId)
                .orElseThrow(() -> new AuthenticationException("Invalid client credentials"));

        // Check status
        if (!"ACTIVE".equals(client.getStatus())) {
            throw new AuthenticationException("Client is disabled");
        }

        // Check expiry
        if (client.getExpiresAt() != null && client.getExpiresAt() < Instant.now().toEpochMilli()) {
            throw new AuthenticationException("Client credentials have expired");
        }

        // Verify secret hash
        String secretHash = hashSecret(clientSecret);
        if (!secretHash.equals(client.getClientSecretHash())) {
            throw new AuthenticationException("Invalid client credentials");
        }

        // Build scopes
        String scopes = client.getScopes() != null && !client.getScopes().isEmpty()
                ? String.join(" ", client.getScopes())
                : "identity.system.read";

        // Issue token
        Instant now = Instant.now();
        String token = Jwts.builder()
                .subject(client.getClientId())
                .claim("type", "service")
                .claim("scope", scopes)
                .issuer(issuer)
                .audience().add("core-identity-backend").and()
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(tokenTtlSeconds)))
                .signWith(signingKey)
                .compact();

        // Update last used
        clientRepository.updateLastUsedAt(clientId, now.toEpochMilli());

        log.info("Service token issued for client: {}", clientId);
        return token;
    }

    @Override
    public String validateToken(String token) {
        try {
            var claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .requireAudience("core-identity-backend")
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.getSubject();
        } catch (Exception e) {
            throw new AuthenticationException("Invalid or expired service token");
        }
    }

    @Override
    public boolean hasScope(String token, String requiredScope) {
        try {
            var claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String scopeStr = claims.get("scope", String.class);
            if (scopeStr == null) return false;

            for (String s : scopeStr.split(" ")) {
                if (s.equals(requiredScope)) return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public static String hashSecret(String secret) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(secret.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    /**
     * Internal authentication exception.
     */
    public static class AuthenticationException extends RuntimeException {
        public AuthenticationException(String message) {
            super(message);
        }
    }
}