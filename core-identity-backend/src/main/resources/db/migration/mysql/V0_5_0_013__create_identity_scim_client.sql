CREATE TABLE IF NOT EXISTS identity_scim_client (
    id                VARCHAR(36)  NOT NULL,
    organization_id   VARCHAR(36)  NOT NULL,
    connection_id     VARCHAR(36)  NOT NULL,
    name              VARCHAR(150),
    token_prefix      VARCHAR(40)  NOT NULL,
    token_hash        VARCHAR(255) NOT NULL,
    scopes_json       TEXT,
    status            VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    expires_at        BIGINT,
    ip_allowlist_json TEXT,
    last_used_at      BIGINT,
    last_used_ip      VARCHAR(64),
    created_at        BIGINT       NOT NULL,
    updated_at        BIGINT       NOT NULL,
    version           BIGINT       NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_identity_sc_org ON identity_scim_client(organization_id);
CREATE INDEX idx_identity_sc_conn ON identity_scim_client(connection_id);
CREATE INDEX idx_identity_sc_prefix ON identity_scim_client(token_prefix);
