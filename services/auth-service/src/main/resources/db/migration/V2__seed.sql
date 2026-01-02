INSERT INTO users (id, username, email, password, role, enabled)
VALUES
    (1, 'admin', 'admin@example.com', '{noop}password', 'ADMIN', TRUE),
    (2, 'alice', 'alice@example.com', '{noop}password', 'USER', TRUE),
    (3, 'bob', 'bob@example.com', '{noop}password', 'USER', TRUE)
ON CONFLICT (id) DO NOTHING;

SELECT setval('users_id_seq', (SELECT GREATEST(COALESCE(MAX(id), 0), 1) FROM users));
