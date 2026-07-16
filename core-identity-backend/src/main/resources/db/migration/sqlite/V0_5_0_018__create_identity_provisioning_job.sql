CREATE TABLE IF NOT EXISTS identity_provisioning_job (
    id              TEXT    NOT NULL,
    organization_id TEXT    NOT NULL,
    connection_id   TEXT    NOT NULL,
    job_type        TEXT    NOT NULL,
    status          TEXT    NOT NULL DEFAULT 'PENDING',
    total_items     INTEGER NOT NULL DEFAULT 0,
    success_items   INTEGER NOT NULL DEFAULT 0,
    failed_items    INTEGER NOT NULL DEFAULT 0,
    started_at      INTEGER,
    completed_at    INTEGER,
    created_at      INTEGER NOT NULL,
    updated_at      INTEGER NOT NULL,
    version         INTEGER NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
);

CREATE INDEX idx_identity_pj_org ON identity_provisioning_job(organization_id);
CREATE INDEX idx_identity_pj_conn ON identity_provisioning_job(connection_id);
