CREATE TABLE IF NOT EXISTS identity_privileged_activation (
    id                    VARCHAR(36)   NOT NULL,
    grant_id              VARCHAR(36),
    user_id               VARCHAR(36)   NOT NULL,
    organization_id       VARCHAR(36),
    role_id               VARCHAR(36),
    reason                VARCHAR(1000),
    ticket_reference      VARCHAR(255),
    status                VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
    authentication_level  VARCHAR(30),
    session_id            VARCHAR(36),
    activated_at          BIGINT        NOT NULL,
    expires_at            BIGINT        NOT NULL,
    ended_at              BIGINT,
    created_at            BIGINT        NOT NULL,
    updated_at            BIGINT        NOT NULL,
    version               BIGINT        NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_identity_priv_act_user ON identity_privileged_activation(user_id);
CREATE INDEX idx_identity_priv_act_grant ON identity_privileged_activation(grant_id);
CREATE INDEX idx_identity_priv_act_status ON identity_privileged_activation(status);
CREATE INDEX idx_identity_priv_act_expires ON identity_privileged_activation(expires_at);