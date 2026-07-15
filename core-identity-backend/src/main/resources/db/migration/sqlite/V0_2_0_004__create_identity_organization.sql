-- V0.2.0.004: Create identity_organization table
CREATE TABLE IF NOT EXISTS identity_organization (
    id                     VARCHAR(36)  NOT NULL PRIMARY KEY,
    organization_type      VARCHAR(30)  NOT NULL DEFAULT 'PERSONAL',
    name                   VARCHAR(150) NOT NULL,
    slug                   VARCHAR(100),
    personal_owner_user_id VARCHAR(36),
    status                 VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    created_at             BIGINT       NOT NULL,
    updated_at             BIGINT       NOT NULL,
    version                BIGINT       NOT NULL DEFAULT 1
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_identity_organization_personal_owner
    ON identity_organization(personal_owner_user_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_identity_organization_slug
    ON identity_organization(slug);
CREATE INDEX IF NOT EXISTS idx_identity_organization_type
    ON identity_organization(organization_type);