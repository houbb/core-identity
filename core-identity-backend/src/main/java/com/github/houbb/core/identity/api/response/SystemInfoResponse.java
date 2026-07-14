package com.github.houbb.core.identity.api.response;

/**
 * System info response for internal API.
 */
public class SystemInfoResponse {

    private String service;
    private String version;
    private String apiVersion;
    private String databaseType;
    private String databaseStatus;
    private String flywayStatus;
    private long bootTime;

    public SystemInfoResponse() {
    }

    public String getService() { return service; }
    public void setService(String service) { this.service = service; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getApiVersion() { return apiVersion; }
    public void setApiVersion(String apiVersion) { this.apiVersion = apiVersion; }
    public String getDatabaseType() { return databaseType; }
    public void setDatabaseType(String databaseType) { this.databaseType = databaseType; }
    public String getDatabaseStatus() { return databaseStatus; }
    public void setDatabaseStatus(String databaseStatus) { this.databaseStatus = databaseStatus; }
    public String getFlywayStatus() { return flywayStatus; }
    public void setFlywayStatus(String flywayStatus) { this.flywayStatus = flywayStatus; }
    public long getBootTime() { return bootTime; }
    public void setBootTime(long bootTime) { this.bootTime = bootTime; }
}