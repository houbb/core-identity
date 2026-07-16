CREATE TABLE IF NOT EXISTS identity_sod_policy_item (
    id                   VARCHAR(36) NOT NULL,
    policy_id            VARCHAR(36) NOT NULL,
    left_entitlement_id  VARCHAR(36) NOT NULL,
    right_entitlement_id VARCHAR(36) NOT NULL,
    conflict_type        VARCHAR(30) NOT NULL DEFAULT 'MUTUAL_EXCLUSION',
    risk_level           VARCHAR(20) NOT NULL DEFAULT 'HIGH',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_identity_sod_item_policy ON identity_sod_policy_item(policy_id);
CREATE UNIQUE INDEX uq_identity_sod_item_pair ON identity_sod_policy_item(policy_id, left_entitlement_id, right_entitlement_id);