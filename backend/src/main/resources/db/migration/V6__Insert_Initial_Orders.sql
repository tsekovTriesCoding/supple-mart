-- Insert initial orders with different statuses
INSERT INTO orders (id, user_id, order_number, total_amount, status, stripe_payment_intent_id, shipping_address, created_at, updated_at) VALUES
-- Completed orders
(UNHEX(REPLACE('950e8400-e29b-41d4-a716-446655440001', '-', '')), UNHEX(REPLACE('550e8400-e29b-41d4-a716-446655440002', '-', '')), 'ORD-2024-001', 89.97, 'DELIVERED', 'pi_1234567890abcdef', '123 Main St, Fitness City, FC 12345, USA', '2024-01-15 10:30:00', '2024-01-20 16:45:00'),
(UNHEX(REPLACE('950e8400-e29b-41d4-a716-446655440002', '-', '')), UNHEX(REPLACE('550e8400-e29b-41d4-a716-446655440003', '-', '')), 'ORD-2024-002', 76.97, 'DELIVERED', 'pi_abcdef1234567890', '456 Health Ave, Wellness Town, WT 67890, USA', '2024-01-18 14:20:00', '2024-01-23 11:30:00'),

-- Recent orders in various stages
(UNHEX(REPLACE('950e8400-e29b-41d4-a716-446655440003', '-', '')), UNHEX(REPLACE('550e8400-e29b-41d4-a716-446655440004', '-', '')), 'ORD-2024-003', 74.98, 'SHIPPED', 'pi_fedcba0987654321', '789 Strength Blvd, Muscle City, MC 54321, USA', '2024-01-22 09:15:00', '2024-01-24 13:20:00'),
(UNHEX(REPLACE('950e8400-e29b-41d4-a716-446655440004', '-', '')), UNHEX(REPLACE('550e8400-e29b-41d4-a716-446655440005', '-', '')), 'ORD-2024-004', 69.98, 'PROCESSING', 'pi_1a2b3c4d5e6f7g8h', '321 Nutrition Dr, Supplement Valley, SV 98765, USA', '2024-01-24 16:45:00', '2024-01-24 16:45:00'),
(UNHEX(REPLACE('950e8400-e29b-41d4-a716-446655440005', '-', '')), UNHEX(REPLACE('550e8400-e29b-41d4-a716-446655440002', '-', '')), 'ORD-2024-005', 54.99, 'PAID', 'pi_9h8g7f6e5d4c3b2a', '123 Main St, Fitness City, FC 12345, USA', '2024-01-25 12:00:00', '2024-01-25 12:05:00');
