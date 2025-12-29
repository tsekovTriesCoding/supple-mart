-- V2: Insert initial admin and test users

INSERT INTO users (id, email, password, first_name, last_name, role, auth_provider, created_at, updated_at)
VALUES (
    UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440001'),
    'admin@supplemart.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqY6s5n5jTqEeLNfUfqKnFuDC4jjW',
    'Admin',
    'User',
    'ADMIN',
    'LOCAL',
    NOW(),
    NOW()
);

INSERT INTO users (id, email, password, first_name, last_name, role, auth_provider, created_at, updated_at)
VALUES
(
    UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440002'),
    'john.doe@email.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqY6s5n5jTqEeLNfUfqKnFuDC4jjW',
    'John',
    'Doe',
    'CUSTOMER',
    'LOCAL',
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440003'),
    'jane.smith@email.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqY6s5n5jTqEeLNfUfqKnFuDC4jjW',
    'Jane',
    'Smith',
    'CUSTOMER',
    'LOCAL',
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440004'),
    'mike.johnson@email.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqY6s5n5jTqEeLNfUfqKnFuDC4jjW',
    'Mike',
    'Johnson',
    'CUSTOMER',
    'LOCAL',
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440005'),
    'sarah.wilson@email.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqY6s5n5jTqEeLNfUfqKnFuDC4jjW',
    'Sarah',
    'Wilson',
    'CUSTOMER',
    'LOCAL',
    NOW(),
    NOW()
);
