-- V0.1.0.001: Create identity_instance_metadata table
CREATE TABLE IF NOT EXISTS identity_instance_metadata (
    instance_id     VARCHAR(36)  NOT NULL PRIMARY KEY,
    instance_name   VARCHAR(100) NOT NULL,
    installation_id VARCHAR(36)  NOT NULL,
    edition         VARCHAR(30)  NOT NULL DEFAULT 'COMMUNITY',
    current_version VARCHAR(30)  NOT NULL,
    schema_version  VARCHAR(50)  NOT NULL,
    installed_at    BIGINT       NOT NULL,
    last_started_at BIGINT       NOT NULL,
    created_at      BIGINT       NOT NULL,
    updated_at      BIGINT       NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;