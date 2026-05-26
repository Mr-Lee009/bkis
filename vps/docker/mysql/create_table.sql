USE bkis_edu;

CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(36) NOT NULL,
    username VARCHAR(100) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    bio VARCHAR(1000),
    profile_picture_url VARCHAR(255),
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    failed_login_attempts INT DEFAULT 0,
    locked BIT DEFAULT b'0',
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    created_at DATETIME,
    updated_at DATETIME,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_username (username),
    UNIQUE KEY uk_users_email (email)
);

CREATE TABLE IF NOT EXISTS user_oauth_accounts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id VARCHAR(36) NOT NULL,
    provider VARCHAR(20) NOT NULL,
    provider_user_id VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    display_name VARCHAR(255),
    profile_picture_url VARCHAR(500),
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    created_at DATETIME,
    updated_at DATETIME,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_oauth_accounts_provider_subject (provider, provider_user_id),
    KEY idx_user_oauth_accounts_user_id (user_id),
    CONSTRAINT fk_user_oauth_accounts_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id VARCHAR(36) NOT NULL,
    token VARCHAR(100) NOT NULL,
    expires_at DATETIME NOT NULL,
    used_at DATETIME,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    created_at DATETIME,
    updated_at DATETIME,
    PRIMARY KEY (id),
    UNIQUE KEY uk_password_reset_tokens_token (token),
    KEY idx_password_reset_tokens_user_id (user_id),
    CONSTRAINT fk_password_reset_tokens_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS courses (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    highlights TEXT,
    teacher_id VARCHAR(36) NOT NULL,
    price DECIMAL(19,2) NOT NULL,
    total_students INT,
    active_flag BIT DEFAULT b'1',
    course_status VARCHAR(20),
    tag VARCHAR(100),
    image_url VARCHAR(500),
    rating INT,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    created_at DATETIME,
    updated_at DATETIME,
    PRIMARY KEY (id),
    KEY idx_courses_teacher_id (teacher_id),
    CONSTRAINT fk_courses_teacher FOREIGN KEY (teacher_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS lessons (
    id BIGINT NOT NULL AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    position INT NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    created_at DATETIME,
    updated_at DATETIME,
    PRIMARY KEY (id),
    KEY idx_lessons_course_id (course_id),
    CONSTRAINT fk_lessons_course FOREIGN KEY (course_id) REFERENCES courses(id)
);

CREATE TABLE IF NOT EXISTS lesson_videos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    lesson_id BIGINT NOT NULL,
    title VARCHAR(500) NOT NULL,
    video_url VARCHAR(500) NOT NULL,
    duration INT,
    position INT NOT NULL,
    is_preview BIT DEFAULT b'0',
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    created_at DATETIME,
    updated_at DATETIME,
    PRIMARY KEY (id),
    KEY idx_lesson_videos_lesson_id (lesson_id),
    CONSTRAINT fk_lesson_videos_lesson FOREIGN KEY (lesson_id) REFERENCES lessons(id)
);

CREATE TABLE IF NOT EXISTS payments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    student_id VARCHAR(36) NOT NULL,
    course_id BIGINT NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    created_at DATETIME,
    updated_at DATETIME,
    PRIMARY KEY (id),
    KEY idx_payments_student_id (student_id),
    KEY idx_payments_course_id (course_id),
    CONSTRAINT fk_payments_student FOREIGN KEY (student_id) REFERENCES users(id),
    CONSTRAINT fk_payments_course FOREIGN KEY (course_id) REFERENCES courses(id)
);

CREATE TABLE IF NOT EXISTS payment_gateways (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL,
    display_name VARCHAR(150) NOT NULL,
    provider_type VARCHAR(50) NOT NULL,
    description VARCHAR(500),
    merchant_id VARCHAR(150),
    partner_code VARCHAR(150),
    secret_key VARCHAR(500),
    payment_endpoint VARCHAR(500),
    return_url VARCHAR(500),
    webhook_url VARCHAR(500),
    ip_allowlist TEXT,
    enabled BIT DEFAULT b'1',
    sandbox_mode BIT DEFAULT b'0',
    routing_priority INT DEFAULT 99,
    transaction_fee_percent DECIMAL(8,2) DEFAULT 0,
    success_rate_percent DECIMAL(8,2) DEFAULT 0,
    status VARCHAR(30) NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    created_at DATETIME,
    updated_at DATETIME,
    PRIMARY KEY (id),
    UNIQUE KEY uk_payment_gateways_code (code)
);

CREATE TABLE IF NOT EXISTS enrollments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    student_id VARCHAR(36) NOT NULL,
    course_id BIGINT NOT NULL,
    payment_id BIGINT,
    status VARCHAR(20) NOT NULL,
    enrolled_at DATETIME,
    expires_at DATETIME,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    created_at DATETIME,
    updated_at DATETIME,
    PRIMARY KEY (id),
    KEY idx_enrollments_student_id (student_id),
    KEY idx_enrollments_course_id (course_id),
    KEY idx_enrollments_payment_id (payment_id),
    CONSTRAINT fk_enrollments_student FOREIGN KEY (student_id) REFERENCES users(id),
    CONSTRAINT fk_enrollments_course FOREIGN KEY (course_id) REFERENCES courses(id),
    CONSTRAINT fk_enrollments_payment FOREIGN KEY (payment_id) REFERENCES payments(id)
);

CREATE TABLE IF NOT EXISTS course_reviews (
    id BIGINT NOT NULL AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    student_id VARCHAR(36) NOT NULL,
    rating INT NOT NULL,
    comment TEXT,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    created_at DATETIME,
    updated_at DATETIME,
    PRIMARY KEY (id),
    KEY idx_course_reviews_course_id (course_id),
    KEY idx_course_reviews_student_id (student_id),
    CONSTRAINT fk_course_reviews_course FOREIGN KEY (course_id) REFERENCES courses(id),
    CONSTRAINT fk_course_reviews_student FOREIGN KEY (student_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS progress (
    id BIGINT NOT NULL AUTO_INCREMENT,
    student_id VARCHAR(36) NOT NULL,
    lesson_video_id BIGINT NOT NULL,
    watched_duration INT,
    is_completed BIT DEFAULT b'0',
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    created_at DATETIME,
    updated_at DATETIME,
    PRIMARY KEY (id),
    KEY idx_progress_student_id (student_id),
    KEY idx_progress_lesson_video_id (lesson_video_id),
    CONSTRAINT fk_progress_student FOREIGN KEY (student_id) REFERENCES users(id),
    CONSTRAINT fk_progress_lesson_video FOREIGN KEY (lesson_video_id) REFERENCES lesson_videos(id)
);
