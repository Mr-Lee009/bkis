package vn.edu.bkis.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import jakarta.persistence.Lob;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import vn.edu.bkis.dto.CourseDetailPageDto;
import vn.edu.bkis.dto.CourseLessonDto;
import vn.edu.bkis.dto.CourseLessonVideoDto;
import vn.edu.bkis.dto.HomeCourseDto;
import vn.edu.bkis.model.Course;
import vn.edu.bkis.model.EnrollmentStatus;
import vn.edu.bkis.model.Lesson;
import vn.edu.bkis.model.LessonVideo;
import vn.edu.bkis.model.User;
import vn.edu.bkis.repository.CourseRepository;
import vn.edu.bkis.repository.CourseReviewRepository;
import vn.edu.bkis.repository.EnrollmentRepository;
import vn.edu.bkis.repository.LessonRepository;
import vn.edu.bkis.repository.LessonVideoRepository;
import vn.edu.bkis.repository.UserRepository;
import vn.edu.bkis.util.BkisNumberUtils;

/**
 * Service layer for course detail functionality. Aggregates data from multiple repositories and
 * prepares DTOs for course detail page.
 */
@Service
public class CourseDetailService {

  private static final String DEFAULT_COURSE_IMAGE = "/img/course-1.jpg";

  private final CourseRepository courseRepository;
  private final UserRepository userRepository;
  private final CourseReviewRepository courseReviewRepository;
  private final LessonRepository lessonRepository;
  private final LessonVideoRepository lessonVideoRepository;
  private final EnrollmentRepository enrollmentRepository;

  /**
   * Constructor for dependency injection.
   *
   * @param courseRepository       for course data access
   * @param userRepository         for instructor information
   * @param courseReviewRepository for course ratings and reviews
   * @param lessonRepository       for course curriculum
   */
  public CourseDetailService(CourseRepository courseRepository, UserRepository userRepository,
      CourseReviewRepository courseReviewRepository, LessonRepository lessonRepository,
      LessonVideoRepository lessonVideoRepository, EnrollmentRepository enrollmentRepository) {
    this.courseRepository = courseRepository;
    this.userRepository = userRepository;
    this.courseReviewRepository = courseReviewRepository;
    this.lessonRepository = lessonRepository;
    this.lessonVideoRepository = lessonVideoRepository;
    this.enrollmentRepository = enrollmentRepository;
  }

  /**
   * Get course detail by course ID.
   *
   * @param courseId the ID of the course
   * @return the course detail DTO
   * @throws IllegalArgumentException if the course is not found or inactive
   */
  public CourseDetailPageDto getCourseDetail(Long courseId) {
    return getCourseDetail(courseId, null);
  }

  // Lấy chi tiết khóa học kèm trạng thái đăng ký của học viên hiện tại nếu có đăng nhập.
  public CourseDetailPageDto getCourseDetail(Long courseId, User currentUser) {
    Course course =
        courseRepository.findById(courseId).filter(c -> Boolean.TRUE.equals(c.getActiveFlag()))
            .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));

    User teacher = userRepository.findById(course.getTeacherId()).orElse(null);
    List<Lesson> lessons = lessonRepository.findByCourseIdOrderByPositionAsc(course.getId());
    Map<Long, List<CourseLessonVideoDto>> lessonVideos = loadLessonVideos(lessons);
    long totalReviews = courseReviewRepository.countByCourseId(course.getId());
    Double avgRating = courseReviewRepository.findAverageRatingByCourseId(course.getId());
    boolean enrolled = isEnrolled(currentUser, course.getId());

    return new CourseDetailPageDto(course.getId(), course.getTitle(), course.getDescription(),
        teacher == null ? "BKIS Instructor" : teacher.getFullName(),
        teacher == null ? "Description about teacher" : teacher.getBio(),
        teacher == null ? "" : teacher.getProfilePictureUrl(),
        teacher == null ? "support@bkis.edu.vn" : teacher.getEmail(),
        normalizeImage(course.getImageUrl()), course.getTag(), defaultPrice(course.getPrice()),
        BkisNumberUtils.defaultInteger(course.getTotalStudents(), 0),
        normalizeRating(avgRating, course.getRating()), (int) totalReviews, lessons.size(),
        estimateDurationHours(lessons.size()), defaultHighlights(course.getHighlights()),
        lessons.stream().map(
                lesson -> toLessonDto(lesson, lessonVideos.getOrDefault(lesson.getId(), List.of())))
            .toList(), getRelatedCourses(course), enrolled);
  }

  // Kiểm tra học viên hiện tại đã có enrollment ACTIVE cho khóa học hay chưa.
  private boolean isEnrolled(User currentUser, Long courseId) {
    if (currentUser == null || currentUser.getId() == null) {
      return false;
    }
    return enrollmentRepository.existsByStudentIdAndCourseIdAndStatus(
        currentUser.getId(), courseId, EnrollmentStatus.ACTIVE);
  }

  /**
   * Load videos for each lesson and group them by lesson ID.
   *
   * @param lessons the lessons of the course
   * @return map of lesson ID to ordered video DTO list
   */
  private Map<Long, List<CourseLessonVideoDto>> loadLessonVideos(List<Lesson> lessons) {

    // mock video locked
    List<Long> videoLockedLessonIds = List.of(1L, 2L, 3L);

    // get videos for each lesson and convert to DTOs, by lesson IDs
    List<Long> lessonIds = lessons.stream().map(Lesson::getId).toList();
    List<LessonVideo> videos = lessonVideoRepository.findByLessonIdInOrderByPositionAsc(lessonIds);
    if (CollectionUtils.isEmpty(videos)) {
      return Map.of();
    }

    // convert to DTOs and mark videos as locked if their lesson ID is in the locked list
    List<CourseLessonVideoDto> videoDtos = videos.stream().map(video -> {
      boolean locked = videoLockedLessonIds.contains(video.getId());
      return new CourseLessonVideoDto(
          video.getId(), video.getLessonId(), video.getTitle(), video.getVideoUrl(),
          video.getDuration(), video.getPosition(), !locked
      );
    }).toList();

    // Group videoDtos by lesson ID for easy lookup when building lesson DTOs
    return videoDtos.stream().collect(Collectors.groupingBy(CourseLessonVideoDto::lessonId));
  }

  /**
   * Fetch and convert related courses to DTOs for display.
   *
   * @param course the current course (excluded from results)
   * @return list of up to 4 related active courses
   */
  private List<HomeCourseDto> getRelatedCourses(Course course) {
    List<Course> related =
        courseRepository.findTop4ByActiveFlagTrueAndIdNotOrderByCreatedAtDesc(course.getId());
    return related.stream().map(c -> new HomeCourseDto(c.getId(), c.getTitle(), c.getDescription(),
        userRepository.findById(c.getTeacherId()).map(User::getUsername).orElse("BKIS Instructor"),
        defaultPrice(c.getPrice()), BkisNumberUtils.defaultInteger(c.getTotalStudents(), 0),
        estimateDurationHours(6), BkisNumberUtils.defaultInteger(c.getRating(), 5),
        Math.max(50, BkisNumberUtils.defaultInteger(c.getTotalStudents(), 0)),
        normalizeImage(c.getImageUrl()), c.getTag())).toList();
  }

  /**
   * Convert Lesson entity to CourseLessonDto.
   *
   * @param lesson the Lesson entity
   * @return the DTO representation
   */
  private CourseLessonDto toLessonDto(Lesson lesson, List<CourseLessonVideoDto> videos) {
    boolean locked = lesson.getPosition() != null && lesson.getPosition() > 3;
    String previewVideoUrl = videos.stream().findFirst().map(CourseLessonVideoDto::videoUrl)
        .orElse(locked ? "" : "https://www.youtube.com/embed/1Rs2ND1ryYc");
    int totalDuration = videos.stream().map(CourseLessonVideoDto::durationMinutes)
        .filter(duration -> duration != null).reduce(0, Integer::sum);
    return new CourseLessonDto(
        lesson.getId(),
        lesson.getPosition(),
        lesson.getTitle(),
        "/assets/lesson-" + lesson.getId() + "-summary.pdf", totalDuration > 0 ?
        totalDuration : 10 + ((lesson.getPosition() == null ? 1 : lesson.getPosition()) * 4), videos);
  }

  /**
   * Generate default learning highlights if not provided.
   *
   * @return list of course learning objectives
   */
  private List<String> defaultHighlights(String highlights) {
    // split text by '||' or return default highlights if null/empty
    if (highlights == null || highlights.isBlank()) {
      return List.of("Build a complete course project from start to finish",
          "Practice with structured lessons and real examples",
          "Review progress with guided resources and mentor support",
          "Improve code quality, performance, and delivery skills");
    }
    return Arrays.stream(highlights.split("\\|\\|")).map(String::trim)
        .filter(value -> !value.isBlank()).toList();
  }

  /**
   * Estimate course duration in hours based on lesson count.
   *
   * @param lessons number of lessons in the course
   * @return estimated duration in hours (minimum 8)
   */
  private Integer estimateDurationHours(int lessons) {
    return Math.max(8, lessons * 2);
  }

  /**
   * Normalize and validate course rating score.
   *
   * @param avgRating      average rating from reviews (nullable)
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
   *
   * @param price the raw price (nullable)
   * @return normalized BigDecimal price or zero if null
   */
  private BigDecimal defaultPrice(BigDecimal price) {
    return price == null ? BigDecimal.ZERO : price.setScale(2, RoundingMode.HALF_UP);
  }

  /**
   * Normalize and validate image URL paths. Ensures URL is absolute path or HTTP/HTTPS URL.
   *
   * @param imageUrl the raw image URL (nullable)
   * @return normalized URL or default image path
   */
  private String normalizeImage(String imageUrl) {
    if (imageUrl == null || imageUrl.isBlank()) {
      return DEFAULT_COURSE_IMAGE;
    }
    if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://") || imageUrl.startsWith(
        "/")) {
      return imageUrl;
    }
    return "/" + imageUrl;
  }
}
