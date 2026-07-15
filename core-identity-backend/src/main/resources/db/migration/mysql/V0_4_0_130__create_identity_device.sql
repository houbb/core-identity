-- V0.4.0.130: Create identity_device table
CREATE TABLE IF NOT EXISTS identity_device (
    id                 VARCHAR(36)  NOT NULL,
    user_id            VARCHAR(36)  NOT NULL,
    device_cookie_hash VARCHAR(255),
    display_name       VARCHAR(150),
    browser            VARCHAR(100),
    operating_system   VARCHAR(100),
    first_seen_at      BIGINT       NOT NULL,
    last_seen_at       BIGINT       NOT NULL,
    last_ip            VARCHAR(64),
    status             VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at         BIGINT       NOT NULL,
    updated_at         BIGINT       NOT NULL,
    version            BIGINT       NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_identity_device_user ON identity_device(user_id);
CREATE INDEX idx_identity_device_cookie ON identity_device(device_cookie_hash);