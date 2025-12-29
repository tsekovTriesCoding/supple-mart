-- V3: Insert 50 products across all categories

INSERT INTO products (id, name, description, price, image_url, category, stock_quantity, is_active, created_at, updated_at)
VALUES
-- PROTEIN Products (10 products)
(
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440001'),
    'Whey Protein Isolate 5lbs',
    'Premium whey protein isolate with 25g protein per serving. Ultra-filtered for maximum purity and fast absorption. Perfect for post-workout recovery. Contains all essential amino acids and BCAAs. Low in fat and carbohydrates.',
    79.99,
    'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1766942419/supplemart/products/g6bjcdlop1f2a9jfztcz.jpg',
    'PROTEIN',
    150,
    TRUE,
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440002'),
    'Casein Protein 2lbs',
    'Slow-release micellar casein protein ideal for overnight muscle recovery. Provides sustained amino acid release for up to 8 hours. Perfect as a bedtime shake. Rich, creamy texture with delicious flavors.',
    54.99,
    'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1766942795/supplemart/products/eppyrm9bfb9ppr53rz0u.jpg',
    'PROTEIN',
    75,
    TRUE,
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440003'),
    'Plant-Based Protein 2lbs',
    'Complete vegan protein blend from pea, rice, and hemp. Contains 24g protein per serving with all essential amino acids. Naturally sweetened and easy to digest. Perfect for plant-based athletes.',
    39.99,
    'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1766942806/supplemart/products/d5mesc0p5kt2ylqk8jza.jpg',
    'PROTEIN',
    80,
    TRUE,
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440004'),
    'Whey Protein Concentrate 5lbs',
    'High-quality whey concentrate with 22g protein per serving. Great taste and excellent value. Contains natural growth factors and immunoglobulins. Available in chocolate, vanilla, and strawberry.',
    59.99,
    'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1766942816/supplemart/products/b2sezqiklhxfigazhqq7.jpg',
    'PROTEIN',
    200,
    TRUE,
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440005'),
    'Hydrolyzed Whey Protein 3lbs',
    'Pre-digested whey protein for ultra-fast absorption. Clinically proven to enhance recovery. Ideal for athletes with sensitive digestion. 27g protein per serving with minimal lactose.',
    89.99,
    'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1766942830/supplemart/products/iogxwb05bhmo4racpsxf.jpg',
    'PROTEIN',
    60,
    TRUE,
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440006'),
    'Egg White Protein Powder 2lbs',
    'Pure egg white protein with 25g protein per serving. Paleo-friendly and lactose-free. Complete amino acid profile for optimal muscle building. Neutral taste mixes easily.',
    49.99,
    'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1766942840/supplemart/products/bllgvip61gjeecauz96e.jpg',
    'PROTEIN',
    90,
    TRUE,
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440007'),
    'Beef Protein Isolate 2lbs',
    'Premium beef protein isolate with 23g protein per serving. Zero fat, zero sugar, zero cholesterol. Paleo and keto-friendly. Rich in natural creatine and B vitamins.',
    64.99,
    'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1766942859/supplemart/products/cb1vkl5olkclhwgcoi2j.jpg',
    'PROTEIN',
    45,
    TRUE,
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440008'),
    'Soy Protein Isolate 2lbs',
    'Non-GMO soy protein isolate with 25g protein per serving. Complete plant-based protein source. Supports heart health and muscle recovery. Cholesterol-free formula.',
    34.99,
    'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1766942869/supplemart/products/dcgjxl4vjwyrsbbjvrrg.jpg',
    'PROTEIN',
    120,
    TRUE,
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440009'),
    'Multi-Protein Blend 5lbs',
    'Time-release protein blend of whey, casein, and egg. Sustained protein delivery for all-day muscle support. 26g protein per serving. Ideal for between meals.',
    69.99,
    'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1766942881/supplemart/products/m7onfgpm6rzmqqtabtch.jpg',
    'PROTEIN',
    100,
    TRUE,
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440010'),
    'Clear Whey Isolate 1.5lbs',
    'Revolutionary clear protein that drinks like juice. 20g protein per serving with refreshing fruit flavors. Perfect for hot days or those who dont like milky shakes.',
    44.99,
    'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1766942930/supplemart/products/lbci69ihujw0t2jscv8e.jpg',
    'PROTEIN',
    85,
    TRUE,
    NOW(),
    NOW()
),

-- VITAMINS Products (8 products)
(
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440011'),
    'Multivitamin Complex',
    'Complete daily multivitamin with 25 essential vitamins and minerals. Supports immune system, energy production, and overall health. One tablet daily formula with extended release technology.',
    24.99,
    'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1766943522/supplemart/products/y93nynm51ho2x5pr03d2.jpg',
    'VITAMINS',
    150,
    TRUE,
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440012'),
    'Vitamin D3 5000 IU',
    'High-potency vitamin D3 for bone health and immune support. Supports calcium absorption and muscle function. Easy-to-swallow softgels with olive oil for enhanced absorption.',
    19.99,
    'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1766943532/supplemart/products/nghwhp2ddyy9r2vpiu14.jpg',
    'VITAMINS',
    200,
    TRUE,
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440013'),
    'Vitamin C 1000mg',
    'High-potency vitamin C with rose hips and bioflavonoids. Powerful antioxidant support for immune health. Gentle on stomach formula. 120 capsules per bottle.',
    14.99,
    'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1766943541/supplemart/products/foebsmtmvwtlafhxmzlp.jpg',
    'VITAMINS',
    250,
    TRUE,
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440014'),
    'Vitamin B Complex',
    'Complete B-vitamin formula with all 8 essential B vitamins. Supports energy metabolism, nervous system, and red blood cell production. Vegetarian capsules.',
    17.99,
    'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1766943551/supplemart/products/lgmn3xrgl42am33cigf7.jpg',
    'VITAMINS',
    180,
    TRUE,
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440015'),
    'Vitamin E 400 IU',
    'Natural vitamin E (d-alpha tocopherol) for antioxidant protection. Supports skin health and cardiovascular function. Softgels for easy absorption.',
    22.99,
    'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1766943560/supplemart/products/fxbfnepckknafoutgxrv.jpg',
    'VITAMINS',
    130,
    TRUE,
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440016'),
    'Vitamin K2 MK-7 200mcg',
    'Premium vitamin K2 as MK-7 for cardiovascular and bone health. Helps direct calcium to bones where its needed. Works synergistically with vitamin D3.',
    29.99,
    'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1766943571/supplemart/products/n7h44phs3jy6pe2bry6s.jpg',
    'VITAMINS',
    95,
    TRUE,
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440017'),
    'Biotin 10000mcg',
    'High-potency biotin for hair, skin, and nail health. Supports healthy hair growth and strong nails. Also supports energy metabolism. 120 vegetarian capsules.',
    16.99,
    'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1766943578/supplemart/products/yg1fq6hzlr5wsdpvhls0.jpg',
    'VITAMINS',
    160,
    TRUE,
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440018'),
    'Vitamin A 10000 IU',
    'Premium vitamin A for vision, immune, and skin health. Derived from fish liver oil. Supports healthy cell growth and reproduction. Softgel capsules.',
    12.99,
    'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1766943589/supplemart/products/tsubdzlqd2uzj8rnkelc.jpg',
    'VITAMINS',
    140,
    TRUE,
    NOW(),
    NOW()
),

-- MINERALS Products (6 products)
(
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440019'),
    'Magnesium Glycinate 400mg',
    'Highly absorbable magnesium glycinate for muscle relaxation and sleep support. Gentle on stomach and well-tolerated. Supports over 300 enzymatic reactions in the body.',
    19.99,
    'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1767014185/supplemart/products/mfibl20npba1x7my9s4f.jpg',
    'MINERALS',
    120,
    TRUE,
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440020'),
    'Zinc Picolinate 50mg',
    'Highly bioavailable zinc picolinate for immune support and testosterone production. Supports skin health and wound healing. Essential mineral for over 100 enzymes.',
    14.99,
    'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1767014193/supplemart/products/oiyouaevpdczmc9qrgzn.jpg',
    'MINERALS',
    180,
    TRUE,
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440021'),
    'Iron Bisglycinate 25mg',
    'Gentle iron formula thats easy on the digestive system. Supports healthy red blood cell production and energy levels. Ideal for those with low iron levels.',
    11.99,
    'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1767014203/supplemart/products/qcly3gnrth6kyvyuvero.jpg',
    'MINERALS',
    150,
    TRUE,
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440022'),
    'Calcium Citrate 500mg',
    'Highly absorbable calcium citrate with vitamin D3. Supports bone density and muscle function. Can be taken with or without food. Ideal for those over 50.',
    16.99,
    'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1767014211/supplemart/products/t6ixatyni0hoajwlictn.jpg',
    'MINERALS',
    200,
    TRUE,
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440023'),
    'Potassium Gluconate 99mg',
    'Essential mineral for heart rhythm and muscle contractions. Supports healthy blood pressure levels. Helps maintain proper fluid balance.',
    9.99,
    'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1767014220/supplemart/products/lgkbea8x5ycwzoosd0d6.jpg',
    'MINERALS',
    170,
    TRUE,
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440024'),
    'Selenium 200mcg',
    'Essential trace mineral with powerful antioxidant properties. Supports thyroid function and immune health. Important for reproductive health.',
    13.99,
    'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1767014227/supplemart/products/boac7ykj8tsjum2o4ajd.jpg',
    'MINERALS',
    130,
    TRUE,
    NOW(),
    NOW()
),

-- AMINO_ACIDS Products (6 products)
(
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440025'),
    'BCAA 2:1:1 Powder 300g',
    'Branched-chain amino acids in optimal 2:1:1 ratio. Supports muscle recovery and reduces muscle soreness. Helps preserve lean muscle during training. Refreshing fruit flavors.',
    29.99,
    'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1767014726/supplemart/products/jdk5ydccrly2xth3hrem.jpg',
    'AMINO_ACIDS',
    90,
    TRUE,
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440026'),
    'L-Glutamine Powder 500g',
    'Pure L-glutamine for muscle recovery and gut health. Supports immune system during intense training. Helps reduce muscle breakdown. Unflavored for versatile use.',
    22.99,
    'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1767014733/supplemart/products/hqym3ledvxupygk56ibn.jpg',
    'AMINO_ACIDS',
    110,
    TRUE,
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440027'),
    'EAA Complex Powder 400g',
    'Complete essential amino acid formula with all 9 EAAs. Stimulates muscle protein synthesis better than BCAAs alone. Perfect for intra-workout or any time of day.',
    34.99,
    'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1767014744/supplemart/products/tsqo3w0iahynr30psbue.jpg',
    'AMINO_ACIDS',
    70,
    TRUE,
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440028'),
    'L-Arginine 500mg Capsules',
    'Amino acid that supports nitric oxide production for improved blood flow. Enhances muscle pumps during workouts. Supports cardiovascular health.',
    19.99,
    'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1767014751/supplemart/products/qgnrz858wgcsnvkl8jhv.jpg',
    'AMINO_ACIDS',
    140,
    TRUE,
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440029'),
    'L-Carnitine 1000mg',
    'Supports fat metabolism and energy production. Helps transport fatty acids to mitochondria for burning. Popular for weight management and endurance.',
    24.99,
    'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1767014759/supplemart/products/hg2gnu9zfgqocwsjpsmc.jpg',
    'AMINO_ACIDS',
    100,
    TRUE,
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440030'),
    'Beta-Alanine 750mg',
    'Increases muscle carnosine levels for enhanced endurance. Reduces muscle fatigue during high-intensity exercise. Backed by extensive clinical research.',
    21.99,
    'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1767014766/supplemart/products/jkixcme93rsthhlyanuf.jpg',
    'AMINO_ACIDS',
    85,
    TRUE,
    NOW(),
    NOW()
),

-- CREATINE Products (4 products)
(
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440031'),
    'Creatine Monohydrate 500g',
    'Pure micronized creatine monohydrate for strength and power. The most researched sports supplement ever. Supports muscle growth and workout performance. Unflavored.',
    19.99,
    'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1767015346/supplemart/products/haaxjbqqwnpdob8hzx0j.jpg',
    'CREATINE',
    200,
    TRUE,
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440032'),
    'Creatine HCl Capsules',
    'Creatine hydrochloride for enhanced solubility and absorption. No loading phase required. Gentle on stomach with no water retention. 120 capsules.',
    34.99,
    'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1767015353/supplemart/products/lbnhnvhtsfchzmbunzkb.jpg',
    'CREATINE',
    85,
    TRUE,
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440033'),
    'Creatine Ethyl Ester 1000mg',
    'Esterified creatine for improved absorption and bioavailability. No bloating or water retention. Convenient capsule form for on-the-go use.',
    29.99,
    'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1767015360/supplemart/products/epmcoqwkehwlvbjfga7n.jpg',
    'CREATINE',
    75,
    TRUE,
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440034'),
    'Buffered Creatine 750mg',
    'pH-buffered creatine (Kre-Alkalyn) for maximum stability and absorption. No conversion to creatinine. Easier on the stomach than regular creatine.',
    32.99,
    'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1767015370/supplemart/products/xtcglkfqrfmlxt6ics15.jpg',
    'CREATINE',
    65,
    TRUE,
    NOW(),
    NOW()
),

-- PRE_WORKOUT Products (4 products)
(
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440035'),
    'Extreme Pre-Workout',
    'High-stimulant pre-workout for intense energy and focus. Contains caffeine, beta-alanine, and citrulline. Explosive power and endurance. Not for beginners.',
    44.99,
    'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1767015750/supplemart/products/get9cpro6rakihpnepbm.jpg',
    'PRE_WORKOUT',
    60,
    TRUE,
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440036'),
    'Pump Pre-Workout (Stimulant-Free)',
    'Non-stimulant pre-workout for massive pumps without the jitters. Loaded with citrulline, arginine, and glycerol. Perfect for evening workouts.',
    39.99,
    'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1767015758/supplemart/products/qgbf0nztcgjqvdxozzqo.jpg',
    'PRE_WORKOUT',
    70,
    TRUE,
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440037'),
    'Focus Pre-Workout',
    'Nootropic-enhanced pre-workout for mental clarity and focus. Contains Alpha-GPC, tyrosine, and moderate caffeine. Perfect for athletes who need mental edge.',
    42.99,
    'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1767015765/supplemart/products/tvjviuu7vgeezvim81h9.jpg',
    'PRE_WORKOUT',
    55,
    TRUE,
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440038'),
    'Natural Pre-Workout',
    'Clean pre-workout with natural caffeine from green tea. No artificial colors or sweeteners. Contains beetroot and coconut water powder. Great tasting.',
    37.99,
    'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1767015771/supplemart/products/cd961xewowazmbvmryys.jpg',
    'PRE_WORKOUT',
    80,
    TRUE,
    NOW(),
    NOW()
),

-- POST_WORKOUT Products (3 products)
(
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440039'),
    'Post-Workout Recovery',
    'Complete post-workout formula with fast carbs and protein. Contains glutamine, BCAAs, and electrolytes. Accelerates recovery and replenishes glycogen stores.',
    49.99,
    'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1767016030/supplemart/products/hjrhamevkd3kwtlkce6u.jpg',
    'POST_WORKOUT',
    50,
    TRUE,
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440040'),
    'Advanced Recovery Matrix',
    'Science-backed recovery blend with HMB, tart cherry, and turmeric. Reduces muscle soreness and inflammation. Supports faster return to training.',
    54.99,
    'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1767016038/supplemart/products/k136hplyl7omjshuqtj4.jpg',
    'POST_WORKOUT',
    45,
    TRUE,
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440041'),
    'Electrolyte Recovery Powder',
    'Complete electrolyte replacement for intense training. Contains sodium, potassium, magnesium, and calcium. Sugar-free formula with natural flavors.',
    24.99,
    'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1767016045/supplemart/products/gnmgbugmkb5j8f8wlimg.jpg',
    'POST_WORKOUT',
    120,
    TRUE,
    NOW(),
    NOW()
),

-- WEIGHT_LOSS Products (4 products)
(
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440042'),
    'Fat Burner Capsules',
    'Thermogenic fat burner with green tea extract, caffeine, and cayenne. Boosts metabolism and supports appetite control. Use alongside diet and exercise.',
    34.99,
    'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1767016311/supplemart/products/vux7lnjbieuuistck7re.jpg',
    'WEIGHT_LOSS',
    95,
    TRUE,
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440043'),
    'CLA Softgels 1000mg',
    'Conjugated linoleic acid for body composition support. Helps reduce body fat while preserving lean muscle. Derived from safflower oil.',
    24.99,
    'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1767016318/supplemart/products/wozxtuiulhp7ifokquo9.jpg',
    'WEIGHT_LOSS',
    130,
    TRUE,
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440044'),
    'Green Tea Extract 500mg',
    'Standardized for EGCG catechins for metabolic support. Natural antioxidant properties. Caffeine-controlled formula. 120 vegetarian capsules.',
    18.99,
    'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1767016325/supplemart/products/hhubf7z5meji0zybrtrc.jpg',
    'WEIGHT_LOSS',
    160,
    TRUE,
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440045'),
    'Appetite Control Formula',
    'Natural appetite suppressant with glucomannan, chromium, and 5-HTP. Helps manage cravings and portion control. Stimulant-free formula.',
    27.99,
    'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1767016333/supplemart/products/sdj3rmphur14yfaznr4f.jpg',
    'WEIGHT_LOSS',
    85,
    TRUE,
    NOW(),
    NOW()
),

-- MASS_GAINER Products (2 products)
(
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440046'),
    'Mass Gainer 5lbs',
    'High-calorie mass gainer with 1250 calories per serving. Contains 50g protein and complex carbohydrates. Ideal for hardgainers looking to build size.',
    59.99,
    'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1767016476/supplemart/products/lzqfhc1rkru9smou70xi.jpg',
    'MASS_GAINER',
    40,
    TRUE,
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440047'),
    'Lean Mass Gainer 4lbs',
    'Cleaner mass gainer with lower sugar and higher protein ratio. 600 calories and 45g protein per serving. Added digestive enzymes for better absorption.',
    49.99,
    'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1767016482/supplemart/products/b3pqhgo2busw8ynvwmfd.jpg',
    'MASS_GAINER',
    55,
    TRUE,
    NOW(),
    NOW()
),

-- OMEGA_3 Products (3 products)
(
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440048'),
    'Omega-3 Fish Oil 1000mg',
    'Triple-strength fish oil with 600mg EPA and 400mg DHA per softgel. Supports heart, brain, and joint health. Molecularly distilled for purity.',
    26.99,
    'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1767016635/supplemart/products/jb4mqekfjrpqazwwczr8.jpg',
    'OMEGA_3',
    140,
    TRUE,
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440049'),
    'Vegan Omega-3 (Algae)',
    'Plant-based omega-3 from algae oil. Sustainable and ocean-friendly. Contains DHA and EPA without fish. Perfect for vegans and vegetarians.',
    32.99,
    'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1767016665/supplemart/products/xuwda0i1vvlehfby4dgf.jpg',
    'OMEGA_3',
    80,
    TRUE,
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440050'),
    'Krill Oil 1000mg',
    'Premium Antarctic krill oil with phospholipid-bound omega-3s. Enhanced absorption compared to fish oil. Contains natural astaxanthin antioxidant.',
    39.99,
    'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1767016673/supplemart/products/xxzxir9cqyni31fq0rvb.jpg',
    'OMEGA_3',
    70,
    TRUE,
    NOW(),
    NOW()
),

-- OTHER Products (4 products)
(
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440051'),
    'Collagen Peptides 500g',
    'Grass-fed hydrolyzed collagen peptides for skin, hair, and joint health. Dissolves easily in hot or cold beverages. Type I and III collagen. Unflavored.',
    29.99,
    'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1767016902/supplemart/products/hkchryb5gvlhcwa6qsrx.jpg',
    'OTHER',
    100,
    TRUE,
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440052'),
    'Probiotics 50 Billion CFU',
    'High-potency probiotic with 10 diverse strains. Supports digestive and immune health. Shelf-stable formula with delayed-release capsules.',
    39.99,
    'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1767016909/supplemart/products/sgqwe5ihs6z6bncjoger.jpg',
    'OTHER',
    110,
    TRUE,
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440053'),
    'Ashwagandha KSM-66 600mg',
    'Clinically studied ashwagandha extract for stress and cortisol management. Supports healthy testosterone levels and athletic performance. Root-only extract.',
    24.99,
    'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1767016916/supplemart/products/xdikqtl2btgjnmookzfk.jpg',
    'OTHER',
    95,
    TRUE,
    NOW(),
    NOW()
),
(
    UUID_TO_BIN('650e8400-e29b-41d4-a716-446655440054'),
    'Melatonin 5mg Gummies',
    'Natural sleep support with melatonin gummies. Helps regulate sleep-wake cycle. Great tasting berry flavor. Non-habit forming formula.',
    14.99,
    'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1767016922/supplemart/products/zgms5dldlbpgd0vzszcr.jpg',
    'OTHER',
    180,
    TRUE,
    NOW(),
    NOW()
);
