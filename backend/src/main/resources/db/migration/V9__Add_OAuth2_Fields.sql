-- Add OAuth2 authentication fields to users table
ALTER TABLE users
    ADD COLUMN auth_provider VARCHAR(20) NOT NULL DEFAULT 'LOCAL' AFTER role,
    ADD COLUMN provider_id VARCHAR(255) NULL AFTER auth_provider,
    ADD COLUMN image_url VARCHAR(512) NULL AFTER provider_id;

-- Add index for provider_id lookups
CREATE INDEX idx_users_provider ON users(auth_provider, provider_id);
