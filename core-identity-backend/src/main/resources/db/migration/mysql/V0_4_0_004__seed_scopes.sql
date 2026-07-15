-- V0.4.0.004: Seed core identity scopes
INSERT IGNORE INTO identity_scope (id, scope_code, source_service, audience_code, name, description, risk_level, consent_display, assignable, status, created_at, updated_at, version)
VALUES
('scope-openid', 'openid', 'core-identity', NULL, '身份标识', '确认你的身份，获取唯一用户 ID', 'LOW', '确认你的身份', 1, 'ACTIVE', 0, 0, 1),
('scope-profile', 'profile', 'core-identity', NULL, '个人资料', '查看你的显示名称和头像', 'LOW', '查看你的个人资料', 1, 'ACTIVE', 0, 0, 1),
('scope-email', 'email', 'core-identity', NULL, '邮箱地址', '查看你的邮箱地址', 'LOW', '查看你的邮箱地址', 1, 'ACTIVE', 0, 0, 1),
('scope-organization', 'organization', 'core-identity', NULL, '组织信息', '查看你所属的组织信息', 'LOW', '查看你的组织信息', 1, 'ACTIVE', 0, 0, 1),
('scope-offline-access', 'offline_access', 'core-identity', NULL, '离线访问', '允许应用在你不在线时访问数据', 'MEDIUM', '允许应用在后台持续访问', 1, 'ACTIVE', 0, 0, 1),
('scope-identity-org-read', 'identity.organization.read', 'core-identity', 'core-identity', '查看组织', '查看组织基本信息', 'LOW', '查看组织信息', 1, 'ACTIVE', 0, 0, 1),
('scope-identity-org-manage', 'identity.organization.manage', 'core-identity', 'core-identity', '管理组织', '修改组织设置、转移所有权', 'HIGH', '管理组织设置', 1, 'ACTIVE', 0, 0, 1),
('scope-identity-member-read', 'identity.member.read', 'core-identity', 'core-identity', '查看成员', '查看组织成员列表', 'LOW', '查看成员列表', 1, 'ACTIVE', 0, 0, 1),
('scope-identity-member-manage', 'identity.member.manage', 'core-identity', 'core-identity', '管理成员', '邀请、移除、管理成员角色', 'HIGH', '管理组织成员', 1, 'ACTIVE', 0, 0, 1),
('scope-identity-role-read', 'identity.role.read', 'core-identity', 'core-identity', '查看角色', '查看组织角色列表', 'LOW', '查看角色信息', 1, 'ACTIVE', 0, 0, 1),
('scope-identity-role-manage', 'identity.role.manage', 'core-identity', 'core-identity', '管理角色', '创建、修改、删除角色', 'HIGH', '管理角色和权限', 1, 'ACTIVE', 0, 0, 1),
('scope-storage-read', 'storage.read', 'core-storage', 'core-storage', '查看文件', '查看文件和文件夹列表', 'LOW', '查看你的文件和文件夹', 1, 'ACTIVE', 0, 0, 1),
('scope-storage-write', 'storage.write', 'core-storage', 'core-storage', '上传修改文件', '上传和修改文件', 'MEDIUM', '上传和修改文件', 1, 'ACTIVE', 0, 0, 1),
('scope-storage-delete', 'storage.delete', 'core-storage', 'core-storage', '删除文件', '删除文件和文件夹', 'HIGH', '删除你的文件', 1, 'ACTIVE', 0, 0, 1),
('scope-billing-read', 'billing.read', 'core-billing', 'core-billing', '查看账单', '查看账单和使用统计', 'LOW', '查看账单信息', 1, 'ACTIVE', 0, 0, 1),
('scope-billing-manage', 'billing.manage', 'core-billing', 'core-billing', '管理账单', '修改付款方式、订阅方案', 'HIGH', '管理账单和订阅', 1, 'ACTIVE', 0, 0, 1),
('scope-ai-use', 'ai.use', 'core-ai-gateway', 'core-ai-gateway', '使用 AI', '调用 AI 模型进行推理', 'MEDIUM', '使用 AI 能力', 1, 'ACTIVE', 0, 0, 1),
('scope-ai-provider-manage', 'ai.provider.manage', 'core-ai-gateway', 'core-ai-gateway', '管理 AI 提供商', '管理 AI 提供商配置', 'HIGH', '管理 AI 提供商', 1, 'ACTIVE', 0, 0, 1),
('scope-workflow-execute', 'workflow.execute', 'core-workflow', 'core-workflow', '执行工作流', '运行自动化工作流', 'MEDIUM', '执行工作流', 1, 'ACTIVE', 0, 0, 1),
('scope-notification-send', 'notification.send', 'core-notification', 'core-notification', '发送通知', '通过平台发送通知', 'MEDIUM', '发送通知', 1, 'ACTIVE', 0, 0, 1);