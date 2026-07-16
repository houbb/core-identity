CREATE TABLE IF NOT EXISTS identity_federation_connection (
    id                  VARCHAR(36)  NOT NULL,
    connection_key      VARCHAR(120) NOT NULL,
    organization_id     VARCHAR(36)  NOT NULL,
    connection_type     VARCHAR(20)  NOT NULL,
    name                VARCHAR(150),
    status              VARCHAR(30)  NOT NULL DEFAULT 'DRAFT',
    login_button_text   VARCHAR(150),
    logo_object_id      VARCHAR(36),
    priority            INTEGER      NOT NULL DEFAULT 0,
    jit_enabled         INTEGER      NOT NULL DEFAULT 0,
    scim_enabled        INTEGER      NOT NULL DEFAULT 0,
    last_success_at     BIGINT,
    last_failure_at     BIGINT,
    last_error_code     VARCHAR(100),
    created_by          VARCHAR(36),
    created_at          BIGINT       NOT NULL,
    updated_at          BIGINT       NOT NULL,
    version             BIGINT       NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE UNIQUE INDEX uq_identity_fed_conn_key ON identity_federation_connection(connection_key);
CREATE INDEX idx_identity_fed_conn_org ON identity_federation_connection(organization_id);
CREATE INDEX idx_identity_fed_conn_status ON identity_federation_connection(status);
