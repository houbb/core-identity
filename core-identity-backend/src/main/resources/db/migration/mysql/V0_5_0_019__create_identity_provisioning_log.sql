CREATE TABLE IF NOT EXISTS identity_provisioning_log (
    id            VARCHAR(36)   NOT NULL,
    job_id        VARCHAR(36),
    resource_type VARCHAR(30)   NOT NULL,
    external_id   VARCHAR(500),
    operation     VARCHAR(30)   NOT NULL,
    result        VARCHAR(30)   NOT NULL DEFAULT 'SUCCESS',
    error_code    VARCHAR(100),
    error_message VARCHAR(1000),
    request_id    VARCHAR(64),
    occurred_at   BIGINT        NOT NULL,
    created_at    BIGINT        NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_identity_pl_job ON identity_provisioning_log(job_id);
