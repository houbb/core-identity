-- V0.4.0.043: Create identity_service_credential table
CREATE TABLE IF NOT EXISTS identity_service_credential (
    id                  VARCHAR(36)  NOT NULL PRIMARY KEY,
    service_account_id  VARCHAR(36)  NOT NULL,
    client_id           VARCHAR(120) NOT NULL,
    secret_prefix       VARCHAR(30)  NOT NULL,
    secret_hash         VARCHAR(255) NOT NULL,
    name                VARCHAR(100) NOT NULL,
    status              VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    expires_at          BIGINT,
    last_used_at        BIGINT,
    created_at          BIGINT       NOT NULL,
    revoked_at          BIGINT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX IF NOT EXISTS idx_identity_sc_sa ON identity_service_credential(service_account_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_identity_sc_client_id ON identity_service_credential(client_id);
CREATE INDEX IF NOT EXISTS idx_identity_sc_status ON identity_service_credential(status);
