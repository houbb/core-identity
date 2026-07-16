CREATE TABLE IF NOT EXISTS identity_platform_operator_role (
    id          TEXT    NOT NULL,
    operator_id TEXT    NOT NULL,
    role_code   TEXT    NOT NULL,
    granted_by  TEXT,
    granted_at  INTEGER NOT NULL,
    created_at  INTEGER NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX idx_identity_plat_op_role_operator ON identity_platform_operator_role(operator_id);
CREATE UNIQUE INDEX uq_identity_plat_op_role ON identity_platform_operator_role(operator_id, role_code);