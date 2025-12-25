-- Add audit columns to products table for Spring Data JPA Auditing
ALTER TABLE products
    ADD COLUMN created_by VARCHAR(255) NULL,
    ADD COLUMN last_modified_by VARCHAR(255) NULL;

-- Add audit column to orders table for Spring Data JPA Auditing
ALTER TABLE orders
    ADD COLUMN last_modified_by VARCHAR(255) NULL;

-- Create revinfo table for Hibernate Envers
CREATE TABLE revinfo (
    rev INT AUTO_INCREMENT PRIMARY KEY,
    revtstmp BIGINT
);

-- Create products_aud table for Hibernate Envers audit trail
CREATE TABLE products_aud (
    id BINARY(16) NOT NULL,
    rev INT NOT NULL,
    revtype TINYINT,
    name VARCHAR(255),
    description TEXT,
    price DECIMAL(10,2),
    image_url VARCHAR(500),
    category ENUM('PROTEIN', 'VITAMINS', 'MINERALS', 'AMINO_ACIDS', 'CREATINE', 'PRE_WORKOUT', 'POST_WORKOUT', 'WEIGHT_LOSS', 'MASS_GAINER', 'OMEGA_3', 'OTHER'),
    stock_quantity INT,
    is_active BOOLEAN,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255),
    PRIMARY KEY (id, rev),
    FOREIGN KEY (rev) REFERENCES revinfo(rev)
);

-- Create orders_aud table for Hibernate Envers audit trail
CREATE TABLE orders_aud (
    id BINARY(16) NOT NULL,
    rev INT NOT NULL,
    revtype TINYINT,
    order_number VARCHAR(50),
    total_amount DECIMAL(10,2),
    status ENUM('PENDING', 'PAID', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED'),
    stripe_payment_intent_id VARCHAR(255),
    shipping_address TEXT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    last_modified_by VARCHAR(255),
    PRIMARY KEY (id, rev),
    FOREIGN KEY (rev) REFERENCES revinfo(rev)
);

