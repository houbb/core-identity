CREATE TABLE IF NOT EXISTS identity_external_identity (
    id                   VARCHAR(36)  NOT NULL,
    user_id              VARCHAR(36),
    organization_id      VARCHAR(36)  NOT NULL,
    connection_id        VARCHAR(36)  NOT NULL,
    external_subject     VARCHAR(500) NOT NULL,
    external_username    VARCHAR(320),
    external_email       VARCHAR(320),
    external_employee_id VARCHAR(255),
    status               VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    management_source    VARCHAR(20),
    claims_snapshot_json TEXT,
    first_login_at       BIGINT,
    last_login_at        BIGINT,
    linked_at            BIGINT,
    unlinked_at          BIGINT,
    created_at           BIGINT       NOT NULL,
    updated_at           BIGINT       NOT NULL,
    version              BIGINT       NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE UNIQUE INDEX uq_identity_ei_conn_sub ON identity_external_identity(connection_id, external_subject);
CREATE INDEX idx_identity_ei_user ON identity_external_identity(user_id);
CREATE INDEX idx_identity_ei_org ON identity_external_identity(organization_id);
