CREATE TABLE IF NOT EXISTS identity_attribute_mapping (
    id                  VARCHAR(36)  NOT NULL,
    connection_id       VARCHAR(36)  NOT NULL,
    target_attribute    VARCHAR(100) NOT NULL,
    source_attribute    VARCHAR(200),
    source_type         VARCHAR(30)  NOT NULL DEFAULT 'CLAIM',
    ownership           VARCHAR(30)  NOT NULL DEFAULT 'JIT',
    required            INTEGER      NOT NULL DEFAULT 0,
    default_value       VARCHAR(500),
    transformation_type VARCHAR(50),
    status              VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at          BIGINT       NOT NULL,
    updated_at          BIGINT       NOT NULL,
    version             BIGINT       NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_identity_am_conn ON identity_attribute_mapping(connection_id);
