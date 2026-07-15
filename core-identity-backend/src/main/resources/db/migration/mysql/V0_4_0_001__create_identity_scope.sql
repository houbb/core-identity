-- V0.4.0.001: Create identity_scope table
CREATE TABLE IF NOT EXISTS identity_scope (
    id              VARCHAR(36)  NOT NULL PRIMARY KEY,
    scope_code      VARCHAR(150) NOT NULL,
    source_service  VARCHAR(100) NOT NULL,
    audience_code   VARCHAR(120),
    name            VARCHAR(150) NOT NULL,
    description     VARCHAR(500),
    risk_level      VARCHAR(20)  NOT NULL DEFAULT 'LOW',
    consent_display VARCHAR(500),
    assignable      INTEGER      NOT NULL DEFAULT 1,
    status          VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    created_at      BIGINT       NOT NULL,
    updated_at      BIGINT       NOT NULL,
    version         BIGINT       NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE UNIQUE INDEX IF NOT EXISTS uq_identity_scope_code
    ON identity_scope(scope_code);
CREATE INDEX IF NOT EXISTS idx_identity_scope_service_status
    ON identity_scope(source_service, status);
CREATE INDEX IF NOT EXISTS idx_identity_scope_risk
    ON identity_scope(risk_level);
CREATE INDEX IF NOT EXISTS idx_identity_scope_audience
    ON identity_scope(audience_code);