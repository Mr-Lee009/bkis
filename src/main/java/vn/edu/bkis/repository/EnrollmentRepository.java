package vn.edu.bkis.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.edu.bkis.dto.NewestStudentDto;
import vn.edu.bkis.model.Enrollment;
import vn.edu.bkis.model.EnrollmentStatus;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
  @Query(value = "SELECT u.username AS username, "
      + "        c.title AS titleCourse, "
      + "        en.enrolled_at AS enrolledAt, "
      + "        en.status AS status "
      + " FROM enrollments en "
      + "   JOIN users u ON en.student_id = u.id "
      + "   JOIN courses c ON en.course_id = c.id "
      + " WHERE u.role = 'STUDENT' "
      + "   AND u.locked = FALSE "
      + "   LIMIT 10", nativeQuery = true)
  List<NewestStudentDto> getStudentCreateThisMonth();

  // Kiểm tra học viên đã có quyền truy cập khóa học theo trạng thái enrollment hay chưa.
  boolean existsByStudentIdAndCourseIdAndStatus(String studentId, Long courseId, EnrollmentStatus status);

  // Lấy bản ghi đăng ký khóa học hiện có để tránh tạo trùng enrollment cho cùng học viên.
  Optional<Enrollment> findByStudentIdAndCourseId(String studentId, Long courseId);

  // Lấy danh sách enrollment của học viên hiện tại để hiển thị trang khóa học của bạn.
  List<Enrollment> findByStudentIdOrderByEnrolledAtDesc(String studentId);
}
