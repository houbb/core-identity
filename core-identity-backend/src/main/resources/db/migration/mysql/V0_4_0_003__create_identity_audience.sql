-- V0.4.0.003: Create identity_audience table
CREATE TABLE IF NOT EXISTS identity_audience (
    id                VARCHAR(36)  NOT NULL PRIMARY KEY,
    audience_code     VARCHAR(120) NOT NULL,
    service_name      VARCHAR(120) NOT NULL,
    description       VARCHAR(500),
    issuer_allowed    INTEGER      NOT NULL DEFAULT 1,
    token_ttl_seconds INTEGER      NOT NULL DEFAULT 900,
    status            VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at        BIGINT       NOT NULL,
    updated_at        BIGINT       NOT NULL,
    version           BIGINT       NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE UNIQUE INDEX IF NOT EXISTS uq_identity_audience_code
    ON identity_audience(audience_code);
CREATE INDEX IF NOT EXISTS idx_identity_audience_status
    ON identity_audience(status);