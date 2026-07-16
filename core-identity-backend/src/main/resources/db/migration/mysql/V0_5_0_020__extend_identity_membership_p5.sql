ALTER TABLE identity_membership
    ADD COLUMN IF NOT EXISTS management_source       VARCHAR(20),
    ADD COLUMN IF NOT EXISTS managed_by_connection_id VARCHAR(36),
    ADD COLUMN IF NOT EXISTS external_resource_id     VARCHAR(36),
    ADD COLUMN IF NOT EXISTS provisioned_at           BIGINT,
    ADD COLUMN IF NOT EXISTS deprovisioned_at         BIGINT;
