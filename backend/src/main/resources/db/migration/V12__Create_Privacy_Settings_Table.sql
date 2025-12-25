-- V12__Create_Privacy_Settings_Table.sql
-- Creates the privacy_settings table for user privacy preferences

CREATE TABLE IF NOT EXISTS privacy_settings (
    id BINARY(16) PRIMARY KEY,
    user_id BINARY(16) NOT NULL UNIQUE,
    show_profile BOOLEAN NOT NULL DEFAULT TRUE,
    show_activity BOOLEAN NOT NULL DEFAULT FALSE,
    show_online_status BOOLEAN NOT NULL DEFAULT FALSE,
    share_analytics BOOLEAN NOT NULL DEFAULT FALSE,
    share_marketing BOOLEAN NOT NULL DEFAULT FALSE,
    share_third_party BOOLEAN NOT NULL DEFAULT FALSE,
    searchable BOOLEAN NOT NULL DEFAULT TRUE,
    allow_messages BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_privacy_settings_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create index for faster lookups by user_id
CREATE INDEX idx_privacy_settings_user_id ON privacy_settings(user_id);

-- Create wishlist table for user wishlist functionality
CREATE TABLE IF NOT EXISTS wishlist (
    id BINARY(16) PRIMARY KEY,
    user_id BINARY(16) NOT NULL,
    product_id BINARY(16) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_wishlist_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_wishlist_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT uk_wishlist_user_product UNIQUE (user_id, product_id)
);

-- Create indexes for wishlist lookups
CREATE INDEX idx_wishlist_user_id ON wishlist(user_id);
CREATE INDEX idx_wishlist_product_id ON wishlist(product_id);

