-- V0.7.0.001: Create identity_cluster_node table (P7.1)
CREATE TABLE IF NOT EXISTS identity_cluster_node (
    id                VARCHAR(36)  NOT NULL PRIMARY KEY,
    instance_id       VARCHAR(100) NOT NULL,
    service_type      VARCHAR(40)  NOT NULL,
    version           VARCHAR(30)  NOT NULL,
    api_version       VARCHAR(30)  NOT NULL,
    region            VARCHAR(100) NOT NULL DEFAULT 'default',
    availability_zone VARCHAR(100) NOT NULL DEFAULT 'default',
    hostname          VARCHAR(255) NOT NULL DEFAULT '',
    status            VARCHAR(30)  NOT NULL DEFAULT 'HEALTHY',
    started_at        BIGINT       NOT NULL,
    last_heartbeat_at BIGINT       NOT NULL,
    draining_at       BIGINT       NULL,
    metadata_json     TEXT         NULL,
    created_at        BIGINT       NOT NULL,
    updated_at        BIGINT       NOT NULL,
    version           BIGINT       NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_cluster_node_status ON identity_cluster_node(status);
CREATE INDEX idx_cluster_node_heartbeat ON identity_cluster_node(last_heartbeat_at);
CREATE UNIQUE INDEX uq_cluster_node_instance ON identity_cluster_node(instance_id);