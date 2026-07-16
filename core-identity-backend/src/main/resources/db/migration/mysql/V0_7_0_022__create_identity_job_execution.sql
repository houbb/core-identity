-- V0.7.0.022: Create identity_job_execution table (P7.4)
CREATE TABLE IF NOT EXISTS identity_job_execution (
    id             VARCHAR(36)  NOT NULL PRIMARY KEY,
    job_id         VARCHAR(36)  NOT NULL,
    node_id        VARCHAR(36)  NOT NULL,
    attempt_number INTEGER      NOT NULL DEFAULT 1,
    status         VARCHAR(30)  NOT NULL DEFAULT 'RUNNING',
    started_at     BIGINT       NOT NULL,
    heartbeat_at   BIGINT       NOT NULL,
    completed_at   BIGINT       NULL,
    error_code     VARCHAR(100) NULL,
    error_message  TEXT         NULL,
    metrics_json   TEXT         NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_job_exec_job ON identity_job_execution(job_id);
CREATE INDEX idx_job_exec_node ON identity_job_execution(node_id);
CREATE INDEX idx_job_exec_status ON identity_job_execution(status);