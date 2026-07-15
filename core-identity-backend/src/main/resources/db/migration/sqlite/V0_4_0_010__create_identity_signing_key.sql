-- V0.4.0.010: Create identity_signing_key table
CREATE TABLE IF NOT EXISTS identity_signing_key (
    id                    VARCHAR(36)  NOT NULL PRIMARY KEY,
    key_id                VARCHAR(100) NOT NULL,
    algorithm             VARCHAR(30)  NOT NULL DEFAULT 'RS256',
    public_key            TEXT         NOT NULL,
    encrypted_private_key TEXT         NOT NULL,
    status                VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    active_from           BIGINT,
    retire_after          BIGINT,
    created_at            BIGINT       NOT NULL,
    updated_at            BIGINT       NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_identity_signing_key_key_id
    ON identity_signing_key(key_id);
CREATE INDEX IF NOT EXISTS idx_identity_signing_key_status
    ON identity_signing_key(status);