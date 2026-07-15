-- V0.4.0.042: Create identity_service_account_role table
CREATE TABLE IF NOT EXISTS identity_service_account_role (
    id                  VARCHAR(36) NOT NULL PRIMARY KEY,
    service_account_id  VARCHAR(36) NOT NULL,
    role_id             VARCHAR(36) NOT NULL,
    assigned_by         VARCHAR(36),
    created_at          BIGINT      NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX IF NOT EXISTS idx_identity_sar_sa ON identity_service_account_role(service_account_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_identity_sar_sa_role ON identity_service_account_role(service_account_id, role_id);
