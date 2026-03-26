-- Seed a dev user for login
INSERT INTO users (id, username, email, password_hash, role, created_by, updated_by, failed_login_attempts, locked)
VALUES ('00000000-0000-0000-0000-000000000001', 'admin', 'admin@example.com', '$2a$10$Gd1VW82G9G1NsoxgtSD6kO6xt3fa4QKSGeTSwyCuZktoewDPS0eeS', 'ADMIN', 'system', 'system', 0, 0);

-- add full_name column to users table
ALTER TABLE users ADD COLUMN full_name VARCHAR(255) AFTER username;
-- add Bio column to users table
ALTER TABLE users ADD COLUMN bio TEXT AFTER full_name;
-- add profile_picture_url column to users table
ALTER TABLE users ADD COLUMN profile_picture_url VARCHAR(255) AFTER bio;

-- add Highlights column to courses table
ALTER TABLE courses ADD COLUMN highlights TEXT AFTER description;
