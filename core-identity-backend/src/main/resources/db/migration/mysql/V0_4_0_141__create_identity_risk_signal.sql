-- V0.4.0.141: Create identity_risk_signal table
CREATE TABLE IF NOT EXISTS identity_risk_signal (
    id                VARCHAR(36)  NOT NULL,
    assessment_id     VARCHAR(36)  NOT NULL,
    signal_type       VARCHAR(80)  NOT NULL,
    signal_value_hash VARCHAR(255),
    weight            INTEGER      NOT NULL DEFAULT 0,
    result            VARCHAR(30),
    metadata_json     TEXT,
    created_at        BIGINT       NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_identity_risk_signal_assessment ON identity_risk_signal(assessment_id);