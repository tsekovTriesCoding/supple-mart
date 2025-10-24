-- Insert initial cart items for existing carts
INSERT INTO cart_items (id, cart_id, product_id, quantity, price) VALUES
-- John Doe's cart items
(UNHEX(REPLACE('850e8400-e29b-41d4-a716-446655440001', '-', '')), UNHEX(REPLACE('750e8400-e29b-41d4-a716-446655440001', '-', '')), UNHEX(REPLACE('650e8400-e29b-41d4-a716-446655440001', '-', '')), 1, 49.99), -- Whey Protein
(UNHEX(REPLACE('850e8400-e29b-41d4-a716-446655440002', '-', '')), UNHEX(REPLACE('750e8400-e29b-41d4-a716-446655440001', '-', '')), UNHEX(REPLACE('650e8400-e29b-41d4-a716-446655440011', '-', '')), 1, 19.99), -- Creatine Monohydrate

-- Jane Smith's cart items
(UNHEX(REPLACE('850e8400-e29b-41d4-a716-446655440003', '-', '')), UNHEX(REPLACE('750e8400-e29b-41d4-a716-446655440002', '-', '')), UNHEX(REPLACE('650e8400-e29b-41d4-a716-446655440004', '-', '')), 2, 24.99), -- Multivitamin Complex
(UNHEX(REPLACE('850e8400-e29b-41d4-a716-446655440004', '-', '')), UNHEX(REPLACE('750e8400-e29b-41d4-a716-446655440002', '-', '')), UNHEX(REPLACE('650e8400-e29b-41d4-a716-446655440019', '-', '')), 1, 26.99), -- Omega-3 Fish Oil

-- Mike Johnson's cart items
(UNHEX(REPLACE('850e8400-e29b-41d4-a716-446655440005', '-', '')), UNHEX(REPLACE('750e8400-e29b-41d4-a716-446655440003', '-', '')), UNHEX(REPLACE('650e8400-e29b-41d4-a716-446655440013', '-', '')), 1, 44.99), -- Extreme Pre-Workout
(UNHEX(REPLACE('850e8400-e29b-41d4-a716-446655440006', '-', '')), UNHEX(REPLACE('750e8400-e29b-41d4-a716-446655440003', '-', '')), UNHEX(REPLACE('650e8400-e29b-41d4-a716-446655440009', '-', '')), 1, 29.99), -- BCAA Powder

-- Sarah Wilson's cart items
(UNHEX(REPLACE('850e8400-e29b-41d4-a716-446655440007', '-', '')), UNHEX(REPLACE('750e8400-e29b-41d4-a716-446655440004', '-', '')), UNHEX(REPLACE('650e8400-e29b-41d4-a716-446655440003', '-', '')), 1, 39.99), -- Plant-Based Protein
(UNHEX(REPLACE('850e8400-e29b-41d4-a716-446655440008', '-', '')), UNHEX(REPLACE('750e8400-e29b-41d4-a716-446655440004', '-', '')), UNHEX(REPLACE('650e8400-e29b-41d4-a716-446655440021', '-', '')), 1, 29.99); -- Collagen Peptides
