package com.github.houbb.core.identity.api.response;

/**
 * Public API metadata response.
 */
public class MetaResponse {

    private String service;
    private String version;
    private String apiVersion;
    private String status;
    private String instanceName;
    private String edition;
    private String[] capabilities;

    public MetaResponse() {
    }

    public String getService() { return service; }
    public void setService(String service) { this.service = service; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getApiVersion() { return apiVersion; }
    public void setApiVersion(String apiVersion) { this.apiVersion = apiVersion; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getInstanceName() { return instanceName; }
    public void setInstanceName(String instanceName) { this.instanceName = instanceName; }

    public String getEdition() { return edition; }
    public void setEdition(String edition) { this.edition = edition; }

    public String[] getCapabilities() { return capabilities; }
    public void setCapabilities(String[] capabilities) { this.capabilities = capabilities; }
}