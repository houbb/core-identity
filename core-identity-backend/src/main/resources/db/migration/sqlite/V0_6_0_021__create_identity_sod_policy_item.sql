CREATE TABLE IF NOT EXISTS identity_sod_policy_item (
    id                   TEXT    NOT NULL,
    policy_id            TEXT    NOT NULL,
    left_entitlement_id  TEXT    NOT NULL,
    right_entitlement_id TEXT    NOT NULL,
    conflict_type        TEXT    NOT NULL DEFAULT 'MUTUAL_EXCLUSION',
    risk_level           TEXT    NOT NULL DEFAULT 'HIGH',
    PRIMARY KEY (id)
);

CREATE INDEX idx_identity_sod_item_policy ON identity_sod_policy_item(policy_id);
CREATE UNIQUE INDEX uq_identity_sod_item_pair ON identity_sod_policy_item(policy_id, left_entitlement_id, right_entitlement_id);