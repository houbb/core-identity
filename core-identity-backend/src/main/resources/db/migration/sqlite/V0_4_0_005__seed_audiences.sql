-- V0.4.0.005: Seed audiences
INSERT OR IGNORE INTO identity_audience (id, audience_code, service_name, description, issuer_allowed, token_ttl_seconds, status, created_at, updated_at, version)
VALUES
('aud-core-identity', 'core-identity', 'Core Identity', '身份与访问管理服务', 1, 900, 'ACTIVE', 0, 0, 1),
('aud-core-storage', 'core-storage', 'Core Storage', '文件存储服务', 1, 900, 'ACTIVE', 0, 0, 1),
('aud-core-billing', 'core-billing', 'Core Billing', '计费服务', 1, 600, 'ACTIVE', 0, 0, 1),
('aud-core-ai-gateway', 'core-ai-gateway', 'Core AI Gateway', 'AI 网关服务', 1, 1200, 'ACTIVE', 0, 0, 1),
('aud-core-workflow', 'core-workflow', 'Core Workflow', '工作流引擎', 1, 900, 'ACTIVE', 0, 0, 1),
('aud-core-notification', 'core-notification', 'Core Notification', '通知服务', 1, 600, 'ACTIVE', 0, 0, 1);