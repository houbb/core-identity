CREATE TABLE IF NOT EXISTS identity_entitlement (
    id                    TEXT    NOT NULL,
    organization_id       TEXT,
    entitlement_type      TEXT    NOT NULL,
    target_id             TEXT    NOT NULL,
    code                  TEXT    NOT NULL,
    name                  TEXT    NOT NULL,
    risk_level            TEXT    NOT NULL DEFAULT 'LOW',
    owner_user_id         TEXT,
    status                TEXT    NOT NULL DEFAULT 'ACTIVE',
    review_frequency_days INTEGER NOT NULL DEFAULT 180,
    created_at            INTEGER NOT NULL,
    updated_at            INTEGER NOT NULL,
    version               INTEGER NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uq_identity_entitlement_code ON identity_entitlement(code);
CREATE INDEX idx_identity_entitlement_org ON identity_entitlement(organization_id);
CREATE INDEX idx_identity_entitlement_type ON identity_entitlement(entitlement_type);
CREATE INDEX idx_identity_entitlement_target ON identity_entitlement(target_id);
CREATE INDEX idx_identity_entitlement_status ON identity_entitlement(status);
