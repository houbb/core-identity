CREATE TABLE IF NOT EXISTS identity_provisioning_log (
    id            TEXT    NOT NULL,
    job_id        TEXT,
    resource_type TEXT    NOT NULL,
    external_id   TEXT,
    operation     TEXT    NOT NULL,
    result        TEXT    NOT NULL DEFAULT 'SUCCESS',
    error_code    TEXT,
    error_message TEXT,
    request_id    TEXT,
    occurred_at   INTEGER NOT NULL,
    created_at    INTEGER NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX idx_identity_pl_job ON identity_provisioning_log(job_id);
