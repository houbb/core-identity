ALTER TABLE identity_session ADD COLUMN authentication_source TEXT;
ALTER TABLE identity_session ADD COLUMN federation_connection_id TEXT;
ALTER TABLE identity_session ADD COLUMN external_identity_id TEXT;
