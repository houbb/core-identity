-- V0.3.0.023: Migrate P1 membership_type to P2 role system
-- Create built-in roles for each PERSONAL org that doesn't have them yet
INSERT INTO identity_role (id, organization_id, role_key, name, description, role_type, status, system_protected, sort_order, created_by, created_at, updated_at, version)
SELECT
    hex(randomblob(16)), o.id, 'OWNER', 'Owner', '组织所有者 - 拥有完全管理权限', 'BUILT_IN', 'ACTIVE', 1, 0, 'system', o.created_at, o.created_at, 1
FROM identity_organization o
WHERE o.organization_type = 'PERSONAL'
  AND NOT EXISTS (SELECT 1 FROM identity_role r WHERE r.organization_id = o.id AND r.role_key = 'OWNER');

INSERT INTO identity_role (id, organization_id, role_key, name, description, role_type, status, system_protected, sort_order, created_by, created_at, updated_at, version)
SELECT
    hex(randomblob(16)), o.id, 'ADMIN', 'Administrator', '管理员 - 管理成员、角色和设置', 'BUILT_IN', 'ACTIVE', 1, 1, 'system', o.created_at, o.created_at, 1
FROM identity_organization o
WHERE o.organization_type = 'PERSONAL'
  AND NOT EXISTS (SELECT 1 FROM identity_role r WHERE r.organization_id = o.id AND r.role_key = 'ADMIN');

INSERT INTO identity_role (id, organization_id, role_key, name, description, role_type, status, system_protected, sort_order, created_by, created_at, updated_at, version)
SELECT
    hex(randomblob(16)), o.id, 'MEMBER', 'Member', '成员 - 基础访问权限', 'BUILT_IN', 'ACTIVE', 1, 2, 'system', o.created_at, o.created_at, 1
FROM identity_organization o
WHERE o.organization_type = 'PERSONAL'
  AND NOT EXISTS (SELECT 1 FROM identity_role r WHERE r.organization_id = o.id AND r.role_key = 'MEMBER');

INSERT INTO identity_role (id, organization_id, role_key, name, description, role_type, status, system_protected, sort_order, created_by, created_at, updated_at, version)
SELECT
    hex(randomblob(16)), o.id, 'VIEWER', 'Viewer', '观察者 - 只读访问权限', 'BUILT_IN', 'ACTIVE', 1, 3, 'system', o.created_at, o.created_at, 1
FROM identity_organization o
WHERE o.organization_type = 'PERSONAL'
  AND NOT EXISTS (SELECT 1 FROM identity_role r WHERE r.organization_id = o.id AND r.role_key = 'VIEWER');

-- Map existing P1 membership_type='OWNER' to MembershipRole (bind OWNER role)
INSERT INTO identity_membership_role (membership_id, role_id, assigned_by, created_at)
SELECT m.id, r.id, 'system', m.created_at
FROM identity_membership m
INNER JOIN identity_organization o ON m.organization_id = o.id
INNER JOIN identity_role r ON r.organization_id = o.id AND r.role_key = 'OWNER'
WHERE m.membership_type = 'OWNER'
  AND NOT EXISTS (SELECT 1 FROM identity_membership_role mr WHERE mr.membership_id = m.id AND mr.role_id = r.id);
