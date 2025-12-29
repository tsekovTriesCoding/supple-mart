-- V1: Create all database tables for SuppleMart

-- Users table
CREATE TABLE users (
    id BINARY(16) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255),
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    auth_provider VARCHAR(50) NOT NULL DEFAULT 'LOCAL',
    provider_id VARCHAR(255),
    image_url VARCHAR(500),
    created_at DATETIME,
    updated_at DATETIME
);

-- Products table
CREATE TABLE products (
    id BINARY(16) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    image_url VARCHAR(500),
    category VARCHAR(50) NOT NULL,
    stock_quantity INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME,
    updated_at DATETIME,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255)
);

-- Carts table
CREATE TABLE carts (
    id BINARY(16) PRIMARY KEY,
    user_id BINARY(16) NOT NULL,
    created_at DATETIME,
    updated_at DATETIME,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Cart Items table
CREATE TABLE cart_items (
    id BINARY(16) PRIMARY KEY,
    cart_id BINARY(16) NOT NULL,
    product_id BINARY(16) NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Orders table
CREATE TABLE orders (
    id BINARY(16) PRIMARY KEY,
    user_id BINARY(16) NOT NULL,
    order_number VARCHAR(100) NOT NULL UNIQUE,
    total_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    stripe_payment_intent_id VARCHAR(255),
    shipping_address TEXT,
    created_at DATETIME,
    updated_at DATETIME,
    last_modified_by VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Order Items table
CREATE TABLE order_items (
    id BINARY(16) PRIMARY KEY,
    order_id BINARY(16) NOT NULL,
    product_id BINARY(16) NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Reviews table
CREATE TABLE reviews (
    id BINARY(16) PRIMARY KEY,
    user_id BINARY(16) NOT NULL,
    product_id BINARY(16) NOT NULL,
    rating INT NOT NULL,
    comment TEXT,
    created_at DATETIME,
    updated_at DATETIME,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Notification Preferences table
CREATE TABLE notification_preferences (
    id BINARY(16) PRIMARY KEY,
    user_id BINARY(16) NOT NULL UNIQUE,
    order_updates BOOLEAN NOT NULL DEFAULT TRUE,
    shipping_notifications BOOLEAN NOT NULL DEFAULT TRUE,
    promotional_emails BOOLEAN NOT NULL DEFAULT TRUE,
    newsletter BOOLEAN NOT NULL DEFAULT TRUE,
    product_recommendations BOOLEAN NOT NULL DEFAULT TRUE,
    price_drop_alerts BOOLEAN NOT NULL DEFAULT TRUE,
    back_in_stock_alerts BOOLEAN NOT NULL DEFAULT TRUE,
    account_security_alerts BOOLEAN NOT NULL DEFAULT TRUE,
    password_reset_emails BOOLEAN NOT NULL DEFAULT TRUE,
    review_reminders BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME,
    updated_at DATETIME,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Privacy Settings table
CREATE TABLE privacy_settings (
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
    created_at DATETIME,
    updated_at DATETIME,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Wishlist table
CREATE TABLE wishlist (
    id BINARY(16) PRIMARY KEY,
    user_id BINARY(16) NOT NULL,
    product_id BINARY(16) NOT NULL,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    UNIQUE KEY uk_wishlist_user_product (user_id, product_id)
);

-- Hibernate Envers audit tables for Product
CREATE TABLE products_aud (
    id BINARY(16) NOT NULL,
    rev INT NOT NULL,
    revtype TINYINT,
    name VARCHAR(255),
    description TEXT,
    price DECIMAL(10,2),
    image_url VARCHAR(500),
    category VARCHAR(50),
    stock_quantity INT,
    is_active BOOLEAN,
    created_at DATETIME,
    updated_at DATETIME,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255),
    PRIMARY KEY (id, rev)
);

-- Hibernate Envers audit tables for Order
CREATE TABLE orders_aud (
    id BINARY(16) NOT NULL,
    rev INT NOT NULL,
    revtype TINYINT,
    order_number VARCHAR(100),
    total_amount DECIMAL(10,2),
    status VARCHAR(50),
    stripe_payment_intent_id VARCHAR(255),
    shipping_address TEXT,
    created_at DATETIME,
    updated_at DATETIME,
    last_modified_by VARCHAR(255),
    PRIMARY KEY (id, rev)
);

-- Hibernate Envers revision info table
CREATE TABLE revinfo (
    rev INT AUTO_INCREMENT PRIMARY KEY,
    revtstmp BIGINT
);

-- Add foreign keys for audit tables
ALTER TABLE products_aud ADD CONSTRAINT fk_products_aud_revinfo FOREIGN KEY (rev) REFERENCES revinfo(rev);
ALTER TABLE orders_aud ADD CONSTRAINT fk_orders_aud_revinfo FOREIGN KEY (rev) REFERENCES revinfo(rev);

-- Create indexes for better query performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_products_category ON products(category);
CREATE INDEX idx_products_is_active ON products(is_active);
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_reviews_product_id ON reviews(product_id);
CREATE INDEX idx_reviews_user_id ON reviews(user_id);
CREATE INDEX idx_wishlist_user_id ON wishlist(user_id);
CREATE INDEX idx_cart_items_cart_id ON cart_items(cart_id);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
