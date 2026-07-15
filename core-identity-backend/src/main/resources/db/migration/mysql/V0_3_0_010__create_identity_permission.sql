-- V0.3.0.010: Create identity_permission table (MySQL)
CREATE TABLE IF NOT EXISTS identity_permission (
    id              VARCHAR(36)  NOT NULL,
    permission_code VARCHAR(150) NOT NULL,
    source_service  VARCHAR(100) NOT NULL,
    resource        VARCHAR(100) NOT NULL,
    action          VARCHAR(100) NOT NULL,
    name            VARCHAR(150) NOT NULL,
    description     VARCHAR(500),
    risk_level      VARCHAR(20)  NOT NULL DEFAULT 'LOW',
    assignable      INTEGER      NOT NULL DEFAULT 1,
    status          VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    source_version  VARCHAR(30),
    created_at      BIGINT       NOT NULL,
    updated_at      BIGINT       NOT NULL,
    version         BIGINT       NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE UNIQUE INDEX uq_identity_permission_code ON identity_permission(permission_code);
CREATE INDEX idx_identity_permission_service_status ON identity_permission(source_service, status);
CREATE INDEX idx_identity_permission_resource ON identity_permission(resource);
CREATE INDEX idx_identity_permission_risk ON identity_permission(risk_level);
