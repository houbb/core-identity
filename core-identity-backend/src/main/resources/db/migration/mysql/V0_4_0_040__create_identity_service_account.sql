-- V0.4.0.040: Create identity_service_account table
CREATE TABLE IF NOT EXISTS identity_service_account (
    id              VARCHAR(36)  NOT NULL PRIMARY KEY,
    organization_id VARCHAR(36),
    account_type    VARCHAR(30)  NOT NULL DEFAULT 'ORGANIZATION',
    name            VARCHAR(150) NOT NULL,
    description     VARCHAR(500),
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    last_used_at    BIGINT,
    created_by      VARCHAR(36),
    created_at      BIGINT       NOT NULL,
    updated_at      BIGINT       NOT NULL,
    version         BIGINT       NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX IF NOT EXISTS idx_identity_sa_org ON identity_service_account(organization_id);
CREATE INDEX IF NOT EXISTS idx_identity_sa_status ON identity_service_account(status);