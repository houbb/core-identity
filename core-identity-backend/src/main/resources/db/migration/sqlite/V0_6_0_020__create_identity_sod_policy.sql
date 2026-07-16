CREATE TABLE IF NOT EXISTS identity_sod_policy (
    id               TEXT    NOT NULL,
    organization_id  TEXT,
    name             TEXT    NOT NULL,
    policy_type      TEXT    NOT NULL DEFAULT 'STATIC',
    enforcement_mode TEXT    NOT NULL DEFAULT 'DENY',
    status           TEXT    NOT NULL DEFAULT 'ACTIVE',
    owner_user_id    TEXT,
    created_at       INTEGER NOT NULL,
    updated_at       INTEGER NOT NULL,
    version          INTEGER NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
);

CREATE INDEX idx_identity_sod_policy_org ON identity_sod_policy(organization_id);
CREATE INDEX idx_identity_sod_policy_status ON identity_sod_policy(status);