INSERT INTO payments (id, booking_id, user_id, amount, currency, status, description, payment_method, completed_at)
VALUES
    (1, 1, 2, 300.00, 'EUR', 'SUCCEEDED', 'Payment for Main Hall booking', 'card', NOW()),
    (2, 2, 3, 120.00, 'EUR', 'PENDING', 'Payment for Conference Room booking', 'card', NULL)
ON CONFLICT (id) DO NOTHING;

SELECT setval('payments_id_seq', (SELECT GREATEST(COALESCE(MAX(id), 0), 1) FROM payments));
