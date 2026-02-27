# 1. **Lấy danh sách tất cả học viên (Student) với tên và email.**
SELECT *
FROM users
where role = 'STUDENT'
  and username = 'ducla'
  and email like '%ducla%';

# 2. **Đếm số lượng học viên trong bảng Student.**
SELECT COUNT(*)
FROM users
where role = 'STUDENT';
SELECT COUNT(*)
FROM users
where role = 'ADMIN';

# 3. **Lấy tất cả các bài học (Lesson) thuộc một khoá học cụ thể (course_id = ?).**
select *
from courses
where id in (1, 2);
select *
from lessons
where course_id = 1;

# 4. **Tìm các học viên đã đánh giá khoá học với rating >= 4 trong bảng CourseReview.**

select u.id, u.username
from users u
         join course_reviews cr on u.id = cr.student_id
where cr.course_id = 1
  and cr.rating >= 4;

select u.id, u.username
from users u
where id in (select student_id from course_reviews cr where cr.rating >= 4 and cr.course_id = 1)
  and u.role = 'STUDENT';

# 5. **Tính điểm rating trung bình của từng khoá học dựa trên bảng CourseReview.**

select c.title as TITLE, AVG(cr.rating) as RATING_POINT
from courses c
         join course_reviews cr on c.id = cr.course_id
group by c.id;

# 6. **Lấy danh sách các học viên chưa hoàn thành bất kỳ video nào (Progress.completed = false).**

SELECT u.username, COUNT(p.student_id) as total_video
FROM users u
         join progress p on u.id = p.student_id
group by student_id;


select u.id, u.username
from users u
where id in (select p.student_id
             from progress p
             group by student_id
             having sum(case when p.is_completed then 1 else 0 end) = 0)
  and role = 'STUDENT';

# 7. **Lấy 5 khoá học có nhiều đánh giá nhất (dựa vào số lượng CourseReview).**

select c.id, c.title, COUNT(cr.course_id) as review_count
from courses c
         join course_reviews cr
              on c.id = cr.course_id
group by cr.course_id, c.title
order by review_count DESC
limit 3;

select * from course_reviews;
