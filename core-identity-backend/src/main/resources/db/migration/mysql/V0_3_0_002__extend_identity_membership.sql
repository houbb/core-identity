-- V0.3.0.002: Extend identity_membership for P2 (MySQL)
ALTER TABLE identity_membership
    ADD COLUMN source VARCHAR(30) NULL AFTER status,
    ADD COLUMN left_at BIGINT NULL,
    ADD COLUMN removed_at BIGINT NULL,
    ADD COLUMN suspended_at BIGINT NULL,
    ADD COLUMN last_accessed_at BIGINT NULL,
    ADD COLUMN created_by VARCHAR(36) NULL;

CREATE INDEX idx_identity_membership_last_accessed ON identity_membership(last_accessed_at);
