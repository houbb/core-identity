-- V0.2.0.005: Create identity_membership table
CREATE TABLE IF NOT EXISTS identity_membership (
    id              VARCHAR(36) NOT NULL,
    organization_id VARCHAR(36) NOT NULL,
    user_id         VARCHAR(36) NOT NULL,
    membership_type VARCHAR(30) NOT NULL DEFAULT 'OWNER',
    status          VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    joined_at       BIGINT      NOT NULL,
    created_at      BIGINT      NOT NULL,
    updated_at      BIGINT      NOT NULL,
    version         BIGINT      NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE UNIQUE INDEX uq_identity_membership_org_user ON identity_membership(organization_id, user_id);
CREATE INDEX idx_identity_membership_user ON identity_membership(user_id);
CREATE INDEX idx_identity_membership_org ON identity_membership(organization_id);