-- V6: Insert initial orders

INSERT INTO orders (id, user_id, order_number, total_amount, status, shipping_address, created_at, updated_at)
VALUES
(
    UUID_TO_BIN('950e8400-e29b-41d4-a716-446655440001'),
    UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440002'),
    'ORD-2024-001',
    179.97,
    'DELIVERED',
    '123 Main St, New York, NY 10001',
    '2024-01-15 10:30:00',
    '2024-01-20 14:00:00'
),
(
    UUID_TO_BIN('950e8400-e29b-41d4-a716-446655440002'),
    UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440003'),
    'ORD-2024-002',
    76.97,
    'DELIVERED',
    '456 Oak Ave, Los Angeles, CA 90001',
    '2024-01-18 14:20:00',
    '2024-01-23 11:00:00'
),
(
    UUID_TO_BIN('950e8400-e29b-41d4-a716-446655440003'),
    UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440004'),
    'ORD-2024-003',
    74.98,
    'SHIPPED',
    '789 Pine Rd, Chicago, IL 60601',
    '2024-01-22 09:15:00',
    '2024-01-24 16:00:00'
),
(
    UUID_TO_BIN('950e8400-e29b-41d4-a716-446655440004'),
    UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440005'),
    'ORD-2024-004',
    69.98,
    'PROCESSING',
    '321 Elm St, Houston, TX 77001',
    '2024-01-24 16:45:00',
    '2024-01-25 08:00:00'
),
(
    UUID_TO_BIN('950e8400-e29b-41d4-a716-446655440005'),
    UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440002'),
    'ORD-2024-005',
    54.99,
    'PAID',
    '123 Main St, New York, NY 10001',
    '2024-01-25 12:00:00',
    '2024-01-25 12:00:00'
);
