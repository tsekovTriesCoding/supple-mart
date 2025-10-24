-- Insert initial users with admin and customer roles
INSERT INTO users (id, email, password, first_name, last_name, role, created_at, updated_at) VALUES
-- Admin user (password: admin123) - Known BCrypt hash for 'admin123'
(UNHEX(REPLACE('550e8400-e29b-41d4-a716-446655440001', '-', '')), 'admin@supplemart.com', '$2a$10$CwTycUXWue0Thq9StjUM0uS3L8OJIdVmCnJHH0xVHLtUYHgqjBd/K', 'Admin', 'User', 'ADMIN', NOW(), NOW()),

-- Customer users (password: customer123) - Known BCrypt hash for 'customer123'
(UNHEX(REPLACE('550e8400-e29b-41d4-a716-446655440002', '-', '')), 'john.doe@email.com', '$2a$10$5ub7YW4oEQWEqoNSWHYO0OQ.VJIEMksO4o5P2IQxhA6LdXJGQQPkG', 'John', 'Doe', 'CUSTOMER', NOW(), NOW()),
(UNHEX(REPLACE('550e8400-e29b-41d4-a716-446655440003', '-', '')), 'jane.smith@email.com', '$2a$10$5ub7YW4oEQWEqoNSWHYO0OQ.VJIEMksO4o5P2IQxhA6LdXJGQQPkG', 'Jane', 'Smith', 'CUSTOMER', NOW(), NOW()),
(UNHEX(REPLACE('550e8400-e29b-41d4-a716-446655440004', '-', '')), 'mike.johnson@email.com', '$2a$10$5ub7YW4oEQWEqoNSWHYO0OQ.VJIEMksO4o5P2IQxhA6LdXJGQQPkG', 'Mike', 'Johnson', 'CUSTOMER', NOW(), NOW()),
(UNHEX(REPLACE('550e8400-e29b-41d4-a716-446655440005', '-', '')), 'sarah.wilson@email.com', '$2a$10$5ub7YW4oEQWEqoNSWHYO0OQ.VJIEMksO4o5P2IQxhA6LdXJGQQPkG', 'Sarah', 'Wilson', 'CUSTOMER', NOW(), NOW());
