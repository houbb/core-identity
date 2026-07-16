-- V0.7.0.020: Create identity_runtime_lease table (P7.4)
CREATE TABLE IF NOT EXISTS identity_runtime_lease (
    lease_name    VARCHAR(150) NOT NULL PRIMARY KEY,
    owner_node_id VARCHAR(36)  NOT NULL,
    fencing_token BIGINT       NOT NULL DEFAULT 1,
    acquired_at   BIGINT       NOT NULL,
    heartbeat_at  BIGINT       NOT NULL,
    locked_until  BIGINT       NOT NULL,
    payload_json  TEXT         NULL,
    version       BIGINT       NOT NULL DEFAULT 1
);

CREATE INDEX idx_lease_owner ON identity_runtime_lease(owner_node_id);
CREATE INDEX idx_lease_locked_until ON identity_runtime_lease(locked_until);