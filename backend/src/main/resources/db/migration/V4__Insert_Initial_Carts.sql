-- Insert initial carts for existing users
INSERT INTO carts (id, user_id, created_at, updated_at) VALUES
(UNHEX(REPLACE('750e8400-e29b-41d4-a716-446655440001', '-', '')), UNHEX(REPLACE('550e8400-e29b-41d4-a716-446655440002', '-', '')), NOW(), NOW()), -- John Doe's cart
(UNHEX(REPLACE('750e8400-e29b-41d4-a716-446655440002', '-', '')), UNHEX(REPLACE('550e8400-e29b-41d4-a716-446655440003', '-', '')), NOW(), NOW()), -- Jane Smith's cart
(UNHEX(REPLACE('750e8400-e29b-41d4-a716-446655440003', '-', '')), UNHEX(REPLACE('550e8400-e29b-41d4-a716-446655440004', '-', '')), NOW(), NOW()), -- Mike Johnson's cart
(UNHEX(REPLACE('750e8400-e29b-41d4-a716-446655440004', '-', '')), UNHEX(REPLACE('550e8400-e29b-41d4-a716-446655440005', '-', '')), NOW(), NOW()); -- Sarah Wilson's cart

