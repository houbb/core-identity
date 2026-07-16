CREATE TABLE IF NOT EXISTS identity_attribute_mapping (
    id                  TEXT    NOT NULL,
    connection_id       TEXT    NOT NULL,
    target_attribute    TEXT    NOT NULL,
    source_attribute    TEXT,
    source_type         TEXT    NOT NULL DEFAULT 'CLAIM',
    ownership           TEXT    NOT NULL DEFAULT 'JIT',
    required            INTEGER NOT NULL DEFAULT 0,
    default_value       TEXT,
    transformation_type TEXT,
    status              TEXT    NOT NULL DEFAULT 'ACTIVE',
    created_at          INTEGER NOT NULL,
    updated_at          INTEGER NOT NULL,
    version             INTEGER NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
);

CREATE INDEX idx_identity_am_conn ON identity_attribute_mapping(connection_id);