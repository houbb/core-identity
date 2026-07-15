-- V0.4.0.041: Create identity_api_key table
CREATE TABLE IF NOT EXISTS identity_api_key (
    id              VARCHAR(36)  NOT NULL PRIMARY KEY,
    key_prefix      VARCHAR(40)  NOT NULL,
    key_hash        VARCHAR(255) NOT NULL,
    name            VARCHAR(120) NOT NULL,
    owner_type      VARCHAR(30)  NOT NULL,
    owner_id        VARCHAR(36)  NOT NULL,
    organization_id VARCHAR(36),
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    expires_at      BIGINT,
    last_used_at    BIGINT,
    last_used_ip    VARCHAR(64),
    created_at      BIGINT       NOT NULL,
    revoked_at      BIGINT,
    version         BIGINT       NOT NULL DEFAULT 1
);

CREATE INDEX IF NOT EXISTS idx_identity_ak_owner ON identity_api_key(owner_type, owner_id);
CREATE INDEX IF NOT EXISTS idx_identity_ak_prefix ON identity_api_key(key_prefix);
CREATE INDEX IF NOT EXISTS idx_identity_ak_status ON identity_api_key(status);

-- V0.4.0.042: Create identity_api_key_scope table
CREATE TABLE IF NOT EXISTS identity_api_key_scope (
    id        VARCHAR(36) NOT NULL PRIMARY KEY,
    key_id    VARCHAR(36) NOT NULL,
    scope_id  VARCHAR(36) NOT NULL,
    created_at BIGINT     NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_identity_aks_key ON identity_api_key_scope(key_id);

-- V0.4.0.043: Create identity_api_key_audience table
CREATE TABLE IF NOT EXISTS identity_api_key_audience (
    id           VARCHAR(36) NOT NULL PRIMARY KEY,
    key_id       VARCHAR(36) NOT NULL,
    audience_id  VARCHAR(36) NOT NULL,
    created_at   BIGINT      NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_identity_aka_key ON identity_api_key_audience(key_id);