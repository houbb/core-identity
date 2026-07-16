CREATE TABLE IF NOT EXISTS identity_sod_policy (
    id               VARCHAR(36)  NOT NULL,
    organization_id  VARCHAR(36),
    name             VARCHAR(150) NOT NULL,
    policy_type      VARCHAR(30)  NOT NULL DEFAULT 'STATIC',
    enforcement_mode VARCHAR(30)  NOT NULL DEFAULT 'DENY',
    status           VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    owner_user_id    VARCHAR(36),
    created_at       BIGINT       NOT NULL,
    updated_at       BIGINT       NOT NULL,
    version          BIGINT       NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_identity_sod_policy_org ON identity_sod_policy(organization_id);
CREATE INDEX idx_identity_sod_policy_status ON identity_sod_policy(status);