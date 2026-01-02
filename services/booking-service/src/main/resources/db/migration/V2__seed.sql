INSERT INTO bookings (id, user_id, facility_id, start_time, end_time, status, total_price, notes)
VALUES
    (1, 2, 1, NOW() + INTERVAL '1 day', NOW() + INTERVAL '1 day' + INTERVAL '2 hours', 'CONFIRMED', 300.00, 'Team offsite'),
    (2, 3, 2, NOW() + INTERVAL '2 days', NOW() + INTERVAL '2 days' + INTERVAL '3 hours', 'PENDING', 120.00, 'Client workshop')
ON CONFLICT (id) DO NOTHING;

SELECT setval('bookings_id_seq', (SELECT GREATEST(COALESCE(MAX(id), 0), 1) FROM bookings));
