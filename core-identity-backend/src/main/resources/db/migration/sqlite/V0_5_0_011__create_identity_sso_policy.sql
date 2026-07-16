CREATE TABLE IF NOT EXISTS identity_sso_policy (
    id                         TEXT    NOT NULL,
    organization_id            TEXT    NOT NULL,
    enforcement_mode           TEXT    NOT NULL DEFAULT 'OPTIONAL',
    connection_ids_json        TEXT,
    grace_period_ends_at       INTEGER,
    local_login_allowed        INTEGER NOT NULL DEFAULT 1,
    require_sso_for_privileged INTEGER NOT NULL DEFAULT 1,
    break_glass_required       INTEGER NOT NULL DEFAULT 0,
    status                     TEXT    NOT NULL DEFAULT 'DRAFT',
    published_at               INTEGER,
    created_by                 TEXT,
    created_at                 INTEGER NOT NULL,
    updated_at                 INTEGER NOT NULL,
    version                    INTEGER NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uq_identity_sp_org ON identity_sso_policy(organization_id);
