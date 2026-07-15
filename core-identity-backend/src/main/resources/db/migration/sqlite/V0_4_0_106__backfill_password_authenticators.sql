-- V0.4.0.106: Backfill PASSWORD authenticators from identity_credential (SQLite)
INSERT INTO identity_authenticator (id, user_id, authenticator_type, name, status, assurance_level,
    phishing_resistant, user_verification_capable, enrolled_at, last_used_at, created_at, updated_at, version)
SELECT
    c.id,
    c.user_id,
    'PASSWORD',
    'Password',
    CASE WHEN c.status = 'ACTIVE' THEN 'ACTIVE' ELSE 'REVOKED' END,
    'AUTH_LEVEL_1',
    0,
    0,
    c.password_changed_at,
    c.password_changed_at,
    c.created_at,
    c.updated_at,
    1
FROM identity_credential c
WHERE c.credential_type = 'PASSWORD'
  AND NOT EXISTS (SELECT 1 FROM identity_authenticator a WHERE a.id = c.id);