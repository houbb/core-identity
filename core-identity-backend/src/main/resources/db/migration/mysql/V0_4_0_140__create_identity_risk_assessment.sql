-- V0.4.0.140: Create identity_risk_assessment table
CREATE TABLE IF NOT EXISTS identity_risk_assessment (
    id                  VARCHAR(36)  NOT NULL,
    user_id             VARCHAR(36),
    session_id          VARCHAR(36),
    operation           VARCHAR(100),
    risk_level          VARCHAR(20)  NOT NULL,
    decision            VARCHAR(40)  NOT NULL,
    required_auth_level VARCHAR(30),
    score               INTEGER      NOT NULL DEFAULT 0,
    reasons_json        TEXT,
    model_version       VARCHAR(50),
    request_id          VARCHAR(64),
    created_at          BIGINT       NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_identity_risk_user ON identity_risk_assessment(user_id);
CREATE INDEX idx_identity_risk_session ON identity_risk_assessment(session_id);
CREATE INDEX idx_identity_risk_created ON identity_risk_assessment(created_at);