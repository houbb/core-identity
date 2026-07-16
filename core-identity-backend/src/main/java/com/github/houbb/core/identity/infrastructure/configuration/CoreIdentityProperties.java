package com.github.houbb.core.identity.infrastructure.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Consolidated P7 configuration properties.
 * <p>
 * All properties are prefixed with {@code core}.
 * <p>
 * Design principle: every property has a sensible default that works
 * in standalone/community mode (SQLite, no Redis, no clustering).
 */
@Component
@ConfigurationProperties(prefix = "core")
public class CoreIdentityProperties {

    /** Identity instance properties */
    private Identity identity = new Identity();

    /** Internal service-to-service auth */
    private InternalAuth internalAuth = new InternalAuth();

    /** OAuth / OIDC configuration */
    private OAuth oauth = new OAuth();

    /** Outbox event relay */
    private Outbox outbox = new Outbox();

    /** Idempotency configuration */
    private Idempotency idempotency = new Idempotency();

    /** Login security configuration */
    private Login login = new Login();

    /** Session configuration */
    private Session session = new Session();

    /** Password policy */
    private Password password = new Password();

    /** Database configuration */
    private Database database = new Database();

    /** Deployment mode */
    private Deployment deployment = new Deployment();

    /** Cache configuration */
    private Cache cache = new Cache();

    /** Cluster configuration */
    private Cluster cluster = new Cluster();

    /** Observability configuration */
    private Observability observability = new Observability();

    /** Redis configuration */
    private Redis redis = new Redis();

    // === Nested property classes ===

    public static class Identity {
        private String instanceName = "Core Identity";
        private String edition = "COMMUNITY";

        public String getInstanceName() { return instanceName; }
        public void setInstanceName(String instanceName) { this.instanceName = instanceName; }
        public String getEdition() { return edition; }
        public void setEdition(String edition) { this.edition = edition; }
    }

    public static class InternalAuth {
        private String issuer = "core-identity";
        private int tokenTtlSeconds = 600;
        private String signingKey = "dev-signing-key-at-least-256-bits-long-for-hs256";

        public String getIssuer() { return issuer; }
        public void setIssuer(String issuer) { this.issuer = issuer; }
        public int getTokenTtlSeconds() { return tokenTtlSeconds; }
        public void setTokenTtlSeconds(int tokenTtlSeconds) { this.tokenTtlSeconds = tokenTtlSeconds; }
        public String getSigningKey() { return signingKey; }
        public void setSigningKey(String signingKey) { this.signingKey = signingKey; }
    }

    public static class OAuth {
        private String issuerBase = "http://localhost:8101";
        private String masterKey = "dev-master-key-32-bytes-for-aes256!";
        private int accessTokenTtlSeconds = 900;
        private int refreshTokenTtlSeconds = 604800;
        private int authCodeTtlSeconds = 120;
        private int idTokenTtlSeconds = 600;

        public String getIssuerBase() { return issuerBase; }
        public void setIssuerBase(String issuerBase) { this.issuerBase = issuerBase; }
        public String getMasterKey() { return masterKey; }
        public void setMasterKey(String masterKey) { this.masterKey = masterKey; }
        public int getAccessTokenTtlSeconds() { return accessTokenTtlSeconds; }
        public void setAccessTokenTtlSeconds(int accessTokenTtlSeconds) { this.accessTokenTtlSeconds = accessTokenTtlSeconds; }
        public int getRefreshTokenTtlSeconds() { return refreshTokenTtlSeconds; }
        public void setRefreshTokenTtlSeconds(int refreshTokenTtlSeconds) { this.refreshTokenTtlSeconds = refreshTokenTtlSeconds; }
        public int getAuthCodeTtlSeconds() { return authCodeTtlSeconds; }
        public void setAuthCodeTtlSeconds(int authCodeTtlSeconds) { this.authCodeTtlSeconds = authCodeTtlSeconds; }
        public int getIdTokenTtlSeconds() { return idTokenTtlSeconds; }
        public void setIdTokenTtlSeconds(int idTokenTtlSeconds) { this.idTokenTtlSeconds = idTokenTtlSeconds; }
    }

    public static class Outbox {
        private boolean enabled = true;
        private String pollInterval = "5s";
        private int maxAttempts = 5;
        private Relay relay = new Relay();

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getPollInterval() { return pollInterval; }
        public void setPollInterval(String pollInterval) { this.pollInterval = pollInterval; }
        public int getMaxAttempts() { return maxAttempts; }
        public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }
        public Relay getRelay() { return relay; }
        public void setRelay(Relay relay) { this.relay = relay; }

        public static class Relay {
            private boolean enabled = false;
            private String pollInterval = "5s";
            private int maxAttempts = 5;

            public boolean isEnabled() { return enabled; }
            public void setEnabled(boolean enabled) { this.enabled = enabled; }
            public String getPollInterval() { return pollInterval; }
            public void setPollInterval(String pollInterval) { this.pollInterval = pollInterval; }
            public int getMaxAttempts() { return maxAttempts; }
            public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }
        }
    }

    public static class Idempotency {
        private int recordTtlHours = 24;

        public int getRecordTtlHours() { return recordTtlHours; }
        public void setRecordTtlHours(int recordTtlHours) { this.recordTtlHours = recordTtlHours; }
    }

    public static class Login {
        private int maxFailedAttempts = 5;
        private String lockDuration = "15m";
        private int ipRateLimit = 20;
        private String ipRateWindow = "1m";

        public int getMaxFailedAttempts() { return maxFailedAttempts; }
        public void setMaxFailedAttempts(int maxFailedAttempts) { this.maxFailedAttempts = maxFailedAttempts; }
        public String getLockDuration() { return lockDuration; }
        public void setLockDuration(String lockDuration) { this.lockDuration = lockDuration; }
        public int getIpRateLimit() { return ipRateLimit; }
        public void setIpRateLimit(int ipRateLimit) { this.ipRateLimit = ipRateLimit; }
        public String getIpRateWindow() { return ipRateWindow; }
        public void setIpRateWindow(String ipRateWindow) { this.ipRateWindow = ipRateWindow; }
    }

    public static class Session {
        private int idleTimeoutHours = 2;
        private int absoluteTimeoutHours = 24;
        private String storeType = "database";

        public int getIdleTimeoutHours() { return idleTimeoutHours; }
        public void setIdleTimeoutHours(int idleTimeoutHours) { this.idleTimeoutHours = idleTimeoutHours; }
        public int getAbsoluteTimeoutHours() { return absoluteTimeoutHours; }
        public void setAbsoluteTimeoutHours(int absoluteTimeoutHours) { this.absoluteTimeoutHours = absoluteTimeoutHours; }
        public String getStoreType() { return storeType; }
        public void setStoreType(String storeType) { this.storeType = storeType; }
    }

    public static class Password {
        private int minLength = 8;

        public int getMinLength() { return minLength; }
        public void setMinLength(int minLength) { this.minLength = minLength; }
    }

    public static class Database {
        private String type = "sqlite";

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }

    public static class Deployment {
        private String mode = "standalone";

        public String getMode() { return mode; }
        public void setMode(String mode) { this.mode = mode; }
    }

    public static class Cache {
        private String type = "caffeine";

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }

    public static class Cluster {
        private boolean enabled = false;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    public static class Observability {
        private Traces traces = new Traces();
        private Metrics metrics = new Metrics();

        public Traces getTraces() { return traces; }
        public void setTraces(Traces traces) { this.traces = traces; }
        public Metrics getMetrics() { return metrics; }
        public void setMetrics(Metrics metrics) { this.metrics = metrics; }

        public static class Traces {
            private boolean enabled = false;
            private String endpoint = "http://localhost:4317";

            public boolean isEnabled() { return enabled; }
            public void setEnabled(boolean enabled) { this.enabled = enabled; }
            public String getEndpoint() { return endpoint; }
            public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
        }

        public static class Metrics {
            private boolean export = false;

            public boolean isExport() { return export; }
            public void setExport(boolean export) { this.export = export; }
        }
    }

    public static class Redis {
        private String host = "localhost";
        private int port = 6379;
        private String password = "";
        private String timeout = "2000ms";

        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getTimeout() { return timeout; }
        public void setTimeout(String timeout) { this.timeout = timeout; }
    }

    // === Getters and setters for top-level properties ===

    public Identity getIdentity() { return identity; }
    public void setIdentity(Identity identity) { this.identity = identity; }

    public InternalAuth getInternalAuth() { return internalAuth; }
    public void setInternalAuth(InternalAuth internalAuth) { this.internalAuth = internalAuth; }

    public OAuth getOauth() { return oauth; }
    public void setOauth(OAuth oauth) { this.oauth = oauth; }

    public Outbox getOutbox() { return outbox; }
    public void setOutbox(Outbox outbox) { this.outbox = outbox; }

    public Idempotency getIdempotency() { return idempotency; }
    public void setIdempotency(Idempotency idempotency) { this.idempotency = idempotency; }

    public Login getLogin() { return login; }
    public void setLogin(Login login) { this.login = login; }

    public Session getSession() { return session; }
    public void setSession(Session session) { this.session = session; }

    public Password getPassword() { return password; }
    public void setPassword(Password password) { this.password = password; }

    public Database getDatabase() { return database; }
    public void setDatabase(Database database) { this.database = database; }

    public Deployment getDeployment() { return deployment; }
    public void setDeployment(Deployment deployment) { this.deployment = deployment; }

    public Cache getCache() { return cache; }
    public void setCache(Cache cache) { this.cache = cache; }

    public Cluster getCluster() { return cluster; }
    public void setCluster(Cluster cluster) { this.cluster = cluster; }

    public Observability getObservability() { return observability; }
    public void setObservability(Observability observability) { this.observability = observability; }

    public Redis getRedis() { return redis; }
    public void setRedis(Redis redis) { this.redis = redis; }
}
