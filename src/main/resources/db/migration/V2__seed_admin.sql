-- V2__seed_admin.sql
-- Seed default admin user
-- Password: Admin@123 (BCrypt encoded)

INSERT INTO users (username, email, password, role, is_active, created_at, updated_at)
VALUES (
    'admin',
    'admin@riskcalculator.com',
    '$2a$10$cYvzPQr9A814K3mNBFGzwOi05PkQHrmofUBQgoxRUybHjaUtkQ04.',
    'ADMIN',
    TRUE,
    NOW(),
    NOW()
)
ON CONFLICT (username) DO NOTHING;
