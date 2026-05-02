package vn.edu.bkis.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import vn.edu.bkis.dto.MyCourseDto;
import vn.edu.bkis.model.Course;
import vn.edu.bkis.model.Enrollment;
import vn.edu.bkis.repository.CourseRepository;
import vn.edu.bkis.repository.EnrollmentRepository;

@Service
public class MyCoursesService {
    private static final String DEFAULT_COURSE_IMAGE = "/img/course-1.jpg";

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;

    // Khởi tạo service lấy danh sách khóa học đã đăng ký của một học viên.
    public MyCoursesService(EnrollmentRepository enrollmentRepository, CourseRepository courseRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
    }

    // Trả về các khóa học mà học viên hiện tại đã có enrollment, không lấy khóa học của user khác.
    public List<MyCourseDto> getMyCourses(String studentId) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentIdOrderByEnrolledAtDesc(studentId);
        if (enrollments.isEmpty()) {
            return List.of();
        }
        List<Long> courseIds = enrollments.stream().map(Enrollment::getCourseId).distinct().toList();
        Map<Long, Course> coursesById = courseRepository.findAllById(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, Function.identity()));
        return enrollments.stream()
                .map(enrollment -> toDto(enrollment, coursesById.get(enrollment.getCourseId())))
                .filter(course -> course != null)
                .toList();
    }

    // Chuyển enrollment và course sang DTO để render danh sách khóa học của bạn.
    private MyCourseDto toDto(Enrollment enrollment, Course course) {
        if (course == null) {
            return null;
        }
        return new MyCourseDto(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                normalizeImage(course.getImageUrl()),
                course.getPrice() == null ? BigDecimal.ZERO : course.getPrice(),
                enrollment.getStatus() == null ? "UNKNOWN" : enrollment.getStatus().name(),
                enrollment.getEnrolledAt()
        );
    }

    // Chuẩn hóa ảnh khóa học để UI luôn có ảnh fallback hợp lệ.
    private String normalizeImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return DEFAULT_COURSE_IMAGE;
        }
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://") || imageUrl.startsWith("/")) {
            return imageUrl;
        }
        return "/" + imageUrl;
    }
}
