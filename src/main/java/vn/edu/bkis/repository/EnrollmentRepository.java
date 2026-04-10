package vn.edu.bkis.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.edu.bkis.dto.NewestStudentDto;
import vn.edu.bkis.model.Enrollment;

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
}
