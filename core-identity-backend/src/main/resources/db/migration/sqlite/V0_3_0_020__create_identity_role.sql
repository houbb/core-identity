-- V0.3.0.020: Create identity_role table
CREATE TABLE IF NOT EXISTS identity_role (
    id               VARCHAR(36)  NOT NULL PRIMARY KEY,
    organization_id  VARCHAR(36)  NOT NULL,
    role_key         VARCHAR(80)  NOT NULL,
    name             VARCHAR(120) NOT NULL,
    description      VARCHAR(500),
    role_type        VARCHAR(30)  NOT NULL DEFAULT 'CUSTOM',
    status           VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    system_protected INTEGER      NOT NULL DEFAULT 0,
    sort_order       INTEGER      NOT NULL DEFAULT 0,
    created_by       VARCHAR(36),
    created_at       BIGINT       NOT NULL,
    updated_at       BIGINT       NOT NULL,
    version          BIGINT       NOT NULL DEFAULT 1
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_identity_role_org_key
    ON identity_role(organization_id, role_key);
CREATE UNIQUE INDEX IF NOT EXISTS uq_identity_role_org_name
    ON identity_role(organization_id, name);
CREATE INDEX IF NOT EXISTS idx_identity_role_org
    ON identity_role(organization_id);
