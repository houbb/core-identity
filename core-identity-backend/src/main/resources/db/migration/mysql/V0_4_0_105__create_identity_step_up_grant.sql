-- V0.4.0.105: Create identity_step_up_grant table
CREATE TABLE IF NOT EXISTS identity_step_up_grant (
    id                   VARCHAR(36) NOT NULL,
    user_id              VARCHAR(36) NOT NULL,
    session_id           VARCHAR(36) NOT NULL,
    authentication_level VARCHAR(30) NOT NULL,
    allowed_actions_json TEXT,
    status               VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    issued_at            BIGINT      NOT NULL,
    expires_at           BIGINT      NOT NULL,
    consumed_at          BIGINT,
    created_at           BIGINT      NOT NULL,
    version              BIGINT      NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_identity_step_up_user_session ON identity_step_up_grant(user_id, session_id);
CREATE INDEX idx_identity_step_up_expires ON identity_step_up_grant(expires_at);