-- V0.3.0.001: Extend identity_organization for P2
ALTER TABLE identity_organization ADD COLUMN owner_user_id VARCHAR(36);
ALTER TABLE identity_organization ADD COLUMN description VARCHAR(500);
ALTER TABLE identity_organization ADD COLUMN logo_object_id VARCHAR(36);
ALTER TABLE identity_organization ADD COLUMN suspended_at BIGINT;
ALTER TABLE identity_organization ADD COLUMN suspended_reason VARCHAR(500);
ALTER TABLE identity_organization ADD COLUMN deletion_requested_at BIGINT;
ALTER TABLE identity_organization ADD COLUMN deletion_effective_at BIGINT;
ALTER TABLE identity_organization ADD COLUMN authorization_version BIGINT NOT NULL DEFAULT 1;

CREATE INDEX IF NOT EXISTS idx_identity_organization_owner
    ON identity_organization(owner_user_id);
