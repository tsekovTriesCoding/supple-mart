-- Insert initial reviews for products from customers who have purchased them
INSERT INTO reviews (id, user_id, product_id, rating, comment, created_at, updated_at) VALUES
-- Reviews for Whey Protein Isolate
(UNHEX(REPLACE('b50e8400-e29b-41d4-a716-446655440001', '-', '')), UNHEX(REPLACE('550e8400-e29b-41d4-a716-446655440002', '-', '')), UNHEX(REPLACE('650e8400-e29b-41d4-a716-446655440001', '-', '')), 5, 'Excellent protein powder! Mixes well and tastes great. Definitely seeing results in my muscle recovery.', '2024-01-22 18:30:00', '2024-01-22 18:30:00'),
(UNHEX(REPLACE('b50e8400-e29b-41d4-a716-446655440002', '-', '')), UNHEX(REPLACE('550e8400-e29b-41d4-a716-446655440003', '-', '')), UNHEX(REPLACE('650e8400-e29b-41d4-a716-446655440001', '-', '')), 4, 'Good quality whey protein. Fast shipping and great customer service.', '2024-01-25 14:20:00', '2024-01-25 14:20:00'),

-- Reviews for Creatine Monohydrate
(UNHEX(REPLACE('b50e8400-e29b-41d4-a716-446655440003', '-', '')), UNHEX(REPLACE('550e8400-e29b-41d4-a716-446655440002', '-', '')), UNHEX(REPLACE('650e8400-e29b-41d4-a716-446655440011', '-', '')), 5, 'Pure creatine at a great price. No fillers, just quality product. Highly recommend!', '2024-01-23 10:15:00', '2024-01-23 10:15:00'),

-- Reviews for Multivitamin Complex
(UNHEX(REPLACE('b50e8400-e29b-41d4-a716-446655440004', '-', '')), UNHEX(REPLACE('550e8400-e29b-41d4-a716-446655440003', '-', '')), UNHEX(REPLACE('650e8400-e29b-41d4-a716-446655440004', '-', '')), 5, 'Perfect daily multivitamin. Easy to swallow and comprehensive formula. Feel more energetic!', '2024-01-26 09:45:00', '2024-01-26 09:45:00'),

-- Reviews for Omega-3 Fish Oil
(UNHEX(REPLACE('b50e8400-e29b-41d4-a716-446655440005', '-', '')), UNHEX(REPLACE('550e8400-e29b-41d4-a716-446655440003', '-', '')), UNHEX(REPLACE('650e8400-e29b-41d4-a716-446655440019', '-', '')), 4, 'High quality fish oil. No fishy aftertaste which is great. Good value for money.', '2024-01-27 16:30:00', '2024-01-27 16:30:00'),

-- Reviews for Extreme Pre-Workout
(UNHEX(REPLACE('b50e8400-e29b-41d4-a716-446655440006', '-', '')), UNHEX(REPLACE('550e8400-e29b-41d4-a716-446655440004', '-', '')), UNHEX(REPLACE('650e8400-e29b-41d4-a716-446655440013', '-', '')), 5, 'Amazing energy boost! Perfect for intense workouts. The pump is incredible.', '2024-01-26 20:10:00', '2024-01-26 20:10:00'),

-- Reviews for BCAA Powder
(UNHEX(REPLACE('b50e8400-e29b-41d4-a716-446655440007', '-', '')), UNHEX(REPLACE('550e8400-e29b-41d4-a716-446655440004', '-', '')), UNHEX(REPLACE('650e8400-e29b-41d4-a716-446655440009', '-', '')), 4, 'Great for recovery between sets. Taste is decent and mixes well with water.', '2024-01-27 11:25:00', '2024-01-27 11:25:00'),

-- Reviews for Plant-Based Protein
(UNHEX(REPLACE('b50e8400-e29b-41d4-a716-446655440008', '-', '')), UNHEX(REPLACE('550e8400-e29b-41d4-a716-446655440005', '-', '')), UNHEX(REPLACE('650e8400-e29b-41d4-a716-446655440003', '-', '')), 5, 'As a vegan, this is perfect for my protein needs. Great texture and no chalky taste!', '2024-01-25 13:40:00', '2024-01-25 13:40:00'),

-- Reviews for Collagen Peptides
(UNHEX(REPLACE('b50e8400-e29b-41d4-a716-446655440009', '-', '')), UNHEX(REPLACE('550e8400-e29b-41d4-a716-446655440005', '-', '')), UNHEX(REPLACE('650e8400-e29b-41d4-a716-446655440021', '-', '')), 4, 'Great for skin health. Unflavored so it mixes into anything. Noticed improvements in nail strength.', '2024-01-26 08:20:00', '2024-01-26 08:20:00'),

-- Additional reviews for popular products
(UNHEX(REPLACE('b50e8400-e29b-41d4-a716-446655440010', '-', '')), UNHEX(REPLACE('550e8400-e29b-41d4-a716-446655440004', '-', '')), UNHEX(REPLACE('650e8400-e29b-41d4-a716-446655440005', '-', '')), 5, 'Essential Vitamin D3 especially during winter months. High quality supplement.', '2024-01-20 15:30:00', '2024-01-20 15:30:00'),
(UNHEX(REPLACE('b50e8400-e29b-41d4-a716-446655440011', '-', '')), UNHEX(REPLACE('550e8400-e29b-41d4-a716-446655440005', '-', '')), UNHEX(REPLACE('650e8400-e29b-41d4-a716-446655440016', '-', '')), 3, 'Decent fat burner but results are gradual. No jitters which is good.', '2024-01-19 12:15:00', '2024-01-19 12:15:00');
