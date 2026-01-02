INSERT INTO notification_logs (id, user_id, booking_id, payment_id, event_id, type, channel, recipient, subject, content, status, sent_at)
VALUES
    (1, 2, 1, 1, 1, 'BOOKING_CONFIRMATION', 'EMAIL', 'alice@example.com', 'Booking confirmed', 'Your booking for Main Hall is confirmed.', 'SENT', NOW()),
    (2, 3, 2, 2, 2, 'BOOKING_CONFIRMATION', 'EMAIL', 'bob@example.com', 'Booking pending', 'Your booking for Conference Room A is pending payment.', 'PENDING', NULL)
ON CONFLICT (id) DO NOTHING;

SELECT setval('notification_logs_id_seq', (SELECT GREATEST(COALESCE(MAX(id), 0), 1) FROM notification_logs));
