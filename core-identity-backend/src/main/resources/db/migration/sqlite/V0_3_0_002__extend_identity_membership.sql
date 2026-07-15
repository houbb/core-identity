-- V0.3.0.002: Extend identity_membership for P2
ALTER TABLE identity_membership ADD COLUMN source VARCHAR(30);
ALTER TABLE identity_membership ADD COLUMN left_at BIGINT;
ALTER TABLE identity_membership ADD COLUMN removed_at BIGINT;
ALTER TABLE identity_membership ADD COLUMN suspended_at BIGINT;
ALTER TABLE identity_membership ADD COLUMN last_accessed_at BIGINT;
ALTER TABLE identity_membership ADD COLUMN created_by VARCHAR(36);

CREATE INDEX IF NOT EXISTS idx_identity_membership_last_accessed
    ON identity_membership(last_accessed_at);
