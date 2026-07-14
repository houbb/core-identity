package com.github.houbb.core.identity.application.command;

import jakarta.validation.constraints.NotBlank;

/**
 * Command to check idempotency.
 */
public class IdempotencyCommand {

    @NotBlank
    private String idempotencyKey;
    @NotBlank
    private String scope;
    private String requestHash;

    public IdempotencyCommand() {
    }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }
    public String getRequestHash() { return requestHash; }
    public void setRequestHash(String requestHash) { this.requestHash = requestHash; }
}