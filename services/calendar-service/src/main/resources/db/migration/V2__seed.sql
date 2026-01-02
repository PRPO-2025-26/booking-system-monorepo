INSERT INTO calendar_events (id, booking_id, user_id, facility_id, title, description, start_time, end_time, location, status, sync_status)
VALUES
    (1, 1, 2, 1, 'Team Offsite', 'Offsite in Main Hall', NOW() + INTERVAL '1 day', NOW() + INTERVAL '1 day' + INTERVAL '2 hours', '123 Center St', 'SCHEDULED', 'PENDING'),
    (2, 2, 3, 2, 'Client Workshop', 'Workshop in Conference Room A', NOW() + INTERVAL '2 days', NOW() + INTERVAL '2 days' + INTERVAL '3 hours', '123 Center St', 'SCHEDULED', 'PENDING')
ON CONFLICT (id) DO NOTHING;

SELECT setval('calendar_events_id_seq', (SELECT GREATEST(COALESCE(MAX(id), 0), 1) FROM calendar_events));
