-- V0.4.0.011: Create identity_token_revocation table
CREATE TABLE IF NOT EXISTS identity_token_revocation (
    id         VARCHAR(36)  NOT NULL PRIMARY KEY,
    token_jti  VARCHAR(120) NOT NULL,
    subject_id VARCHAR(36),
    reason     VARCHAR(500),
    expires_at BIGINT       NOT NULL,
    created_at BIGINT       NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE UNIQUE INDEX IF NOT EXISTS uq_identity_token_revocation_jti
    ON identity_token_revocation(token_jti);
CREATE INDEX IF NOT EXISTS idx_identity_token_revocation_expires
    ON identity_token_revocation(expires_at);