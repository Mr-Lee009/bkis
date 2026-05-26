package vn.edu.bkis.service.admin;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.bkis.constan.ConstantCommon;
import vn.edu.bkis.dto.admin.AdminOptionDto;
import vn.edu.bkis.dto.admin.student.AdminStudentCreateRequest;
import vn.edu.bkis.dto.admin.student.AdminStudentCreateResponseDto;
import vn.edu.bkis.dto.admin.student.AdminStudentFormOptionsDto;
import vn.edu.bkis.model.Course;
import vn.edu.bkis.model.Enrollment;
import vn.edu.bkis.model.EnrollmentStatus;
import vn.edu.bkis.model.User;
import vn.edu.bkis.model.UserRole;
import vn.edu.bkis.repository.CourseRepository;
import vn.edu.bkis.repository.EnrollmentRepository;
import vn.edu.bkis.repository.UserRepository;
import vn.edu.bkis.security.UserSession;

/**
 * Command service for admin student mutations.
 */
@Service
public class AdminStudentCommandService {
    private static final String DEFAULT_PROFILE_PICTURE = "/img/testimonial-1.jpg";
    private static final String DEFAULT_PASSWORD = "123456";

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserSession userSession;

    /**
     * Create the service with required repositories.
     *
     * @param userRepository user repository
     * @param courseRepository course repository
     * @param enrollmentRepository enrollment repository
     * @param passwordEncoder password encoder
     */
    public AdminStudentCommandService(
        UserRepository userRepository,
        CourseRepository courseRepository,
        EnrollmentRepository enrollmentRepository,
        PasswordEncoder passwordEncoder,
        UserSession userSession
    ) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.userSession = userSession;
    }

    /**
     * Load master data for the add-student modal.
     *
     * @return add-student form options
     */
    @Transactional(readOnly = true)
    public AdminStudentFormOptionsDto getFormOptions() {
        List<AdminOptionDto> courses = courseRepository.findByActiveFlagTrueOrderByTitleAsc().stream()
            .map(course -> new AdminOptionDto(String.valueOf(course.getId()), course.getTitle()))
            .toList();

        List<AdminOptionDto> mentors = userRepository.findByRoleInOrderByFullNameAsc(
            List.of(UserRole.TEACHER, UserRole.INSTRUCTOR)
        ).stream()
            .map(user -> new AdminOptionDto(user.getId(), user.getFullName()))
            .toList();

        return new AdminStudentFormOptionsDto(courses, mentors);
    }

    /**
     * Create a new student and initial enrollment from the admin modal.
     *
     * @param request submitted payload
     * @return created student summary
     */
    @Transactional
    public AdminStudentCreateResponseDto createStudent(AdminStudentCreateRequest request) {
        String fullName = required(request.getFullName(), "Họ và tên là bắt buộc.");
        String email = required(request.getEmail(), "Email là bắt buộc.").toLowerCase(Locale.ROOT);

        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email đã tồn tại.");
        }

        Long courseId = request.getCourseId();
        if (courseId == null) {
            throw new IllegalArgumentException("Khóa học là bắt buộc.");
        }

        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khóa học."));

        if (request.getStartDate() == null) {
            throw new IllegalArgumentException("Ngày bắt đầu là bắt buộc.");
        }

        String username = generateUniqueUsername(fullName, email);
        String studentId = UUID.randomUUID().toString();

        User user = new User();
        user.setId(studentId);
        user.setUsername(username);
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
        user.setRole(UserRole.STUDENT);
        user.setBio(buildBio(request));
        user.setProfilePictureUrl(DEFAULT_PROFILE_PICTURE);
        user.setFailedLoginAttempts(ConstantCommon.ZERO_NUMBER);
        user.setLocked(false);
        user.setCreatedBy(userSession.userId());
        user.setUpdatedBy(userSession.userId());

        userRepository.save(user);

        Enrollment enrollment = new Enrollment();
        enrollment.setStudentId(studentId);
        enrollment.setCourseId(course.getId());
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        enrollment.setEnrolledAt(request.getStartDate().atStartOfDay());
        enrollment.setCreatedBy(userSession.userId());
        enrollment.setUpdatedBy(userSession.userId());

        enrollmentRepository.save(enrollment);

        return new AdminStudentCreateResponseDto(
            studentId,
            username,
            user.getFullName(),
            user.getEmail(),
            course.getId(),
            course.getTitle()
        );
    }

    private String buildBio(AdminStudentCreateRequest request) {
        String goals = request.getGoals() == null ? "" : request.getGoals().stream()
            .filter(value -> value != null && !value.isBlank())
            .map(String::trim)
            .collect(Collectors.joining(", "));

        StringBuilder builder = new StringBuilder("Created from admin students page.");
        if (request.getCohortCode() != null && !request.getCohortCode().isBlank()) {
            builder.append(" Cohort: ").append(request.getCohortCode().trim()).append('.');
        }
        if (request.getMentorId() != null && !request.getMentorId().isBlank()) {
            builder.append(" MentorId: ").append(request.getMentorId().trim()).append('.');
        }
        if (!goals.isBlank()) {
            builder.append(" Goals: ").append(goals).append('.');
        }
        if (request.getNote() != null && !request.getNote().isBlank()) {
            builder.append(" Note: ").append(request.getNote().trim());
        }
        return builder.toString();
    }

    private String generateUniqueUsername(String fullName, String email) {
        String emailPrefix = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
        String base = slugify(fullName);
        if (base.isBlank()) {
            base = slugify(emailPrefix);
        }
        if (base.isBlank()) {
            base = "student";
        }

        String candidate = base;
        int counter = 1;
        while (userRepository.findByUsername(candidate).isPresent()) {
            candidate = base + counter;
            counter += 1;
        }
        return candidate;
    }

    private String slugify(String input) {
        return input == null ? "" : input.trim()
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", ".")
            .replaceAll("^\\.+|\\.+$", "");
    }

    private String required(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}
