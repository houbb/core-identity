CREATE TABLE IF NOT EXISTS identity_scim_group_role_mapping (
    id           TEXT    NOT NULL,
    group_id     TEXT    NOT NULL,
    role_id      TEXT    NOT NULL,
    mapping_mode TEXT    NOT NULL DEFAULT 'ADD_ONLY',
    status       TEXT    NOT NULL DEFAULT 'ACTIVE',
    created_by   TEXT,
    created_at   INTEGER NOT NULL,
    updated_at   INTEGER NOT NULL,
    version      INTEGER NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uq_identity_sgrm_group_role ON identity_scim_group_role_mapping(group_id, role_id);
