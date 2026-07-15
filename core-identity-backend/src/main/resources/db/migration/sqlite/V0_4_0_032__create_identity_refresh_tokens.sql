-- V0.4.0.032: Create identity_refresh_token_family table
CREATE TABLE IF NOT EXISTS identity_refresh_token_family (
    id             VARCHAR(36)  NOT NULL PRIMARY KEY,
    grant_id       VARCHAR(36)  NOT NULL,
    client_id      VARCHAR(36)  NOT NULL,
    user_id        VARCHAR(36)  NOT NULL,
    session_id     VARCHAR(36),
    status         VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    revoked_reason VARCHAR(500),
    created_at     BIGINT       NOT NULL,
    revoked_at     BIGINT
);
CREATE INDEX IF NOT EXISTS idx_identity_rt_family_grant ON identity_refresh_token_family(grant_id);

-- V0.4.0.033: Create identity_refresh_token table
CREATE TABLE IF NOT EXISTS identity_refresh_token (
    id             VARCHAR(36)  NOT NULL PRIMARY KEY,
    family_id      VARCHAR(36)  NOT NULL,
    token_hash     VARCHAR(255) NOT NULL,
    status         VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    expires_at     BIGINT       NOT NULL,
    used_at        BIGINT,
    replaced_by_id VARCHAR(36),
    created_at     BIGINT       NOT NULL,
    version        BIGINT       NOT NULL DEFAULT 1
);
CREATE UNIQUE INDEX IF NOT EXISTS uq_identity_refresh_token_hash ON identity_refresh_token(token_hash);
CREATE INDEX IF NOT EXISTS idx_identity_rt_family ON identity_refresh_token(family_id);