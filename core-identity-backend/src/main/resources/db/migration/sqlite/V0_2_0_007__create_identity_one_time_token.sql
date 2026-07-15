-- V0.2.0.007: Create identity_one_time_token table
CREATE TABLE IF NOT EXISTS identity_one_time_token (
    id            VARCHAR(36)  NOT NULL PRIMARY KEY,
    user_id       VARCHAR(36)  NOT NULL,
    token_type    VARCHAR(40)  NOT NULL,
    token_hash    VARCHAR(255) NOT NULL,
    status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    expires_at    BIGINT       NOT NULL,
    used_at       BIGINT,
    metadata_json TEXT,
    created_at    BIGINT       NOT NULL,
    updated_at    BIGINT       NOT NULL,
    version       BIGINT       NOT NULL DEFAULT 1
);

CREATE INDEX IF NOT EXISTS idx_identity_token_user_type_status
    ON identity_one_time_token(user_id, token_type, status);
CREATE UNIQUE INDEX IF NOT EXISTS uq_identity_token_hash
    ON identity_one_time_token(token_hash);
CREATE INDEX IF NOT EXISTS idx_identity_token_expires
    ON identity_one_time_token(expires_at);