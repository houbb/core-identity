package com.github.houbb.core.identity.application.command;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * Command to create a service token.
 */
public class CreateServiceTokenCommand {

    @NotBlank
    private String clientId;

    @NotBlank
    private String clientSecret;

    public CreateServiceTokenCommand() {
    }

    public CreateServiceTokenCommand(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getClientSecret() { return clientSecret; }
    public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }
}