package vn.edu.bkis.service;

import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.bkis.dto.CourseSignupFormDto;
import vn.edu.bkis.dto.CourseSignupPageDto;
import vn.edu.bkis.model.Course;
import vn.edu.bkis.model.Enrollment;
import vn.edu.bkis.model.EnrollmentStatus;
import vn.edu.bkis.model.User;
import vn.edu.bkis.model.UserRole;
import vn.edu.bkis.repository.CourseRepository;
import vn.edu.bkis.repository.EnrollmentRepository;

@Service
public class CourseSignupService {
    private static final String DEFAULT_COURSE_IMAGE = "/img/course-1.jpg";

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    // Khởi tạo service xử lý trang đăng ký khóa học và tạo enrollment cho học viên.
    public CourseSignupService(CourseRepository courseRepository, EnrollmentRepository enrollmentRepository) {
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    // Lấy dữ liệu khóa học và học viên hiện tại để hiển thị form đăng ký.
    public CourseSignupPageDto getSignupPage(Long courseId, User student) {
        validateStudent(student);
        Course course = getActiveCourse(courseId);
        boolean alreadyEnrolled = enrollmentRepository.existsByStudentIdAndCourseIdAndStatus(
                student.getId(), courseId, EnrollmentStatus.ACTIVE);
        return new CourseSignupPageDto(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                normalizeImage(course.getImageUrl()),
                course.getPrice(),
                student.getFullName(),
                student.getEmail(),
                alreadyEnrolled
        );
    }

    // Đăng ký khóa học cho học viên hiện tại và tránh tạo trùng nếu đã đăng ký trước đó.
    @Transactional
    public void signup(Long courseId, User student, CourseSignupFormDto form) {
        validateStudent(student);
        validateSignupForm(form);
        Course course = getActiveCourse(courseId);
        enrollmentRepository.findByStudentIdAndCourseId(student.getId(), course.getId())
                .ifPresentOrElse(
                        enrollment -> activateEnrollment(enrollment, student.getUsername()),
                        () -> createEnrollment(student, course)
                );
    }

    // Kiểm tra chỉ tài khoản học viên mới được đăng ký khóa học.
    private void validateStudent(User student) {
        if (student == null || student.getRole() != UserRole.STUDENT) {
            throw new IllegalArgumentException("Chỉ tài khoản học viên mới được đăng ký khóa học.");
        }
        if (Boolean.TRUE.equals(student.getLocked())) {
            throw new IllegalArgumentException("Tài khoản của bạn đang bị khóa nên không thể đăng ký khóa học.");
        }
    }

    // Kiểm tra form đăng ký đã đồng ý điều khoản bắt buộc.
    private void validateSignupForm(CourseSignupFormDto form) {
        if (form == null || !form.isAcceptedTerms()) {
            throw new IllegalArgumentException("Bạn cần đồng ý điều khoản dịch vụ trước khi đăng ký khóa học.");
        }
    }

    // Lấy khóa học đang active để đảm bảo không đăng ký khóa học đã bị ẩn.
    private Course getActiveCourse(Long courseId) {
        return courseRepository.findById(courseId)
                .filter(course -> Boolean.TRUE.equals(course.getActiveFlag()))
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khóa học đang mở đăng ký."));
    }

    // Tạo mới enrollment ACTIVE cho học viên sau khi đăng ký thành công.
    private void createEnrollment(User student, Course course) {
        Enrollment enrollment = new Enrollment();
        enrollment.setStudentId(student.getId());
        enrollment.setCourseId(course.getId());
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        enrollment.setEnrolledAt(LocalDateTime.now());
        enrollment.setCreatedBy(student.getUsername());
        enrollment.setUpdatedBy(student.getUsername());
        enrollmentRepository.save(enrollment);
    }

    // Kích hoạt lại enrollment hiện có nếu trước đó chưa ở trạng thái ACTIVE.
    private void activateEnrollment(Enrollment enrollment, String username) {
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        if (enrollment.getEnrolledAt() == null) {
            enrollment.setEnrolledAt(LocalDateTime.now());
        }
        enrollment.setUpdatedBy(username);
        enrollmentRepository.save(enrollment);
    }

    // Chuẩn hóa đường dẫn ảnh khóa học để template luôn render được ảnh hợp lệ.
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
