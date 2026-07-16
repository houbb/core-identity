CREATE TABLE IF NOT EXISTS identity_jit_policy (
    id                     VARCHAR(36)  NOT NULL,
    connection_id          VARCHAR(36)  NOT NULL,
    status                 VARCHAR(30)  NOT NULL DEFAULT 'ENABLED',
    allow_new_users        INTEGER      NOT NULL DEFAULT 1,
    allow_existing_link    INTEGER      NOT NULL DEFAULT 0,
    require_verified_email INTEGER      NOT NULL DEFAULT 1,
    allowed_domains_json   TEXT,
    default_role_ids_json  TEXT,
    sync_profile_on_login  INTEGER      NOT NULL DEFAULT 1,
    sync_groups_on_login   INTEGER      NOT NULL DEFAULT 0,
    require_approval       INTEGER      NOT NULL DEFAULT 0,
    created_at             BIGINT       NOT NULL,
    updated_at             BIGINT       NOT NULL,
    version                BIGINT       NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE UNIQUE INDEX uq_identity_jit_conn ON identity_jit_policy(connection_id);
