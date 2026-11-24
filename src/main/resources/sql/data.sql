-- Prepared data loaded into the database during application start.

INSERT INTO users (id, name, email) VALUES
    (1, 'John Doe', 'john.doe@example.com'),
    (2, 'Jane Smith', 'jane.smith@example.com'),
    (3, 'Bob Johnson', 'bob.johnson@example.com');

INSERT INTO events (id, title, event_date, ticket_price) VALUES
    (1, 'Spring Concert', TIMESTAMP '2026-05-01 19:00:00', 50.00),
    (2, 'Rock Festival', TIMESTAMP '2026-06-15 18:00:00', 80.00),
    (3, 'Jazz Evening', TIMESTAMP '2026-06-15 21:00:00', 35.50);

INSERT INTO tickets (id, event_id, user_id, category, place) VALUES
    (1, 1, 1, 'PREMIUM', 10),
    (2, 2, 2, 'STANDARD', 5);

INSERT INTO user_accounts (id, user_id, amount) VALUES
    (1, 1, 100.00),
    (2, 2, 20.00),
    (3, 3, 0.00);

-- Keep identity generators above the explicitly inserted ids.
ALTER TABLE users ALTER COLUMN id RESTART WITH 100;
ALTER TABLE events ALTER COLUMN id RESTART WITH 100;
ALTER TABLE tickets ALTER COLUMN id RESTART WITH 100;
ALTER TABLE user_accounts ALTER COLUMN id RESTART WITH 100;
