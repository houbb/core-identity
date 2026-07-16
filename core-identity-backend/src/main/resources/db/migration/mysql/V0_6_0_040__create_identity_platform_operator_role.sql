CREATE TABLE IF NOT EXISTS identity_platform_operator_role (
    id               VARCHAR(36)  NOT NULL,
    operator_id      VARCHAR(36)  NOT NULL,
    role_code        VARCHAR(50)  NOT NULL,
    granted_by       VARCHAR(36),
    granted_at       BIGINT       NOT NULL,
    created_at       BIGINT       NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_identity_plat_op_role_operator ON identity_platform_operator_role(operator_id);
CREATE UNIQUE INDEX uq_identity_plat_op_role ON identity_platform_operator_role(operator_id, role_code);