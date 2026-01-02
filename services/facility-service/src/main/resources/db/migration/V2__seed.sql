INSERT INTO facilities (id, name, type, address, description, capacity, price_per_hour, owner_id, available)
VALUES
    (1, 'Main Hall', 'HALL', '123 Center St, Ljubljana', 'Large hall suitable for events', 200, 150.00, 1, TRUE),
    (2, 'Conference Room A', 'MEETING_ROOM', '123 Center St, Ljubljana', 'Conference room with projector', 20, 40.00, 1, TRUE)
ON CONFLICT (id) DO NOTHING;

SELECT setval('facilities_id_seq', (SELECT GREATEST(COALESCE(MAX(id), 0), 1) FROM facilities));
