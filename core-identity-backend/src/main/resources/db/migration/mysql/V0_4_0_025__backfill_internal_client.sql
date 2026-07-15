-- V0.4.0.025: Backfill internal_client to oauth_client (unify)
INSERT IGNORE INTO identity_oauth_client (id, client_id, owner_type, owner_id, client_type, name, description, status, review_status, consent_required, created_by, created_at, updated_at, version)
SELECT 
    id,
    client_id,
    'PLATFORM',
    'platform',
    'CONFIDENTIAL',
    display_name,
    'Migrated from identity_internal_client',
    CASE status WHEN 'ACTIVE' THEN 'ACTIVE' ELSE 'SUSPENDED' END,
    'APPROVED',
    0,
    'system',
    created_at,
    updated_at,
    version
FROM identity_internal_client;

-- Backfill client secrets
INSERT IGNORE INTO identity_oauth_client_secret (id, client_id, secret_prefix, secret_hash, name, status, expires_at, last_used_at, created_at, revoked_at)
SELECT 
    CONCAT(id, '-sec'),
    id,
    SUBSTR(client_id, 1, 8),
    client_secret_hash,
    'Default Secret',
    CASE status WHEN 'ACTIVE' THEN 'ACTIVE' ELSE 'REVOKED' END,
    expires_at,
    last_used_at,
    created_at,
    CASE WHEN status != 'ACTIVE' THEN updated_at ELSE NULL END
FROM identity_internal_client;

-- Backfill scopes
INSERT IGNORE INTO identity_oauth_client_scope (id, client_id, scope_id, created_at)
SELECT 
    CONCAT('cs-', c.id, '-scope-sys'),
    c.id,
    'scope-identity-org-read',
    0
FROM identity_internal_client c
WHERE NOT EXISTS (SELECT 1 FROM identity_oauth_client_scope cs WHERE cs.client_id = c.id);

-- Drop old internal_client table
DROP TABLE IF EXISTS identity_internal_client;