-- ============================================
-- Sample Data for BKIS H2 Database
-- This file is loaded automatically when using H2 in-memory DB
-- ============================================

INSERT INTO users (id, username, email, password_hash, role, created_at, updated_at) VALUES
('teacher-001', 'john_doe', 'john@bkis.edu', '$2a$10$DXJ3SW6G7P50ecc/mGQkCOYO9wjHAfUtq/mKQBtQxWXga2xJA7Z3a', 'INSTRUCTOR', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO users (id, username, email, password_hash, role, created_at, updated_at) VALUES
('student-001', 'alice_smith', 'alice@bkis.edu', '$2a$10$DXJ3SW6G7P50ecc/mGQkCOYO9wjHAfUtq/mKQBtQxWXga2xJA7Z3a', 'STUDENT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('student-002', 'bob_johnson', 'bob@bkis.edu', '$2a$10$DXJ3SW6G7P50ecc/mGQkCOYO9wjHAfUtq/mKQBtQxWXga2xJA7Z3a', 'STUDENT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('student-003', 'carol_white', 'carol@bkis.edu', '$2a$10$DXJ3SW6G7P50ecc/mGQkCOYO9wjHAfUtq/mKQBtQxWXga2xJA7Z3a', 'STUDENT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO courses (title, description, teacher_id, price, total_students, active_flag, tag, image_url, rating, created_at, updated_at) VALUES
(
    'Java Spring Boot Masterclass',
    'Learn Spring Boot framework from basics to advanced. This comprehensive course covers REST APIs, databases, authentication, and deployment strategies. Perfect for beginners and intermediate developers.',
    'teacher-001',
    149.99,
    3,
    true,
    '#java',
    '/img/course-java.jpg',
    5,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

INSERT INTO lessons (course_id, title, description, position, created_at, updated_at) VALUES
(1, 'Introduction to Spring Boot', 'Learn the basics of Spring Boot framework setup and configuration', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 'REST APIs with Spring Boot', 'Build RESTful APIs using Spring Boot annotations', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 'Database Integration', 'Connect your Spring Boot app to MySQL databases using JPA/Hibernate', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO lesson_videos (lesson_id, title, video_url, duration, position, created_at, updated_at) VALUES
(1, 'What is Spring Boot?', 'https://example.com/videos/spring-intro-1.mp4', 450, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 'Setting up Development Environment', 'https://example.com/videos/spring-intro-2.mp4', 520, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Creating Your First REST Endpoint', 'https://example.com/videos/spring-rest-1.mp4', 380, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Request Mapping and HTTP Methods', 'https://example.com/videos/spring-rest-2.mp4', 420, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'JPA and Entity Mapping', 'https://example.com/videos/spring-db-1.mp4', 550, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'CRUD Operations with Repository', 'https://example.com/videos/spring-db-2.mp4', 480, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO payments (student_id, course_id, amount, status, created_at, updated_at) VALUES
('student-001', 1, 149.99, 'COMPLETED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('student-002', 1, 149.99, 'COMPLETED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('student-003', 1, 149.99, 'COMPLETED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO enrollments (student_id, course_id, payment_id, status, enrolled_at, expires_at, created_at, updated_at) VALUES
('student-001', 1, 1, 'ACTIVE', CURRENT_TIMESTAMP, DATEADD('YEAR', 1, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('student-002', 1, 2, 'ACTIVE', CURRENT_TIMESTAMP, DATEADD('YEAR', 1, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('student-003', 1, 3, 'ACTIVE', CURRENT_TIMESTAMP, DATEADD('YEAR', 1, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO progress (student_id, lesson_video_id, watched_duration, is_completed, created_at, updated_at) VALUES
('student-001', 1, 450, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('student-001', 2, 200, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('student-001', 3, 380, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('student-002', 1, 100, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('student-003', 1, 450, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('student-003', 2, 520, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO course_reviews (course_id, student_id, rating, comment, created_at, updated_at) VALUES
(1, 'student-001', 5, 'Excellent course! Very comprehensive and easy to follow.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 'student-002', 4, 'Great content, could use more real-world projects.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
