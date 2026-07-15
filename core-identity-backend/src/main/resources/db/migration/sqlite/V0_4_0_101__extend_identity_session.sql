-- V0.4.0.101: Extend identity_session with P4 security columns (SQLite)
ALTER TABLE identity_session ADD COLUMN device_id VARCHAR(36);
ALTER TABLE identity_session ADD COLUMN authentication_level VARCHAR(30) NOT NULL DEFAULT 'AUTH_LEVEL_1';
ALTER TABLE identity_session ADD COLUMN authentication_methods_json TEXT;
ALTER TABLE identity_session ADD COLUMN strong_auth_at BIGINT;
ALTER TABLE identity_session ADD COLUMN risk_level VARCHAR(20) NOT NULL DEFAULT 'LOW';
ALTER TABLE identity_session ADD COLUMN reauth_required_at BIGINT;
ALTER TABLE identity_session ADD COLUMN security_version BIGINT NOT NULL DEFAULT 1;
ALTER TABLE identity_session ADD COLUMN last_risk_evaluated_at BIGINT;

CREATE INDEX idx_identity_session_auth_level ON identity_session(authentication_level);
CREATE INDEX idx_identity_session_device ON identity_session(device_id);