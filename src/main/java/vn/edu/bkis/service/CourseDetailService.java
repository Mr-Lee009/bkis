package vn.edu.bkis.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
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

/**
 * Service layer for course detail functionality.
 * Aggregates data from multiple repositories and prepares DTOs for course detail page.
 */
@Service
public class CourseDetailService {

    private static final String DEFAULT_COURSE_IMAGE = "/img/course-1.jpg";

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CourseReviewRepository courseReviewRepository;
    private final LessonRepository lessonRepository;

    /**
     * Constructor for dependency injection.
     * @param courseRepository for course data access
     * @param userRepository for instructor information
     * @param courseReviewRepository for course ratings and reviews
     * @param lessonRepository for course curriculum
     */
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
                defaultHighlights(course.getHighlights()),
                lessons.stream()
                        .map(this::toLessonDto)
                        .toList(),
                getRelatedCourses(course));
    }

    /**
     * Fetch and convert related courses to DTOs for display.
     * @param course the current course (excluded from results)
     * @return list of up to 4 related active courses
     */
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

    /**
     * Convert Lesson entity to CourseLessonDto.
     * @param lesson the Lesson entity
     * @return the DTO representation
     */
    private CourseLessonDto toLessonDto(Lesson lesson) {
        return new CourseLessonDto(
                lesson.getId(),
                lesson.getPosition(),
                lesson.getTitle(),
                lesson.getDescription());
    }

    /**
     * Generate default learning highlights if not provided.
     * @return list of course learning objectives
     */
    private List<String> defaultHighlights(String highlights) {
        // split text by '||' or return default highlights if null/empty
        return Arrays.stream(highlights.split("\\|\\|")).toList();
    }

    /**
     * Estimate course duration in hours based on lesson count.
     * @param lessons number of lessons in the course
     * @return estimated duration in hours (minimum 8)
     */
    private Integer estimateDurationHours(int lessons) {
        return Math.max(8, lessons * 2);
    }

    /**
     * Normalize and validate course rating score.
     * @param avgRating average rating from reviews (nullable)
     * @param fallbackRating default rating if average is unavailable
     * @return validated integer rating 0-5
     */
    private Integer normalizeRating(Double avgRating, Integer fallbackRating) {
        if (avgRating != null && avgRating > 0) {
            return (int) Math.round(avgRating);
        }
        return BkisNumberUtils.defaultInteger(fallbackRating, 5);
    }

    /**
     * Normalize price to 2 decimal places using proper rounding.
     * @param price the raw price (nullable)
     * @return normalized BigDecimal price or zero if null
     */
    private BigDecimal defaultPrice(BigDecimal price) {
        return price == null ? BigDecimal.ZERO : price.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Normalize and validate image URL paths.
     * Ensures URL is absolute path or HTTP/HTTPS URL.
     * @param imageUrl the raw image URL (nullable)
     * @return normalized URL or default image path
     */
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
