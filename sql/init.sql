drop database  bkis_edu;
create database bkis_edu;
use bkis_edu;
create table courses
(
    id             bigint auto_increment
        primary key,
    title          varchar(255)                                                        not null,
    description    text                                                                null,
    teacher_id     char(36)                                                            not null,
    price          decimal(10, 2)                                                      not null,
    total_students int          default 0                                              null,
    active_flag    tinyint(1)   default 1                                              null,
    tag            varchar(100)                                                        null,
    image_url      varchar(500) default 'https://example.com/default-course-image.jpg' null,
    rating         int          default 0                                              null,
    created_by     char(36)                                                            null,
    updated_by     char(36)                                                            null,
    created_at     timestamp    default CURRENT_TIMESTAMP                              null,
    updated_at     timestamp    default CURRENT_TIMESTAMP                              null on update CURRENT_TIMESTAMP
)
    comment 'Store courses information including teacher and pricing';

create table course_reviews
(
    id         bigint auto_increment
        primary key,
    course_id  bigint                              not null,
    student_id char(36)                            not null,
    rating     int                                 not null,
    comment    text                                null,
    created_by char(36)                            null,
    updated_by char(36)                            null,
    created_at timestamp default CURRENT_TIMESTAMP null,
    updated_at timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint course_id
        unique (course_id, student_id),
    constraint course_reviews_ibfk_1
        foreign key (course_id) references courses (id)
)
    comment 'Store student reviews and ratings for courses';

create table lessons
(
    id          bigint auto_increment primary key,
    course_id   bigint                              not null,
    title       varchar(255)                        not null,
    description text                                null,
    position    int                                 not null,
    created_by  char(36)                            null,
    updated_by  char(36)                            null,
    created_at  timestamp default CURRENT_TIMESTAMP null,
    updated_at  timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint lessons_ibfk_1
        foreign key (course_id) references courses (id)
)
    comment 'Store lessons belonging to each course';

create table lesson_videos
(
    id         bigint auto_increment primary key,
    lesson_id  bigint                              not null,
    title      varchar(500)                        not null,
    video_url  varchar(500)                        not null,
    duration   int                                 null,
    position   int                                 not null,
    created_by char(36)                            null,
    updated_by char(36)                            null,
    created_at timestamp default CURRENT_TIMESTAMP null,
    updated_at timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint lesson_videos_ibfk_1
        foreign key (lesson_id) references lessons (id)
)
    comment 'Store multiple videos for each lesson';

create index idx_lesson_videos_lesson_id on lesson_videos (lesson_id);
create index idx_lessons_course_id on lessons (course_id);

create table payments
(
    id         bigint auto_increment primary key,
    student_id char(36)                                not null,
    course_id  bigint                                  not null,
    amount     decimal(10, 2)                          not null,
    status     enum ('PENDING', 'COMPLETED', 'FAILED') not null,
    created_by char(36)                                null,
    updated_by char(36)                                null,
    created_at timestamp default CURRENT_TIMESTAMP     null,
    updated_at timestamp default CURRENT_TIMESTAMP     null on update CURRENT_TIMESTAMP,
    constraint payments_ibfk_1
        foreign key (course_id) references courses (id)
)
    comment 'Store payment transactions for course purchases';

create index course_id
    on payments (course_id);

create table progress
(
    id               bigint auto_increment primary key,
    student_id       char(36)                             not null,
    lesson_video_id  bigint                               not null,
    watched_duration int        default 0                 null,
    is_completed     tinyint(1) default 0                 null,
    created_by       char(36)                             null,
    updated_by       char(36)                             null,
    created_at       timestamp  default CURRENT_TIMESTAMP null,
    updated_at       timestamp  default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint progress_ibfk_1
        foreign key (lesson_video_id) references lesson_videos (id)
)
    comment 'Track student progress for each lesson video';

create index lesson_video_id
    on progress (lesson_video_id);



create table users
(
    id            char(36)                                                                     not null primary key,
    username      varchar(100)                                                                 not null,
    email         varchar(255)                                                                 not null,
    password_hash varchar(255)                                                                 not null,
    role          enum ('STUDENT', 'TEACHER', 'ADMIN', 'INSTRUCTOR') default 'STUDENT'         not null,
    failed_login_attempts int          default 0,
    locked       tinyint(1)   default 0,
    created_by    char(36)                                                                     null,
    updated_by    char(36)                                                                     null,
    created_at    timestamp                                          default CURRENT_TIMESTAMP null,
    updated_at    timestamp                                          default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint uq_users_email unique (email),
    constraint uq_users_username unique (username)
)
    comment 'Store system users including students, teachers, and admins';


-- Link courses.teacher_id to users
alter table courses
    add index idx_courses_teacher_id (teacher_id),
    add constraint fk_courses_teacher foreign key (teacher_id) references users (id);

-- Link course_reviews.student_id to users
alter table course_reviews
    add index idx_course_reviews_student_id (student_id),
    add constraint fk_course_reviews_student foreign key (student_id) references users (id);

-- Link payments.student_id to users
alter table payments
    add index idx_payments_student_id (student_id),
    add constraint fk_payments_student foreign key (student_id) references users (id);

-- Link progress.student_id to users
alter table progress
    add index idx_progress_student_id (student_id),
    add constraint fk_progress_student foreign key (student_id) references users (id);

-- Enrollments: grant course access upon (successful) payment
create table enrollments
(
    id          bigint auto_increment primary key,
    student_id  char(36)                              not null,
    course_id   bigint                                not null,
    payment_id  bigint                                null,
    status      enum ('ACTIVE','CANCELLED','EXPIRED') not null default 'ACTIVE',
    enrolled_at timestamp default CURRENT_TIMESTAMP   null,
    expires_at  timestamp                             null,
    created_by  char(36)                              null,
    updated_by  char(36)                              null,
    created_at  timestamp default CURRENT_TIMESTAMP   null,
    updated_at  timestamp default CURRENT_TIMESTAMP   null on update CURRENT_TIMESTAMP,
    constraint uq_enrollments_student_course unique (student_id, course_id),
    constraint fk_enrollments_student foreign key (student_id) references users (id),
    constraint fk_enrollments_course foreign key (course_id) references courses (id),
    constraint fk_enrollments_payment foreign key (payment_id) references payments (id)
)
    comment 'Grant course access for a student (based on payment).';

create index idx_enrollments_course_id on enrollments (course_id);
create index idx_enrollments_student_id on enrollments (student_id);
