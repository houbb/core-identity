-- V0.3.0.022: Create identity_membership_role table
CREATE TABLE IF NOT EXISTS identity_membership_role (
    membership_id VARCHAR(36) NOT NULL,
    role_id       VARCHAR(36) NOT NULL,
    assigned_by   VARCHAR(36),
    created_at    BIGINT      NOT NULL,
    PRIMARY KEY (membership_id, role_id)
);

CREATE INDEX IF NOT EXISTS idx_identity_membership_role_role
    ON identity_membership_role(role_id);
