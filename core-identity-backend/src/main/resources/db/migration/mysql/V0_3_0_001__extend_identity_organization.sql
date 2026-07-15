-- V0.3.0.001: Extend identity_organization for P2 (MySQL)
ALTER TABLE identity_organization
    ADD COLUMN owner_user_id VARCHAR(36) NULL AFTER personal_owner_user_id,
    ADD COLUMN description VARCHAR(500) NULL AFTER slug,
    ADD COLUMN logo_object_id VARCHAR(36) NULL AFTER description,
    ADD COLUMN suspended_at BIGINT NULL,
    ADD COLUMN suspended_reason VARCHAR(500) NULL,
    ADD COLUMN deletion_requested_at BIGINT NULL,
    ADD COLUMN deletion_effective_at BIGINT NULL,
    ADD COLUMN authorization_version BIGINT NOT NULL DEFAULT 1;

CREATE INDEX idx_identity_organization_owner ON identity_organization(owner_user_id);
