# insert 10 records into the users table
INSERT INTO bkis_edu.users (id,created_at, created_by, updated_at, updated_by, email, failed_login_attempts, locked, password_hash, role, username)
VALUES 
 ( UUID(),'2026-02-24 06:43:34.570660', null, '2026-02-24 06:43:34.570660', null, 'ducla@example.com', 0, false, '$2a$10$4Fb5C531EaRxhdMN/b1BaeF7gQqSRuq6lvWzP5VUew.JtkX/hPZDm', 'STUDENT', 'ducla')
,( UUID(),'2026-02-24 06:43:34.570660', null, '2026-02-24 06:43:34.570660', null, 'hoa@example.com', 0, false, '$2a$10$4Fb5C531EaRxhdMN/b1BaeF7gQqSRuq6lvWzP5VUew.JtkX/hPZDm', 'STUDENT', 'hoa')
,( UUID(),'2026-02-24 06:43:34.570660', null, '2026-02-24 06:43:34.570660', null, 'tung@example.com', 0, false, '$2a$10$4Fb5C531EaRxhdMN/b1BaeF7gQqSRuq6lvWzP5VUew.JtkX/hPZDm', 'STUDENT', 'tung')
,( UUID(),'2026-02-24 06:43:34.570660', null, '2026-02-24 06:43:34.570660', null, 'john@gmail.com', 0, false, '$2a$10$4Fb5C531EaRxhdMN/b1BaeF7gQqSRuq6lvWzP5VUew.JtkX/hPZDm', 'STUDENT', 'john')
,( UUID(),'2026-02-24 06:43:34.570660', null, '2026-02-24 06:43:34.570660', null, 'nathan@hotmail.com', 0, false, '$2a$10$4Fb5C531EaRxhdMN/b1BaeF7gQqSRuq6lvWzP5VUew.JtkX/hPZDm', 'STUDENT', 'nathan')
,( UUID(),'2026-02-24 06:43:34.570660', null, '2026-02-24 06:43:34.570660', null, 'alice@example.com', 0, false, '$2a$10$4Fb5C531EaRxhdMN/b1BaeF7gQqSRuq6lvWzP5VUew.JtkX/hPZDm', 'STUDENT', 'alice')
,( UUID(),'2026-02-24 06:43:34.570660', null, '2026-02-24 06:43:34.570660', null, 'bob@example.com', 0, false, '$2a$10$4Fb5C531EaRxhdMN/b1BaeF7gQqSRuq6lvWzP5VUew.JtkX/hPZDm', 'STUDENT', 'bob')
,( UUID(),'2026-02-24 06:43:34.570660', null, '2026-02-24 06:43:34.570660', null, 'charlie@example.com', 0, false, '$2a$10$4Fb5C531EaRxhdMN/b1BaeF7gQqSRuq6lvWzP5VUew.JtkX/hPZDm', 'STUDENT', 'charlie')
;

# add teacher user
INSERT INTO bkis_edu.users (id,created_at, created_by, updated_at, updated_by, email, failed_login_attempts, locked, password_hash, role, username)
VALUES 
 ( UUID(),'2026-02-24 06:43:34.570660', null, '2026-02-24 06:43:34.570660', null, 'teacher@example.com', 0, false, '$2a$10$4Fb5C531EaRxhdMN/b1BaeF7gQqSRuq6lvWzP5VUew.JtkX/hPZDm', 'TEACHER', 'teacher');

select * from users where role = 'TEACHER';

# insert 10 records into the courses table
INSERT INTO bkis_edu.courses (created_at, created_by, updated_at, updated_by, active_flag, description,
 image_url, price, rating, tag, teacher_id, title, total_students) 
 VALUES ('2026-02-27 13:57:39.000000', 'john', '2026-02-27 13:57:45.000000', 'me', true, 'Hoa hoc', null, 1000.00, 4, '#hoahoc', '66215434-13bf-11f1-81e9-52ad0b6a9c89', 'Hoa hoc co ban', 10)
 ,('2026-02-27 13:57:39.000000', 'john', '2026-02-27 13:57:45.000000', 'me', true, 'Toan hoc', null, 1500.00, 5, '#toanhoc', '66215434-13bf-11f1-81e9-52ad0b6a9c89', 'Toan hoc co ban', 20)
 ,('2026-02-27 13:57:39.000000', 'john', '2026-02-27 13:57:45.000000', 'me', true, 'Ly hoc', null, 1200.00, 3, '#lyhoc', '66215434-13bf-11f1-81e9-52ad0b6a9c89', 'Ly hoc co ban', 15)
 ,('2026-02-27 13:57:39.000000', 'john', '2026-02-27 13:57:45.000000', 'me', true, 'Sinh hoc', null, 1300.00, 4, '#sinhhoc', '66215434-13bf-11f1-81e9-52ad0b6a9c89', 'Sinh hoc co ban', 12)
 ,('2026-02-27 13:57:39.000000', 'john', '2026-02-27 13:57:45.000000', 'me', true, 'Tin hoc', null, 1100.00, 5, '#tinhoc', '66215434-13bf-11f1-81e9-52ad0b6a9c89', 'Tin hoc co ban', 18)
 ,('2026-02-27 13:57:39.000000', 'john', '2026-02-27 13:57:45.000000', 'me', true, 'Van hoc', null, 900.00, 4, '#vanhoc', '66215434-13bf-11f1-81e9-52ad0b6a9c89', 'Van hoc co ban', 8)
 ,('2026-02-27 13:57:39.000000', 'john', '2026-02-27 13:57:45.000000', 'me', true, 'Lich su', null, 800.00, 3, '#lichsu', '66215434-13bf-11f1-81e9-52ad0b6a9c89', 'Lich su co ban', 5);


select * from courses;
# insert 10 records into the lessons table
#hoa hoc co ban
INSERT INTO bkis_edu.lessons (created_at, created_by, updated_at, updated_by, course_id, description, title,position)
 VALUES ('2026-02-27 14:00:00.000000', 'john', '2026-02-27 14:00:00.000000', 'john', '1', 'Gioi thieu ve hoa hoc', 'Gioi thieu ve hoa hoc', 1)
 ,('2026-02-27 14:00:00.000000', 'john', '2026-02-27 14:00:00.000000', 'john', '1', 'Cac nguyen to hoa hoc', 'Cac nguyen to hoa hoc', 2)
 ,('2026-02-27 14:00:00.000000', 'john', '2026-02-27 14:00:00.000000', 'john', '1', 'Phan ung hoa hoc co ban', 'Phan ung hoa hoc co ban', 3)
 ,('2026-02-27 14:00:00.000000', 'john', '2026-02-27 14:00:00.000000', 'john', '1', 'Ung dung cua hoa hoc trong cuoc song', 'Ung dung cua hoa hoc trong cuoc song', 4)
 ,('2026-02-27 14:00:00.000000', 'john', '2026-02-27 14:00:00.000000', 'john', '1', 'Thuc hanh hoa hoc co ban', 'Thuc hanh hoa hoc co ban', 5);

#tin hoc co ban
INSERT INTO bkis_edu.lessons (created_at, created_by, updated_at, updated_by, course_id, description, title,position)
 VALUES ('2026-02-27 14:00:00.000000', 'john', '2026-02-27 14:00:00.000000', 'john', '5', 'Gioi thieu ve tin hoc', 'Gioi thieu ve tin hoc',  1)
 ,('2026-02-27 14:00:00.000000', 'john', '2026-02-27 14:00:00.000000', 'john', '5', 'Cac nguyen ly co ban cua tin hoc', 'Cac nguyen ly co ban cua tin hoc',  2)
 ,('2026-02-27 14:00:00.000000', 'john', '2026-02-27 14:00:00.000000', 'john', '5', 'Phan mem va ung dung cua tin hoc trong cuoc song', 'Phan mem va ung dung cua tin hoc trong cuoc song',  3)
 ,('2026-02-27 14:00:00.000000', 'john', '2026-02-27 14:00:00.000000', 'john', '5', 'Thuc hanh tin hoc co ban', 'Thuc hanh tin hoc co ban',  4)
 ,('2026-02-27 14:00:00.000000', 'john', '2026-02-27 14:00:00.000000', 'john', '5', 'Ung dung cua tin hoc trong giao duc va kinh doanh', 'Ung dung cua tin hoc trong giao duc va kinh doanh',  5);


# insert 10 records into the enrollments table

# insert 10 records into the course_reviews table.
# insert for course_id = 1 (hoa hoc co ban) and student_id = 1,2,3,4,5
INSERT INTO bkis_edu.users (id) VALUES ('2e39cbc1-13bf-11f1-81e9-52ad0b6a9c89');
INSERT INTO bkis_edu.users (id) VALUES ('2e39e020-13bf-11f1-81e9-52ad0b6a9c89');
INSERT INTO bkis_edu.users (id) VALUES ('2e39e313-13bf-11f1-81e9-52ad0b6a9c89');
INSERT INTO bkis_edu.users (id) VALUES ('2e39e3a1-13bf-11f1-81e9-52ad0b6a9c89');
INSERT INTO bkis_edu.users (id) VALUES ('2e39e4b1-13bf-11f1-81e9-52ad0b6a9c89');
INSERT INTO bkis_edu.users (id) VALUES ('2e39f110-13bf-11f1-81e9-52ad0b6a9c89');


INSERT INTO bkis_edu.course_reviews (created_at, created_by, updated_at, updated_by, comment, course_id, rating, student_id) 
VALUES ('2026-02-27 14:15:06.000000', 'ducla', '2026-02-27 14:15:12.000000', 'ducla', 'Good', 1, 4, '2e39cbc1-13bf-11f1-81e9-52ad0b6a9c89')
,('2026-02-27 14:15:06.000000', 'hoa', '2026-02-27 14:15:12.000000', 'hoa', 'Excellent', 1, 5, '2e39e020-13bf-11f1-81e9-52ad0b6a9c89')
,('2026-02-27 14:15:06.000000', 'tung', '2026-02-27 14:15:12.000000', 'tung', 'Average', 1, 3, '2e39e313-13bf-11f1-81e9-52ad0b6a9c89')
,('2026-02-27 14:15:06.000000', 'john', '2026-02-27 14:15:12.000000', 'john', 'Not bad', 1, 4, '2e39e3a1-13bf-11f1-81e9-52ad0b6a9c89')
,('2026-02-27 14:15:06.000000', 'nathan', '2026-02-27 14:15:12.000000', 'nathan', 'Could be better', 1, 2, '2e39f110-13bf-11f1-81e9-52ad0b6a9c89');

# insert for course_id = 5 (tin hoc co ban) and student_id = 1,2,3,4,5
INSERT INTO bkis_edu.course_reviews (created_at, created_by, updated_at, updated_by, comment, course_id, rating, student_id) 
VALUES ('2026-02-27 14:15:06.000000', 'ducla', '2026-02-27 14:15:12.000000', 'ducla', 'Good', 5, 4, '2e39cbc1-13bf-11f1-81e9-52ad0b6a9c89')
,('2026-02-27 14:15:06.000000', 'hoa', '2026-02-27 14:15:12.000000', 'hoa', 'Excellent', 5, 5, '2e39e020-13bf-11f1-81e9-52ad0b6a9c89')
,('2026-02-27 14:15:06.000000', 'tung', '2026-02-27 14:15:12.000000', 'tung', 'Average', 5, 3, '2e39e313-13bf-11f1-81e9-52ad0b6a9c89')
,('2026-02-27 14:15:06.000000', 'john', '2026-02-27 14:15:12.000000', 'john', 'Not bad', 5, 4, '2e39e3a1-13bf-11f1-81e9-52ad0b6a9c89')
,('2026-02-27 14:15:06.000000', 'nathan', '2026-02-27 14:15:12.000000', 'nathan', 'Could be better', 5, 2, '2e39f110-13bf-11f1-81e9-52ad0b6a9c89');

# insert for course_id = 2 (toan hoc co ban) and student_id = 1,2,3,4,5
INSERT INTO bkis_edu.course_reviews (created_at, created_by, updated_at, updated_by, comment, course_id, rating, student_id) 
VALUES ('2026-02-27 14:15:06.000000', 'ducla', '2026-02-27 14:15:12.000000', 'ducla', 'Good', 2, 4, '2e39cbc1-13bf-11f1-81e9-52ad0b6a9c89')
,('2026-02-27 14:15:06.000000', 'hoa', '2026-02-27 14:15:12.000000', 'hoa', 'Excellent', 2, 5, '2e39e020-13bf-11f1-81e9-52ad0b6a9c89')
,('2026-02-27 14:15:06.000000', 'tung', '2026-02-27 14:15:12.000000', 'tung', 'Average', 2, 3, '2e39e313-13bf-11f1-81e9-52ad0b6a9c89')
,('2026-02-27 14:15:06.000000', 'john', '2026-02-27 14:15:12.000000', 'john', 'Not bad', 2, 4, '2e39e3a1-13bf-11f1-81e9-52ad0b6a9c89')
,('2026-02-27 14:15:06.000000', 'nathan', '2026-02-27 14:15:12.000000', 'nathan', 'Could be better', 2, 2, '2e39f110-13bf-11f1-81e9-52ad0b6a9c89'); 

select * from lessons;
#insert 10 records into the lesson_videos table
INSERT INTO bkis_edu.lesson_videos (created_at, created_by, updated_at, updated_by, duration, lesson_id, position, title, video_url) 
VALUES 
('2026-02-27 14:58:13.000000', 'sys', '2026-02-27 14:58:17.000000', 'sys', 1, 1, 1, 'video gioi thieu kho hoc', 'link')
,('2026-02-27 14:58:13.000000', 'sys', '2026-02-27 14:58:17.000000', 'sys', 1, 1, 2, 'video cac nguyen to hoa hoc', 'link')
,('2026-02-27 14:58:13.000000', 'sys', '2026-02-27 14:58:17.000000', 'sys', 1, 1, 3, 'video phan ung hoa hoc co ban', 'link')
,('2026-02-27 14:58:13.000000', 'sys', '2026-02-27 14:58:17.000000', 'sys', 1, 1, 4, 'video ung dung cua hoa hoc trong cuoc song', 'link')
,('2026-02-27 14:58:13.000000', 'sys', '2026-02-27 14:58:17.000000', 'sys', 1, 1, 5, 'video thuc hanh hoa hoc co ban', 'link')
;

select * from lesson_videos;

select * from progress;
INSERT INTO bkis_edu.progress (created_at, created_by, updated_at, updated_by, is_completed, lesson_video_id, student_id, watched_duration)
VALUES ('2026-02-27 14:50:15.000000', 'sys', '2026-02-27 14:50:19.000000', 'sys', true, 1, '2e39cbc1-13bf-11f1-81e9-52ad0b6a9c89', 1)
,('2026-02-27 14:50:15.000000', 'sys', '2026-02-27 14:50:19.000000', 'sys', true, 2, '2e39cbc1-13bf-11f1-81e9-52ad0b6a9c89', 0)
,('2026-02-27 14:50:15.000000', 'sys', '2026-02-27 14:50:19.000000', 'sys', true, 3, '2e39cbc1-13bf-11f1-81e9-52ad0b6a9c89', 0)
,('2026-02-27 14:50:15.000000', 'sys', '2026-02-27 14:50:19.000000', 'sys', true, 4, '2e39cbc1-13bf-11f1-81e9-52ad0b6a9c89', 0)
,('2026-02-27 14:50:15.000000', 'sys', '2026-02-27 14:50:19.000000', 'sys', true, 5, '2e39cbc1-13bf-11f1-81e9-52ad0b6a9c89', 0);

select * from enrollments;
