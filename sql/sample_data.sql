-- ============================================
-- Sample Data for BKIS (Course Management System)
-- Insert: 1 Teacher, 2 Students, 1 Course, Enrollments, Payments, Lessons, Videos
-- ============================================

-- 1. Insert Teacher (Instructor)
INSERT INTO users (id, username,full_name, email, password_hash, role, created_at, updated_at) VALUES
('teacher-001', 'john_doe','john_doe', 'john@bkis.edu', '$2a$10$example_hash_teacher', 'INSTRUCTOR', NOW(), NOW());

-- 2. Insert Students  
INSERT INTO users (id, username,full_name,  email, password_hash, role, created_at, updated_at) VALUES
('studentDto-001', 'alice_smith','alice_smith', 'alice@bkis.edu', '$2a$10$example_hash_student', 'STUDENT', NOW(), NOW()),
('studentDto-002', 'bob_johnson','bob_johnson', 'bob@bkis.edu', '$2a$10$example_hash_student', 'STUDENT', NOW(), NOW()),
('studentDto-003', 'carol_white','carol_white', 'carol@bkis.edu', '$2a$10$example_hash_student', 'STUDENT', NOW(), NOW());

select * from users where full_name = 'john_doe';
-- 3. Insert Course (with 3 enrolled studentDtos)
INSERT INTO courses (title, description, teacher_id, price, total_students, active_flag, tag, image_url, rating, created_at, updated_at) VALUES
(
    'Java Spring Boot Masterclass',
    'Learn Spring Boot framework from basics to advanced. This comprehensive course covers REST APIs, databases, authentication, and deployment strategies. Perfect for beginners and intermediate developers.',
    'teacher-001',
    149.99,
    3,
    1,
    '#java',
    '/img/course-java.jpg',
    5,
    NOW(),
    NOW()
);

-- Get the inserted course ID (should be 1 or higher)
SET @course_id = LAST_INSERT_ID();

-- 4. Insert Lessons for the Course
INSERT INTO lessons (course_id, title, description, position, created_at, updated_at) VALUES
(@course_id, 'Introduction to Spring Boot', 'Learn the basics of Spring Boot framework setup and configuration', 1, NOW(), NOW()),
(@course_id, 'REST APIs with Spring Boot', 'Build RESTful APIs using Spring Boot annotations', 2, NOW(), NOW()),
(@course_id, 'Database Integration', 'Connect your Spring Boot app to MySQL databases using JPA/Hibernate', 3, NOW(), NOW());

-- 5. Insert Lesson Videos
INSERT INTO lesson_videos (lesson_id, title, video_url, duration, position, created_at, updated_at) VALUES
(1, 'What is Spring Boot?', 'https://example.com/videos/spring-intro-1.mp4', 450, 1, NOW(), NOW()),
(1, 'Setting up Development Environment', 'https://example.com/videos/spring-intro-2.mp4', 520, 2, NOW(), NOW()),
(2, 'Creating Your First REST Endpoint', 'https://example.com/videos/spring-rest-1.mp4', 380, 1, NOW(), NOW()),
(2, 'Request Mapping and HTTP Methods', 'https://example.com/videos/spring-rest-2.mp4', 420, 2, NOW(), NOW()),
(3, 'JPA and Entity Mapping', 'https://example.com/videos/spring-db-1.mp4', 550, 1, NOW(), NOW()),
(3, 'CRUD Operations with Repository', 'https://example.com/videos/spring-db-2.mp4', 480, 2, NOW(), NOW());

-- 6. Insert Payments for Students
INSERT INTO payments (student_id, course_id, amount, status, created_at, updated_at) VALUES
('studentDto-001', @course_id, 149.99, 'COMPLETED', NOW(), NOW()),
('studentDto-002', @course_id, 149.99, 'COMPLETED', NOW(), NOW()),
('studentDto-003', @course_id, 149.99, 'COMPLETED', NOW(), NOW());

select * from payments;

-- Get payment IDs for enrollments
SET @payment_id_1 = LAST_INSERT_ID() - 2;
SET @payment_id_2 = LAST_INSERT_ID() - 1;
SET @payment_id_3 = LAST_INSERT_ID();

-- 7. Insert Enrollments (Link studentDtos to course)
INSERT INTO enrollments (student_id, course_id, payment_id, status, enrolled_at, expires_at, created_at, updated_at) VALUES
('studentDto-001', @course_id, 2, 'ACTIVE', NOW(), DATE_ADD(NOW(), INTERVAL 1 YEAR), NOW(), NOW()),
('studentDto-002', @course_id, 3, 'ACTIVE', NOW(), DATE_ADD(NOW(), INTERVAL 1 YEAR), NOW(), NOW()),
('studentDto-003', @course_id, 4, 'ACTIVE', NOW(), DATE_ADD(NOW(), INTERVAL 1 YEAR), NOW(), NOW());

-- 8. Insert Progress (Student watching videos)
INSERT INTO progress (student_id, lesson_video_id, watched_duration, is_completed, created_at, updated_at) VALUES
-- Student 1 progress
('studentDto-001', 1, 450, 1, NOW(), NOW()),      -- Completed first video
('studentDto-001', 2, 200, 0, NOW(), NOW()),      -- Partially watched second video
('studentDto-001', 3, 380, 1, NOW(), NOW()),      -- Completed third video
-- Student 2 progress
('studentDto-002', 1, 100, 0, NOW(), NOW()),      -- Just started
-- Student 3 progress
('studentDto-003', 1, 450, 1, NOW(), NOW()),
('studentDto-003', 2, 520, 1, NOW(), NOW());

select * from courses;
-- 9. Insert Course Reviews (Optional)
INSERT INTO course_reviews ( student_id,course_id, rating, comment, created_at, updated_at) VALUES
('studentDto-001', @course_id, 5, 'Excellent course! Very comprehensive and easy to follow.', NOW(), NOW()),
('studentDto-002', @course_id, 4, 'Great content, could use more real-world projects.', NOW(), NOW());

-- =============================================
-- Summary of inserted data:
-- =============================================
-- Teacher: john_doe (john@bkis.edu)
-- Students: alice_smith, bob_johnson, carol_white
-- Course: Java Spring Boot Masterclass - $149.99 (3 studentDtos enrolled)
-- Lessons: 3 lessons with 6 videos total
-- Enrollments: 3 active enrollments
-- Payments: 3 completed payments
-- Progress: Various studentDtos watching videos
-- Reviews: 2 course reviews
-- =============================================
