-- V0.4.0.104: Create identity_authentication_challenge table
CREATE TABLE IF NOT EXISTS identity_authentication_challenge (
    id                   VARCHAR(36)  NOT NULL,
    user_id              VARCHAR(36),
    session_id           VARCHAR(36),
    challenge_type       VARCHAR(40)  NOT NULL,
    required_level       VARCHAR(30),
    allowed_methods_json TEXT,
    challenge_hash       VARCHAR(255),
    context_json         TEXT,
    status               VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    attempt_count        INTEGER      NOT NULL DEFAULT 0,
    expires_at           BIGINT       NOT NULL,
    completed_at         BIGINT,
    created_at           BIGINT       NOT NULL,
    version              BIGINT       NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_identity_challenge_user ON identity_authentication_challenge(user_id);
CREATE INDEX idx_identity_challenge_session ON identity_authentication_challenge(session_id);
CREATE INDEX idx_identity_challenge_status ON identity_authentication_challenge(status, expires_at);