CREATE TABLE IF NOT EXISTS identity_provisioning_job (
    id              VARCHAR(36)  NOT NULL,
    organization_id VARCHAR(36)  NOT NULL,
    connection_id   VARCHAR(36)  NOT NULL,
    job_type        VARCHAR(50)  NOT NULL,
    status          VARCHAR(30)  NOT NULL DEFAULT 'PENDING',
    total_items     INTEGER      NOT NULL DEFAULT 0,
    success_items   INTEGER      NOT NULL DEFAULT 0,
    failed_items    INTEGER      NOT NULL DEFAULT 0,
    started_at      BIGINT,
    completed_at    BIGINT,
    created_at      BIGINT       NOT NULL,
    updated_at      BIGINT       NOT NULL,
    version         BIGINT       NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_identity_pj_org ON identity_provisioning_job(organization_id);
CREATE INDEX idx_identity_pj_conn ON identity_provisioning_job(connection_id);
