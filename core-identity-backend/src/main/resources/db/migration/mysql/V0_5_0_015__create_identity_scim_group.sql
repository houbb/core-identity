CREATE TABLE IF NOT EXISTS identity_scim_group (
    id               VARCHAR(36)  NOT NULL,
    connection_id    VARCHAR(36)  NOT NULL,
    scim_resource_id VARCHAR(36),
    external_id      VARCHAR(500),
    display_name     VARCHAR(255) NOT NULL,
    status           VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    created_at       BIGINT       NOT NULL,
    updated_at       BIGINT       NOT NULL,
    version          BIGINT       NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_identity_sg_conn ON identity_scim_group(connection_id);