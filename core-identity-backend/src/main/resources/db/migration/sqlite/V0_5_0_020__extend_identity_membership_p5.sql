-- Note: SQLite does not support ADD COLUMN IF NOT EXISTS in older versions.
-- If the columns already exist, these ALTER TABLE statements will fail.
-- For fresh databases, Flyway ensures these run before any data exists, so the columns won't exist yet.
ALTER TABLE identity_membership ADD COLUMN management_source TEXT;
ALTER TABLE identity_membership ADD COLUMN managed_by_connection_id TEXT;
ALTER TABLE identity_membership ADD COLUMN external_resource_id TEXT;
ALTER TABLE identity_membership ADD COLUMN provisioned_at INTEGER;
ALTER TABLE identity_membership ADD COLUMN deprovisioned_at INTEGER;
