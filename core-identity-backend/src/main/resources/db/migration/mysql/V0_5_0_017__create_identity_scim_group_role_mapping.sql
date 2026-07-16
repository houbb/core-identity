CREATE TABLE IF NOT EXISTS identity_scim_group_role_mapping (
    id           VARCHAR(36)  NOT NULL,
    group_id     VARCHAR(36)  NOT NULL,
    role_id      VARCHAR(36)  NOT NULL,
    mapping_mode VARCHAR(30)  NOT NULL DEFAULT 'ADD_ONLY',
    status       VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_by   VARCHAR(36),
    created_at   BIGINT       NOT NULL,
    updated_at   BIGINT       NOT NULL,
    version      BIGINT       NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE UNIQUE INDEX uq_identity_sgrm_group_role ON identity_scim_group_role_mapping(group_id, role_id);
