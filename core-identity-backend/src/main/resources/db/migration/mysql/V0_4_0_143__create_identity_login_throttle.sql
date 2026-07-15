-- V0.4.0.143: Create identity_login_throttle table
CREATE TABLE IF NOT EXISTS identity_login_throttle (
    id                VARCHAR(36)  NOT NULL,
    dimension_type    VARCHAR(30)  NOT NULL,
    dimension_hash    VARCHAR(255) NOT NULL,
    failure_count     INTEGER      NOT NULL DEFAULT 0,
    blocked_until     BIGINT,
    last_failure_at   BIGINT,
    window_started_at BIGINT,
    updated_at        BIGINT       NOT NULL,
    version           BIGINT       NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE UNIQUE INDEX uq_identity_throttle_dim ON identity_login_throttle(dimension_type, dimension_hash);