-- Seed a dev user for login
INSERT INTO users (id, username, email, password_hash, role, created_by, updated_by, failed_login_attempts, locked)
VALUES ('00000000-0000-0000-0000-000000000001', 'admin', 'admin@example.com', '$2a$10$Gd1VW82G9G1NsoxgtSD6kO6xt3fa4QKSGeTSwyCuZktoewDPS0eeS', 'ADMIN', 'system', 'system', 0, 0);