-- V0.4.0.022: Create identity_oauth_client_secret table
CREATE TABLE IF NOT EXISTS identity_oauth_client_secret (
    id            VARCHAR(36)  NOT NULL PRIMARY KEY,
    client_id     VARCHAR(36)  NOT NULL,
    secret_prefix VARCHAR(30),
    secret_hash   VARCHAR(255) NOT NULL,
    name          VARCHAR(100),
    status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    expires_at    BIGINT,
    last_used_at  BIGINT,
    created_at    BIGINT       NOT NULL,
    revoked_at    BIGINT
);

CREATE INDEX IF NOT EXISTS idx_identity_oauth_client_secret_client
    ON identity_oauth_client_secret(client_id);
CREATE INDEX IF NOT EXISTS idx_identity_oauth_client_secret_status
    ON identity_oauth_client_secret(status);