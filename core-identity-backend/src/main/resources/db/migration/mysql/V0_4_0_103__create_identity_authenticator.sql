CREATE TABLE IF NOT EXISTS identity_authenticator (
    id                        VARCHAR(36)  NOT NULL,
    user_id                   VARCHAR(36)  NOT NULL,
    authenticator_type        VARCHAR(30)  NOT NULL,
    name                      VARCHAR(150),
    status                    VARCHAR(30)  NOT NULL DEFAULT 'PENDING',
    assurance_level           VARCHAR(30)  NOT NULL DEFAULT 'AUTH_LEVEL_1',
    phishing_resistant        INTEGER      NOT NULL DEFAULT 0,
    user_verification_capable INTEGER      NOT NULL DEFAULT 0,
    enrolled_at               BIGINT,
    last_used_at              BIGINT,
    compromised_at            BIGINT,
    revoked_at                BIGINT,
    created_at                BIGINT       NOT NULL,
    updated_at                BIGINT       NOT NULL,
    version                   BIGINT       NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_identity_auth_user_status ON identity_authenticator(user_id, status);
CREATE INDEX idx_identity_auth_user_type ON identity_authenticator(user_id, authenticator_type);
CREATE INDEX idx_identity_auth_last_used ON identity_authenticator(last_used_at);
