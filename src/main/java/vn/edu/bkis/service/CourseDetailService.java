package vn.edu.bkis.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Service;
import vn.edu.bkis.dto.CourseDetailPageDto;
import vn.edu.bkis.dto.CourseLessonDto;
import vn.edu.bkis.dto.HomeCourseDto;
import vn.edu.bkis.model.Course;
import vn.edu.bkis.model.Lesson;
import vn.edu.bkis.model.User;
import vn.edu.bkis.repository.CourseRepository;
import vn.edu.bkis.repository.CourseReviewRepository;
import vn.edu.bkis.repository.LessonRepository;
import vn.edu.bkis.repository.UserRepository;
import vn.edu.bkis.util.BkisNumberUtils;

@Service
public class CourseDetailService {

    private static final String DEFAULT_COURSE_IMAGE = "/img/course-1.jpg";

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CourseReviewRepository courseReviewRepository;
    private final LessonRepository lessonRepository;

    public CourseDetailService(
            CourseRepository courseRepository,
            UserRepository userRepository,
            CourseReviewRepository courseReviewRepository,
            LessonRepository lessonRepository) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.courseReviewRepository = courseReviewRepository;
        this.lessonRepository = lessonRepository;
    }

    /**
     * Get course detail by course ID.
     *
     * @param courseId the ID of the course
     * @return the course detail DTO
     * @throws IllegalArgumentException if the course is not found or inactive
     */
    public CourseDetailPageDto getCourseDetail(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .filter(c -> Boolean.TRUE.equals(c.getActiveFlag()))
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));

        User teacher = userRepository.findById(course.getTeacherId()).orElse(null);
        List<Lesson> lessons = lessonRepository.findByCourseIdOrderByPositionAsc(course.getId());
        long totalReviews = courseReviewRepository.countByCourseId(course.getId());
        Double avgRating = courseReviewRepository.findAverageRatingByCourseId(course.getId());

        return new CourseDetailPageDto(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                teacher == null ? "BKIS Instructor" : teacher.getFullName(),
                teacher == null ? "Description about teacher" : teacher.getBio(),
                teacher == null ? "" : teacher.getProfilePictureUrl(),
                teacher == null ? "support@bkis.edu.vn" : teacher.getEmail(),
                normalizeImage(course.getImageUrl()),
                course.getTag(),
                defaultPrice(course.getPrice()),
                BkisNumberUtils.defaultInteger(course.getTotalStudents(), 0),
                normalizeRating(avgRating, course.getRating()),
                (int) totalReviews,
                lessons.size(),
                estimateDurationHours(lessons.size()),
                defaultHighlights(),
                lessons.stream()
                        .map(this::toLessonDto)
                        .toList(),
                getRelatedCourses(course));
    }

    private List<HomeCourseDto> getRelatedCourses(Course course) {
        List<Course> related = courseRepository.findTop4ByActiveFlagTrueAndIdNotOrderByCreatedAtDesc(course.getId());
        return related.stream().map(c -> new HomeCourseDto(
                c.getId(),
                c.getTitle(),
                c.getDescription(),
                userRepository.findById(c.getTeacherId()).map(User::getUsername).orElse("BKIS Instructor"),
                defaultPrice(c.getPrice()),
                BkisNumberUtils.defaultInteger(c.getTotalStudents(), 0),
                estimateDurationHours(6),
                BkisNumberUtils.defaultInteger(c.getRating(), 5),
                Math.max(50, BkisNumberUtils.defaultInteger(c.getTotalStudents(), 0)),
                normalizeImage(c.getImageUrl()),
                c.getTag())).toList();
    }

    private CourseLessonDto toLessonDto(Lesson lesson) {
        return new CourseLessonDto(
                lesson.getId(),
                lesson.getPosition(),
                lesson.getTitle(),
                lesson.getDescription());
    }

    private List<String> defaultHighlights() {
        return List.of(
                "Phân tích yêu cầu và dựng lộ trình triển khai thực tế",
                "Thực hành dự án xuyên suốt để hoàn thiện portfolio",
                "Được mentor review bài và hỗ trợ qua cộng đồng học tập",
                "Tối ưu hiệu năng, cấu trúc code và khả năng bảo trì");
    }

    private Integer estimateDurationHours(int lessons) {
        return Math.max(8, lessons * 2);
    }

    private Integer normalizeRating(Double avgRating, Integer fallbackRating) {
        if (avgRating != null && avgRating > 0) {
            return (int) Math.round(avgRating);
        }
        return BkisNumberUtils.defaultInteger(fallbackRating, 5);
    }

    private BigDecimal defaultPrice(BigDecimal price) {
        return price == null ? BigDecimal.ZERO : price.setScale(2, RoundingMode.HALF_UP);
    }

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
