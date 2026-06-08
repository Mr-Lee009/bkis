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

CREATE TABLE IF NOT EXISTS payment_gateway_config
(
    id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    provider          VARCHAR(20)  NOT NULL,
    enabled           BOOLEAN      NOT NULL DEFAULT TRUE,
    environment       VARCHAR(20)  NOT NULL,
    merchant_code     VARCHAR(100)          DEFAULT NULL,
    endpoint_base_url VARCHAR(255) NOT NULL,
    create_api_path   VARCHAR(255)          DEFAULT NULL,
    query_api_path    VARCHAR(255)          DEFAULT NULL,
    return_url        VARCHAR(255) NOT NULL,
    callback_url      VARCHAR(255) NOT NULL,
    secret_ref        VARCHAR(255) NOT NULL,
    timeout_seconds   INT          NOT NULL DEFAULT 15,
    priority          INT          NOT NULL DEFAULT 100,
    config_json       JSON                  DEFAULT NULL,
    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_payment_gateway_provider (provider),
    KEY               idx_payment_gateway_enabled_priority (enabled, priority)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS payment_transaction
(
    id                     BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    payment_code           VARCHAR(64) NOT NULL,
    order_id               VARCHAR(64) NOT NULL,
    student_id             VARCHAR(36) NOT NULL,
    course_id              BIGINT NOT NULL,
    provider               VARCHAR(20) NOT NULL,
    gateway_txn_ref        VARCHAR(100)         DEFAULT NULL,
    gateway_transaction_no VARCHAR(100)         DEFAULT NULL,
    amount                 DECIMAL(19,2) NOT NULL,
    currency               VARCHAR(10) NOT NULL DEFAULT 'VND',
    status                 VARCHAR(20) NOT NULL,
    payment_url            TEXT                 DEFAULT NULL,
    request_payload        JSON                 DEFAULT NULL,
    response_payload       JSON                 DEFAULT NULL,
    callback_payload       JSON                 DEFAULT NULL,
    fail_reason            VARCHAR(255)         DEFAULT NULL,
    paid_at                DATETIME             DEFAULT NULL,
    created_at             DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at             DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_payment_code (payment_code),
    KEY                    idx_payment_student_id (student_id),
    KEY                    idx_payment_course_id (course_id),
    KEY                    idx_payment_order_id (order_id),
    KEY                    idx_payment_provider_ref (provider, gateway_txn_ref),
    KEY                    idx_payment_status (status),
    CONSTRAINT fk_payment_transaction_student FOREIGN KEY (student_id) REFERENCES users(id),
    CONSTRAINT fk_payment_transaction_course FOREIGN KEY (course_id) REFERENCES courses(id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS enrollments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    student_id VARCHAR(36) NOT NULL,
    course_id BIGINT NOT NULL,
    payment_code VARCHAR(64),
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
    KEY idx_enrollments_payment_code (payment_code),
    CONSTRAINT fk_enrollments_student FOREIGN KEY (student_id) REFERENCES users(id),
    CONSTRAINT fk_enrollments_course FOREIGN KEY (course_id) REFERENCES courses(id),
    CONSTRAINT fk_enrollments_payment_code FOREIGN KEY (payment_code) REFERENCES payment_transaction(payment_code)
);




