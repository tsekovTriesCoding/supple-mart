-- V8: Insert initial reviews

INSERT INTO reviews (id, user_id, product_id, rating, comment, created_at, updated_at)
VALUES
(
    UUID_TO_BIN('b50e8400-e29b-41d4-a716-446655440001'),
    UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440002'),
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440001'),
    5,
    'Excellent protein! Mixes well and tastes great. I''ve tried many brands and this is by far the best whey isolate I''ve used.',
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('b50e8400-e29b-41d4-a716-446655440002'),
    UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440003'),
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440001'),
    4,
    'Good quality protein. The taste could be a bit better but the macros are on point. Fast shipping too!',
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('b50e8400-e29b-41d4-a716-446655440003'),
    UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440004'),
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440031'),
    5,
    'Best creatine on the market. No bloating and great results after just a few weeks of use.',
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('b50e8400-e29b-41d4-a716-446655440004'),
    UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440002'),
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440011'),
    5,
    'Great multivitamin! I feel more energetic since I started taking it daily. Highly recommend.',
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('b50e8400-e29b-41d4-a716-446655440005'),
    UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440003'),
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440035'),
    5,
    'Wow! This pre-workout is intense. Amazing energy and focus. The pump is unreal!',
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('b50e8400-e29b-41d4-a716-446655440006'),
    UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440005'),
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440042'),
    3,
    'Decent fat burner but nothing special. Did help with appetite control though.',
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('b50e8400-e29b-41d4-a716-446655440007'),
    UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440004'),
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440025'),
    4,
    'Great BCAAs! I use them during my workouts and recovery is noticeably better.',
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('b50e8400-e29b-41d4-a716-446655440008'),
    UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440002'),
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440003'),
    5,
    'Finally a vegan protein that tastes good! Smooth texture and mixes easily.',
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('b50e8400-e29b-41d4-a716-446655440009'),
    UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440005'),
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440048'),
    4,
    'High quality fish oil. No fishy burps and capsules are easy to swallow.',
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('b50e8400-e29b-41d4-a716-446655440010'),
    UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440003'),
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440051'),
    4,
    'Noticed improvement in my skin and joints after a month of use. Will buy again!',
    NOW(),
    NOW()
);

