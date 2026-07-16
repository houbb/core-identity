CREATE TABLE IF NOT EXISTS identity_jit_policy (
    id                     TEXT    NOT NULL,
    connection_id          TEXT    NOT NULL,
    status                 TEXT    NOT NULL DEFAULT 'ENABLED',
    allow_new_users        INTEGER NOT NULL DEFAULT 1,
    allow_existing_link    INTEGER NOT NULL DEFAULT 0,
    require_verified_email INTEGER NOT NULL DEFAULT 1,
    allowed_domains_json   TEXT,
    default_role_ids_json  TEXT,
    sync_profile_on_login  INTEGER NOT NULL DEFAULT 1,
    sync_groups_on_login   INTEGER NOT NULL DEFAULT 0,
    require_approval       INTEGER NOT NULL DEFAULT 0,
    created_at             INTEGER NOT NULL,
    updated_at             INTEGER NOT NULL,
    version                INTEGER NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uq_identity_jit_conn ON identity_jit_policy(connection_id);
