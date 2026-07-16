ALTER TABLE identity_session
    ADD COLUMN IF NOT EXISTS authentication_source   VARCHAR(50),
    ADD COLUMN IF NOT EXISTS federation_connection_id VARCHAR(36),
    ADD COLUMN IF NOT EXISTS external_identity_id     VARCHAR(36);
