-- Insert initial products across different supplement categories
INSERT INTO products (id, name, description, price, image_url, category, stock_quantity, is_active, created_at, updated_at) VALUES
-- PROTEIN supplements
(UNHEX(REPLACE('650e8400-e29b-41d4-a716-446655440001', '-', '')), 'Whey Protein Isolate 2lbs', 'Premium whey protein isolate with 25g protein per serving. Fast absorption and great taste.', 49.99, 'https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b', 'PROTEIN', 100, true, NOW(), NOW()),
(UNHEX(REPLACE('650e8400-e29b-41d4-a716-446655440002', '-', '')), 'Casein Protein 2lbs', 'Slow-digesting casein protein perfect for nighttime recovery. 24g protein per serving.', 54.99, 'https://images.unsplash.com/photo-1593095948071-474c5cc2989d', 'PROTEIN', 75, true, NOW(), NOW()),
(UNHEX(REPLACE('650e8400-e29b-41d4-a716-446655440003', '-', '')), 'Plant-Based Protein 2lbs', 'Organic pea and rice protein blend. Vegan-friendly with 22g protein per serving.', 39.99, 'https://images.unsplash.com/photo-1544967879-e-0f0580c446e', 'PROTEIN', 80, true, NOW(), NOW()),

-- VITAMINS
(UNHEX(REPLACE('650e8400-e29b-41d4-a716-446655440004', '-', '')), 'Multivitamin Complex', 'Complete daily multivitamin with 23 essential vitamins and minerals. 30-day supply.', 24.99, 'https://images.unsplash.com/photo-1584308666744-24d5c474f2ae', 'VITAMINS', 150, true, NOW(), NOW()),
(UNHEX(REPLACE('650e8400-e29b-41d4-a716-446655440005', '-', '')), 'Vitamin D3 5000 IU', 'High-potency Vitamin D3 for bone health and immune support. 90 capsules.', 16.99, 'https://images.unsplash.com/photo-1584308666744-24d5c474f2ae', 'VITAMINS', 200, true, NOW(), NOW()),
(UNHEX(REPLACE('650e8400-e29b-41d4-a716-446655440006', '-', '')), 'Vitamin C 1000mg', 'Immune support with high-potency Vitamin C. 60 tablets.', 12.99, 'https://images.unsplash.com/photo-1559757148-5c350d0d3c56', 'VITAMINS', 180, true, NOW(), NOW()),

-- MINERALS
(UNHEX(REPLACE('650e8400-e29b-41d4-a716-446655440007', '-', '')), 'Magnesium Glycinate 400mg', 'Highly bioavailable magnesium for muscle and nerve function. 90 capsules.', 19.99, 'https://images.unsplash.com/photo-1559757175-0eb30cd8c063', 'MINERALS', 120, true, NOW(), NOW()),
(UNHEX(REPLACE('650e8400-e29b-41d4-a716-446655440008', '-', '')), 'Zinc Picolinate 30mg', 'Essential mineral for immune function and protein synthesis. 60 capsules.', 14.99, 'https://images.unsplash.com/photo-1556909114-f6e7ad7d3136', 'MINERALS', 160, true, NOW(), NOW()),

-- AMINO_ACIDS
(UNHEX(REPLACE('650e8400-e29b-41d4-a716-446655440009', '-', '')), 'BCAA 2:1:1 Powder', 'Branched-chain amino acids for muscle recovery. Leucine, Isoleucine, Valine.', 29.99, 'https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b', 'AMINO_ACIDS', 90, true, NOW(), NOW()),
(UNHEX(REPLACE('650e8400-e29b-41d4-a716-446655440010', '-', '')), 'L-Glutamine Powder 500g', 'Pure L-Glutamine for muscle recovery and immune support.', 22.99, 'https://images.unsplash.com/photo-1593095948071-474c5cc2989d', 'AMINO_ACIDS', 110, true, NOW(), NOW()),

-- CREATINE
(UNHEX(REPLACE('650e8400-e29b-41d4-a716-446655440011', '-', '')), 'Creatine Monohydrate 500g', 'Pure creatine monohydrate for strength and power. Unflavored powder.', 19.99, 'https://images.unsplash.com/photo-1544966503-7cc5ac882d5e', 'CREATINE', 200, true, NOW(), NOW()),
(UNHEX(REPLACE('650e8400-e29b-41d4-a716-446655440012', '-', '')), 'Creatine HCl Capsules', 'Highly soluble creatine hydrochloride. 120 capsules.', 34.99, 'https://images.unsplash.com/photo-1584308666744-24d5c474f2ae', 'CREATINE', 85, true, NOW(), NOW()),

-- PRE_WORKOUT
(UNHEX(REPLACE('650e8400-e29b-41d4-a716-446655440013', '-', '')), 'Extreme Pre-Workout', 'High-stimulant pre-workout with caffeine, beta-alanine, and citrulline. 30 servings.', 44.99, 'https://images.unsplash.com/photo-1593095948071-474c5cc2989d', 'PRE_WORKOUT', 60, true, NOW(), NOW()),
(UNHEX(REPLACE('650e8400-e29b-41d4-a716-446655440014', '-', '')), 'Pump Pre-Workout (Stimulant-Free)', 'Non-stimulant pre-workout focused on pumps and endurance. 25 servings.', 39.99, 'https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b', 'PRE_WORKOUT', 70, true, NOW(), NOW()),

-- POST_WORKOUT
(UNHEX(REPLACE('650e8400-e29b-41d4-a716-446655440015', '-', '')), 'Post-Workout Recovery', 'Complete post-workout formula with protein, carbs, and electrolytes. 20 servings.', 49.99, 'https://images.unsplash.com/photo-1544966503-7cc5ac882d5e', 'POST_WORKOUT', 50, true, NOW(), NOW()),

-- WEIGHT_LOSS
(UNHEX(REPLACE('650e8400-e29b-41d4-a716-446655440016', '-', '')), 'Fat Burner Capsules', 'Thermogenic fat burner with green tea extract and caffeine. 60 capsules.', 34.99, 'https://images.unsplash.com/photo-1584308666744-24d5c474f2ae', 'WEIGHT_LOSS', 95, true, NOW(), NOW()),
(UNHEX(REPLACE('650e8400-e29b-41d4-a716-446655440017', '-', '')), 'CLA Softgels 1000mg', 'Conjugated Linoleic Acid for body composition support. 90 softgels.', 24.99, 'https://images.unsplash.com/photo-1584308666744-24d5c474f2ae', 'WEIGHT_LOSS', 130, true, NOW(), NOW()),

-- MASS_GAINER
(UNHEX(REPLACE('650e8400-e29b-41d4-a716-446655440018', '-', '')), 'Mass Gainer 5lbs', 'High-calorie mass gainer with 50g protein and complex carbs. Chocolate flavor.', 59.99, 'https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b', 'MASS_GAINER', 40, true, NOW(), NOW()),

-- OMEGA_3
(UNHEX(REPLACE('650e8400-e29b-41d4-a716-446655440019', '-', '')), 'Omega-3 Fish Oil 1000mg', 'Premium fish oil with EPA and DHA for heart and brain health. 120 softgels.', 26.99, 'https://images.unsplash.com/photo-1584308666744-24d5c474f2ae', 'OMEGA_3', 140, true, NOW(), NOW()),
(UNHEX(REPLACE('650e8400-e29b-41d4-a716-446655440020', '-', '')), 'Vegan Omega-3 (Algae)', 'Plant-based omega-3 from algae. Suitable for vegans. 60 capsules.', 32.99, 'https://images.unsplash.com/photo-1559757148-5c350d0d3c56', 'OMEGA_3', 80, true, NOW(), NOW()),

-- OTHER
(UNHEX(REPLACE('650e8400-e29b-41d4-a716-446655440021', '-', '')), 'Collagen Peptides 500g', 'Hydrolyzed collagen for skin, hair, and joint health. Unflavored powder.', 29.99, 'https://images.unsplash.com/photo-1544966503-7cc5ac882d5e', 'OTHER', 100, true, NOW(), NOW()),
(UNHEX(REPLACE('650e8400-e29b-41d4-a716-446655440022', '-', '')), 'Probiotics 50 Billion CFU', 'Multi-strain probiotic for digestive and immune health. 30 capsules.', 39.99, 'https://images.unsplash.com/photo-1584308666744-24d5c474f2ae', 'OTHER', 110, true, NOW(), NOW());
