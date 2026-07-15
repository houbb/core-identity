-- V0.3.0.011: Create identity_permission_source table (MySQL)
CREATE TABLE IF NOT EXISTS identity_permission_source (
    id               VARCHAR(36)  NOT NULL,
    service_name     VARCHAR(100) NOT NULL,
    manifest_version VARCHAR(30),
    checksum         VARCHAR(128),
    last_synced_at   BIGINT,
    last_synced_by   VARCHAR(100),
    status           VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    created_at       BIGINT       NOT NULL,
    updated_at       BIGINT       NOT NULL,
    version          BIGINT       NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE UNIQUE INDEX uq_identity_permission_source_service ON identity_permission_source(service_name);
