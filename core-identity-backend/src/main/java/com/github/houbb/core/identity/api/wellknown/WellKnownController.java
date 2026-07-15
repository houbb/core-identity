package com.github.houbb.core.identity.api.wellknown;

import com.github.houbb.core.identity.application.service.OAuthTokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * OIDC Discovery and JWKS endpoints.
 */
@RestController
public class WellKnownController {

    private final OAuthTokenService tokenService;
    private final String issuerBase;

    public WellKnownController(OAuthTokenService tokenService,
                               @org.springframework.beans.factory.annotation.Value("${core.oauth.issuer-base:http://localhost:8101}") String issuerBase) {
        this.tokenService = tokenService;
        this.issuerBase = issuerBase;
    }

    @GetMapping("/.well-known/openid-configuration")
    public ResponseEntity<Map<String, Object>> openidConfiguration() {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("issuer", issuerBase);
        config.put("authorization_endpoint", issuerBase + "/oauth2/authorize");
        config.put("token_endpoint", issuerBase + "/oauth2/token");
        config.put("revocation_endpoint", issuerBase + "/oauth2/revoke");
        config.put("introspection_endpoint", issuerBase + "/oauth2/introspect");
        config.put("userinfo_endpoint", issuerBase + "/userinfo");
        config.put("jwks_uri", issuerBase + "/.well-known/jwks.json");
        config.put("scopes_supported", List.of("openid", "profile", "email", "organization", "offline_access"));
        config.put("response_types_supported", List.of("code"));
        config.put("grant_types_supported", List.of("authorization_code", "refresh_token", "client_credentials"));
        config.put("subject_types_supported", List.of("public"));
        config.put("id_token_signing_alg_values_supported", List.of("RS256"));
        config.put("token_endpoint_auth_methods_supported", List.of("client_secret_basic", "client_secret_post", "none"));
        config.put("code_challenge_methods_supported", List.of("S256"));
        return ResponseEntity.ok(config);
    }

    @GetMapping("/.well-known/jwks.json")
    public ResponseEntity<Map<String, Object>> jwks() {
        Map<String, Object> jwks = new LinkedHashMap<>();
        jwks.put("keys", tokenService.getJwksKeysAsMap());
        return ResponseEntity.ok(jwks);
    }
}