-- V0.3.0.004: Backfill P1 data for P2 migration
-- Copy personal_owner_user_id to owner_user_id for PERSONAL orgs
UPDATE identity_organization
SET owner_user_id = personal_owner_user_id,
    authorization_version = 1
WHERE organization_type = 'PERSONAL' AND owner_user_id IS NULL;

-- Set source and joined_at for existing memberships
UPDATE identity_membership
SET source = 'OWNER_CREATED',
    joined_at = created_at
WHERE source IS NULL AND membership_type = 'OWNER';

UPDATE identity_membership
SET source = 'INVITATION',
    joined_at = created_at
WHERE source IS NULL AND membership_type = 'MEMBER';
