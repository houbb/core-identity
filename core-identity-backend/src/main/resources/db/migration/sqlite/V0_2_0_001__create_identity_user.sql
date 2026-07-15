-- V0.2.0.001: Create identity_user table
CREATE TABLE IF NOT EXISTS identity_user (
    id               VARCHAR(36)  NOT NULL PRIMARY KEY,
    display_name     VARCHAR(120) NOT NULL DEFAULT '',
    status           VARCHAR(30)  NOT NULL DEFAULT 'PENDING_VERIFICATION',
    locale           VARCHAR(20)  DEFAULT 'zh-CN',
    timezone         VARCHAR(50)  DEFAULT 'Asia/Shanghai',
    avatar_object_id VARCHAR(36),
    last_login_at    BIGINT,
    locked_until     BIGINT,
    disabled_at      BIGINT,
    disabled_reason  VARCHAR(500),
    created_at       BIGINT       NOT NULL,
    updated_at       BIGINT       NOT NULL,
    version          BIGINT       NOT NULL DEFAULT 1
);

CREATE INDEX IF NOT EXISTS idx_identity_user_status
    ON identity_user(status);
CREATE INDEX IF NOT EXISTS idx_identity_user_created_at
    ON identity_user(created_at);
CREATE INDEX IF NOT EXISTS idx_identity_user_last_login_at
    ON identity_user(last_login_at);