
USE bkis_edu;

INSERT INTO users (id, username, full_name, bio, profile_picture_url, email, password_hash, role, failed_login_attempts, locked, created_by, updated_by, created_at, updated_at)
VALUES
    ('00000000-0000-0000-0000-000000000001', 'admin', 'System Admin', 'Platform administrator', '/img/team-1.jpg', 'admin@example.com', '$2a$10$Gd1VW82G9G1NsoxgtSD6kO6xt3fa4QKSGeTSwyCuZktoewDPS0eeS', 'ADMIN', 0, b'0', 'system', 'system', NOW(), NOW()),
    ('00000000-0000-0000-0000-000000000002', 'teacher1', 'Minh Nguyen', 'Lead Front-end instructor', '/img/team-1.jpg', 'teacher1@example.com', '$2a$10$Gd1VW82G9G1NsoxgtSD6kO6xt3fa4QKSGeTSwyCuZktoewDPS0eeS', 'TEACHER', 0, b'0', 'system', 'system', NOW(), NOW()),
    ('00000000-0000-0000-0000-000000000003', 'student1', 'Nguyen Van A', 'Active learner', '/img/testimonial-1.jpg', 'student1@example.com', '$2a$10$Gd1VW82G9G1NsoxgtSD6kO6xt3fa4QKSGeTSwyCuZktoewDPS0eeS', 'STUDENT', 0, b'0', 'system', 'system', NOW(), NOW())
ON DUPLICATE KEY UPDATE
    full_name = VALUES(full_name),
    bio = VALUES(bio),
    profile_picture_url = VALUES(profile_picture_url),
    updated_at = NOW();

INSERT INTO courses (id, title, description, highlights, teacher_id, price, total_students, active_flag, tag, image_url, rating, created_by, updated_by, created_at, updated_at)
VALUES
    (1, 'Web Design & Development A-Z', 'Complete web design and development journey.', 'UI foundations||Responsive layouts||JavaScript interactions||Final project', '00000000-0000-0000-0000-000000000002', 149.00, 230, b'1', '#java', '/img/course-1.jpg', 5, 'system', 'system', NOW(), NOW()),
    (2, 'Spring Boot for Beginners', 'Build backend applications using Spring Boot.', 'REST API||JPA||Security basics||Deployment', '00000000-0000-0000-0000-000000000002', 129.00, 180, b'1', '#java', '/img/course-2.jpg', 5, 'system', 'system', NOW(), NOW())
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    highlights = VALUES(highlights),
    updated_at = NOW();

INSERT INTO lessons (id, course_id, title, description, position, created_by, updated_by, created_at, updated_at)
VALUES
    (1, 1, 'HTML & CSS Foundation', 'Setup and structure page layouts with modern CSS.', 1, 'system', 'system', NOW(), NOW()),
    (2, 1, 'Bootstrap Components', 'Use Bootstrap grid and reusable UI components.', 2, 'system', 'system', NOW(), NOW()),
    (3, 1, 'Interactive JavaScript', 'Add dynamic behavior and plugin integrations.', 3, 'system', 'system', NOW(), NOW())
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    position = VALUES(position),
    updated_at = NOW();

INSERT INTO lesson_videos (id, lesson_id, title, video_url, duration, position, created_by, updated_by, created_at, updated_at)
VALUES
    (1, 1, 'Course Introduction', 'https://www.youtube.com/embed/1Rs2ND1ryYc', 12, 1, 'system', 'system', NOW(), NOW()),
    (2, 1, 'Responsive Layout Basics', 'https://www.youtube.com/embed/fYq5PXgSsbE', 18, 2, 'system', 'system', NOW(), NOW()),
    (3, 2, 'Bootstrap Grid Deep Dive', 'https://www.youtube.com/embed/0ik6X4DJKCc', 15, 1, 'system', 'system', NOW(), NOW()),
    (4, 3, 'DOM Essentials', 'https://www.youtube.com/embed/AeUCNkvzYqQ', 9, 1, 'system', 'system', NOW(), NOW())
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    video_url = VALUES(video_url),
    duration = VALUES(duration),
    position = VALUES(position),
    updated_at = NOW();

INSERT INTO payments (id, student_id, course_id, amount, status, created_by, updated_by, created_at, updated_at)
VALUES
    (1, '00000000-0000-0000-0000-000000000003', 1, 149.00, 'COMPLETED', 'system', 'system', NOW(), NOW())
ON DUPLICATE KEY UPDATE
    amount = VALUES(amount),
    status = VALUES(status),
    updated_at = NOW();

INSERT INTO enrollments (id, student_id, course_id, payment_id, status, enrolled_at, expires_at, created_by, updated_by, created_at, updated_at)
VALUES
    (1, '00000000-0000-0000-0000-000000000003', 1, 1, 'ACTIVE', NOW(), DATE_ADD(NOW(), INTERVAL 365 DAY), 'system', 'system', NOW(), NOW())
ON DUPLICATE KEY UPDATE
    status = VALUES(status),
    expires_at = VALUES(expires_at),
    updated_at = NOW();

INSERT INTO course_reviews (id, course_id, student_id, rating, comment, created_by, updated_by, created_at, updated_at)
VALUES
    (1, 1, '00000000-0000-0000-0000-000000000003', 5, 'Great learning journey and practical exercises.', 'system', 'system', NOW(), NOW())
ON DUPLICATE KEY UPDATE
    rating = VALUES(rating),
    comment = VALUES(comment),
    updated_at = NOW();

INSERT INTO progress (id, student_id, lesson_video_id, watched_duration, is_completed, created_by, updated_by, created_at, updated_at)
VALUES
    (1, '00000000-0000-0000-0000-000000000003', 1, 12, b'1', 'system', 'system', NOW(), NOW()),
    (2, '00000000-0000-0000-0000-000000000003', 2, 10, b'0', 'system', 'system', NOW(), NOW())
ON DUPLICATE KEY UPDATE
    watched_duration = VALUES(watched_duration),
    is_completed = VALUES(is_completed),
    updated_at = NOW();
