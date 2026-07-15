-- V0.3.0.011: Create identity_permission_source table
CREATE TABLE IF NOT EXISTS identity_permission_source (
    id               VARCHAR(36)  NOT NULL PRIMARY KEY,
    service_name     VARCHAR(100) NOT NULL,
    manifest_version VARCHAR(30),
    checksum         VARCHAR(128),
    last_synced_at   BIGINT,
    last_synced_by   VARCHAR(100),
    status           VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    created_at       BIGINT       NOT NULL,
    updated_at       BIGINT       NOT NULL,
    version          BIGINT       NOT NULL DEFAULT 1
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_identity_permission_source_service
    ON identity_permission_source(service_name);
