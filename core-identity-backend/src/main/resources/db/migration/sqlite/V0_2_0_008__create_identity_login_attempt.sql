-- V0.2.0.008: Create identity_login_attempt table
CREATE TABLE IF NOT EXISTS identity_login_attempt (
    id             VARCHAR(36)  NOT NULL PRIMARY KEY,
    user_id        VARCHAR(36),
    email_hash     VARCHAR(128),
    result         VARCHAR(30)  NOT NULL,
    failure_reason VARCHAR(50),
    ip_address     VARCHAR(64),
    user_agent     VARCHAR(500),
    request_id     VARCHAR(64),
    occurred_at    BIGINT       NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_identity_login_attempt_user_time
    ON identity_login_attempt(user_id, occurred_at);
CREATE INDEX IF NOT EXISTS idx_identity_login_attempt_ip_time
    ON identity_login_attempt(ip_address, occurred_at);
CREATE INDEX IF NOT EXISTS idx_identity_login_attempt_email_time
    ON identity_login_attempt(email_hash, occurred_at);
CREATE INDEX IF NOT EXISTS idx_identity_login_attempt_result_time
    ON identity_login_attempt(result, occurred_at);