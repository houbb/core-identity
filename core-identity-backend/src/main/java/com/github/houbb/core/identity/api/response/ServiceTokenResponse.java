package com.github.houbb.core.identity.api.response;

/**
 * Service token response.
 */
public class ServiceTokenResponse {

    private String accessToken;
    private String tokenType;
    private long expiresIn;
    private String scope;

    public ServiceTokenResponse() {
    }

    public ServiceTokenResponse(String accessToken, String tokenType, long expiresIn, String scope) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.scope = scope;
    }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
    public long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }
    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }
}