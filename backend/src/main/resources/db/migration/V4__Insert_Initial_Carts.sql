-- V4: Insert initial carts for users

INSERT INTO carts (id, user_id, created_at, updated_at)
VALUES
(
    UUID_TO_BIN('750e8400-e29b-41d4-a716-446655440001'),
    UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440002'),
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('750e8400-e29b-41d4-a716-446655440002'),
    UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440003'),
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('750e8400-e29b-41d4-a716-446655440003'),
    UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440004'),
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('750e8400-e29b-41d4-a716-446655440004'),
    UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440005'),
    NOW(),
    NOW()
);
