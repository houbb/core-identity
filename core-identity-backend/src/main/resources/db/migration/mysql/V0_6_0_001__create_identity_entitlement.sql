CREATE TABLE IF NOT EXISTS identity_entitlement (
    id                    VARCHAR(36)  NOT NULL,
    organization_id       VARCHAR(36),
    entitlement_type      VARCHAR(30)  NOT NULL,
    target_id             VARCHAR(36)  NOT NULL,
    code                  VARCHAR(180) NOT NULL,
    name                  VARCHAR(150) NOT NULL,
    risk_level            VARCHAR(20)  NOT NULL DEFAULT 'LOW',
    owner_user_id         VARCHAR(36),
    status                VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    review_frequency_days INTEGER      NOT NULL DEFAULT 180,
    created_at            BIGINT       NOT NULL,
    updated_at            BIGINT       NOT NULL,
    version               BIGINT       NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE UNIQUE INDEX uq_identity_entitlement_code ON identity_entitlement(code);
CREATE INDEX idx_identity_entitlement_org ON identity_entitlement(organization_id);
CREATE INDEX idx_identity_entitlement_type ON identity_entitlement(entitlement_type);
CREATE INDEX idx_identity_entitlement_target ON identity_entitlement(target_id);
CREATE INDEX idx_identity_entitlement_status ON identity_entitlement(status);
