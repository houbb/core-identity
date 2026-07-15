-- V0.4.0.006: Seed scope-permission mappings for core-identity scopes
INSERT OR IGNORE INTO identity_scope_permission (id, scope_id, permission_id, created_at)
SELECT 
    'sp-' || s.id || '-' || p.id,
    s.id,
    p.id,
    0
FROM identity_scope s
CROSS JOIN identity_permission p
WHERE s.scope_code = 'identity.organization.read'
  AND p.permission_code = 'identity.organization.read';

INSERT OR IGNORE INTO identity_scope_permission (id, scope_id, permission_id, created_at)
SELECT 
    'sp-' || s.id || '-' || p.id,
    s.id,
    p.id,
    0
FROM identity_scope s
CROSS JOIN identity_permission p
WHERE s.scope_code = 'identity.organization.manage'
  AND p.permission_code IN ('identity.organization.read', 'identity.organization.update',
      'identity.organization.suspend', 'identity.organization.reactivate',
      'identity.organization.transfer_ownership');

INSERT OR IGNORE INTO identity_scope_permission (id, scope_id, permission_id, created_at)
SELECT 
    'sp-' || s.id || '-' || p.id,
    s.id,
    p.id,
    0
FROM identity_scope s
CROSS JOIN identity_permission p
WHERE s.scope_code = 'identity.member.read'
  AND p.permission_code IN ('identity.member.read');

INSERT OR IGNORE INTO identity_scope_permission (id, scope_id, permission_id, created_at)
SELECT 
    'sp-' || s.id || '-' || p.id,
    s.id,
    p.id,
    0
FROM identity_scope s
CROSS JOIN identity_permission p
WHERE s.scope_code = 'identity.member.manage'
  AND p.permission_code IN ('identity.member.read', 'identity.member.invite',
      'identity.member.update', 'identity.member.remove');

INSERT OR IGNORE INTO identity_scope_permission (id, scope_id, permission_id, created_at)
SELECT 
    'sp-' || s.id || '-' || p.id,
    s.id,
    p.id,
    0
FROM identity_scope s
CROSS JOIN identity_permission p
WHERE s.scope_code = 'identity.role.read'
  AND p.permission_code IN ('identity.role.read');

INSERT OR IGNORE INTO identity_scope_permission (id, scope_id, permission_id, created_at)
SELECT 
    'sp-' || s.id || '-' || p.id,
    s.id,
    p.id,
    0
FROM identity_scope s
CROSS JOIN identity_permission p
WHERE s.scope_code = 'identity.role.manage'
  AND p.permission_code IN ('identity.role.read', 'identity.role.create',
      'identity.role.update', 'identity.role.delete');