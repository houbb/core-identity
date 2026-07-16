CREATE TABLE IF NOT EXISTS identity_scim_resource (
    id                VARCHAR(36)  NOT NULL,
    connection_id     VARCHAR(36)  NOT NULL,
    resource_type     VARCHAR(30)  NOT NULL,
    external_id       VARCHAR(500) NOT NULL,
    local_resource_id VARCHAR(36),
    user_name         VARCHAR(320),
    active            INTEGER      NOT NULL DEFAULT 1,
    resource_version  BIGINT       NOT NULL DEFAULT 1,
    last_payload_hash VARCHAR(128),
    last_synced_at    BIGINT,
    created_at        BIGINT       NOT NULL,
    updated_at        BIGINT       NOT NULL,
    version           BIGINT       NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE UNIQUE INDEX uq_identity_sr_conn_ext ON identity_scim_resource(connection_id, resource_type, external_id);
CREATE INDEX idx_identity_sr_local ON identity_scim_resource(local_resource_id);