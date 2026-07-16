-- V0.7.0.021: Create identity_distributed_job table (P7.4)
CREATE TABLE IF NOT EXISTS identity_distributed_job (
    id                  VARCHAR(36)  NOT NULL PRIMARY KEY,
    job_type            VARCHAR(100) NOT NULL,
    job_key             VARCHAR(180) NOT NULL,
    organization_id     VARCHAR(36)  NULL,
    status              VARCHAR(30)  NOT NULL DEFAULT 'PENDING',
    priority            INTEGER      NOT NULL DEFAULT 0,
    payload_json        TEXT         NULL,
    progress_current    BIGINT       NOT NULL DEFAULT 0,
    progress_total      BIGINT       NOT NULL DEFAULT 0,
    max_attempts        INTEGER      NOT NULL DEFAULT 3,
    attempt_count       INTEGER      NOT NULL DEFAULT 0,
    next_attempt_at     BIGINT       NULL,
    locked_by_node_id   VARCHAR(36)  NULL,
    lock_fencing_token  BIGINT       NOT NULL DEFAULT 0,
    locked_until        BIGINT       NULL,
    created_at          BIGINT       NOT NULL,
    started_at          BIGINT       NULL,
    completed_at        BIGINT       NULL,
    updated_at          BIGINT       NOT NULL,
    version             BIGINT       NOT NULL DEFAULT 1
);

CREATE UNIQUE INDEX uq_dist_job_key ON identity_distributed_job(job_type, job_key);
CREATE INDEX idx_dist_job_status ON identity_distributed_job(status);
CREATE INDEX idx_dist_job_next_attempt ON identity_distributed_job(next_attempt_at);