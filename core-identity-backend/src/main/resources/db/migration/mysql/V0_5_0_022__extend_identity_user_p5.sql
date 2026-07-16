ALTER TABLE identity_user
    ADD COLUMN IF NOT EXISTS primary_identity_source VARCHAR(50);
