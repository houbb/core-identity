-- V0.2.0.006: Create identity_session table
CREATE TABLE IF NOT EXISTS identity_session (
    id                  VARCHAR(36)  NOT NULL PRIMARY KEY,
    user_id             VARCHAR(36)  NOT NULL,
    session_type        VARCHAR(30)  NOT NULL DEFAULT 'USER_WEB',
    token_hash          VARCHAR(255) NOT NULL,
    status              VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    ip_address          VARCHAR(64),
    user_agent          VARCHAR(500),
    device_name         VARCHAR(150),
    last_active_at      BIGINT       NOT NULL,
    idle_expires_at     BIGINT       NOT NULL,
    absolute_expires_at BIGINT       NOT NULL,
    revoked_at          BIGINT,
    revoke_reason       VARCHAR(500),
    created_at          BIGINT       NOT NULL,
    updated_at          BIGINT       NOT NULL,
    version             BIGINT       NOT NULL DEFAULT 1
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_identity_session_token_hash
    ON identity_session(token_hash);
CREATE INDEX IF NOT EXISTS idx_identity_session_user_status
    ON identity_session(user_id, status);
CREATE INDEX IF NOT EXISTS idx_identity_session_idle_expires
    ON identity_session(idle_expires_at);
CREATE INDEX IF NOT EXISTS idx_identity_session_absolute_expires
    ON identity_session(absolute_expires_at);