CREATE TABLE IF NOT EXISTS identity_sso_policy (
    id                         VARCHAR(36)  NOT NULL,
    organization_id            VARCHAR(36)  NOT NULL,
    enforcement_mode           VARCHAR(50)  NOT NULL DEFAULT 'OPTIONAL',
    connection_ids_json        TEXT,
    grace_period_ends_at       BIGINT,
    local_login_allowed        INTEGER      NOT NULL DEFAULT 1,
    require_sso_for_privileged INTEGER      NOT NULL DEFAULT 1,
    break_glass_required       INTEGER      NOT NULL DEFAULT 0,
    status                     VARCHAR(30)  NOT NULL DEFAULT 'DRAFT',
    published_at               BIGINT,
    created_by                 VARCHAR(36),
    created_at                 BIGINT       NOT NULL,
    updated_at                 BIGINT       NOT NULL,
    version                    BIGINT       NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE UNIQUE INDEX uq_identity_sp_org ON identity_sso_policy(organization_id);
