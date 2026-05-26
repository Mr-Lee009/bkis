package vn.edu.bkis.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
   * Khởi tạo service chi tiết khóa học với đầy đủ repository phụ thuộc.
   *
   * @param courseRepository repository truy xuất dữ liệu khóa học
   * @param userRepository repository truy xuất dữ liệu giảng viên và người dùng
   * @param courseReviewRepository repository truy xuất đánh giá khóa học
   * @param lessonRepository repository truy xuất danh sách lesson của khóa học
   * @param lessonVideoRepository repository truy xuất video của từng lesson
   * @param enrollmentRepository repository kiểm tra trạng thái đăng ký khóa học
   * @return không trả dữ liệu; constructor dùng để gán dependency cho service
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
   * Lấy dữ liệu chi tiết khóa học cho màn hình public khi chưa có ngữ cảnh người dùng đăng nhập.
   *
   * @param courseId id khóa học cần hiển thị chi tiết
   * @return {@link CourseDetailPageDto} chứa dữ liệu khóa học để render màn hình chi tiết
   * @throws IllegalArgumentException ném ra khi không tìm thấy khóa học hoặc khóa học đang bị ẩn
   */
  public CourseDetailPageDto getCourseDetail(Long courseId) {
    return getCourseDetail(courseId, null);
  }

  /**
   * Lấy dữ liệu chi tiết khóa học kèm quyền xem video của người dùng hiện tại.
   *
   * @param courseId id khóa học cần hiển thị
   * @param currentUser người dùng hiện tại; có thể là {@code null} nếu khách chưa đăng nhập
   * @return {@link CourseDetailPageDto} chứa dữ liệu khóa học, giáo trình, đánh giá và trạng thái đăng ký
   * @throws IllegalArgumentException ném ra khi khóa học không tồn tại hoặc không còn active
   */
  public CourseDetailPageDto getCourseDetail(Long courseId, User currentUser) {
    // Step 1: lấy khóa học active từ database, nếu không có thì dừng sớm để tránh render sai.
    Course course =
        courseRepository.findById(courseId).filter(c -> Boolean.TRUE.equals(c.getActiveFlag()))
            .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));

    // Step 2: tải dữ liệu liên quan gồm giảng viên, lesson, trạng thái enrollment và danh sách video.
    User teacher = userRepository.findById(course.getTeacherId()).orElse(null);
    List<Lesson> lessons = lessonRepository.findByCourseIdOrderByPositionAsc(course.getId());
    boolean enrolled = isEnrolled(currentUser, course.getId());
    Map<Long, List<CourseLessonVideoDto>> lessonVideos = loadLessonVideos(lessons, enrolled);
    long totalReviews = courseReviewRepository.countByCourseId(course.getId());
    Double avgRating = courseReviewRepository.findAverageRatingByCourseId(course.getId());

    // Step 3: tổng hợp dữ liệu đã tải và trả về DTO cuối cùng cho màn hình chi tiết khóa học.
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

  /**
   * Kiểm tra người dùng hiện tại đã có enrollment ACTIVE cho khóa học hay chưa.
   *
   * @param currentUser người dùng đang thao tác; có thể là {@code null}
   * @param courseId id khóa học cần kiểm tra quyền truy cập
   * @return {@code true} nếu người dùng đã đăng ký khóa học và enrollment đang active, ngược lại trả về {@code false}
   */
  private boolean isEnrolled(User currentUser, Long courseId) {
    // Step 1: chặn sớm trường hợp khách chưa đăng nhập hoặc user không có id hợp lệ.
    if (currentUser == null || currentUser.getId() == null) {
      return false;
    }

    // Step 2: tra cứu trực tiếp trên bảng enrollments để xác định quyền học khóa học.
    return enrollmentRepository.existsByStudentIdAndCourseIdAndStatus(
        currentUser.getId(), courseId, EnrollmentStatus.ACTIVE);
  }

  /**
   * Tải danh sách video theo lesson và tính quyền xem từng video dựa trên trạng thái đăng ký.
   *
   * @param lessons danh sách lesson của khóa học hiện tại
   * @param enrolled cờ cho biết người dùng hiện tại đã sở hữu khóa học hay chưa
   * @return {@link Map} ánh xạ lessonId sang danh sách {@link CourseLessonVideoDto} đã sắp xếp theo position
   */
  private Map<Long, List<CourseLessonVideoDto>> loadLessonVideos(List<Lesson> lessons,
      boolean enrolled) {

    // Step 1: lấy toàn bộ id lesson để truy vấn video trong bảng lesson_videos theo đúng thứ tự hiển thị.
    List<Long> lessonIds = lessons.stream().map(Lesson::getId).toList();
    List<LessonVideo> videos = lessonVideoRepository.findByLessonIdInOrderByPositionAsc(lessonIds);
    if (CollectionUtils.isEmpty(videos)) {
      return Map.of();
    }

    // Step 2: map từng video sang DTO và chỉ trả URL thật cho video preview hoặc video của khóa đã mua.
    List<CourseLessonVideoDto> videoDtos = videos.stream().map(video -> {
      boolean preview = Boolean.TRUE.equals(video.getPreview());
      boolean accessible = enrolled || preview;
      return new CourseLessonVideoDto(
          video.getId(), video.getLessonId(), video.getTitle(),
          accessible ? video.getVideoUrl() : "",
          video.getDuration(), video.getPosition(), accessible, preview
      );
    }).toList();

    // Step 3: gom nhóm kết quả theo lessonId để service cha render curriculum nhanh và rõ hơn.
    return videoDtos.stream().collect(Collectors.groupingBy(CourseLessonVideoDto::lessonId));
  }

  /**
   * Tải danh sách khóa học liên quan để hiển thị ở cuối trang chi tiết khóa học.
   *
   * @param course khóa học hiện tại cần loại trừ khỏi danh sách liên quan
   * @return {@link List} tối đa 4 {@link HomeCourseDto} active gần nhất
   */
  private List<HomeCourseDto> getRelatedCourses(Course course) {
    // Step 1: lấy danh sách khóa học active khác khóa hiện tại từ database.
    List<Course> related =
        courseRepository.findTop4ByActiveFlagTrueAndIdNotOrderByCreatedAtDesc(course.getId());

    // Step 2: map dữ liệu khóa học sang DTO dùng lại cho khối related courses ngoài trang public.
    return related.stream().map(c -> new HomeCourseDto(c.getId(), c.getTitle(), c.getDescription(),
        userRepository.findById(c.getTeacherId()).map(User::getUsername).orElse("BKIS Instructor"),
        defaultPrice(c.getPrice()), BkisNumberUtils.defaultInteger(c.getTotalStudents(), 0),
        estimateDurationHours(6), BkisNumberUtils.defaultInteger(c.getRating(), 5),
        Math.max(50, BkisNumberUtils.defaultInteger(c.getTotalStudents(), 0)),
        normalizeImage(c.getImageUrl()), c.getTag())).toList();
  }

  /**
   * Chuyển một lesson cùng danh sách video của lesson đó sang DTO phục vụ màn hình chi tiết khóa học.
   *
   * @param lesson lesson nguồn lấy từ bảng lessons
   * @param videos danh sách video đã được tính quyền xem cho lesson tương ứng
   * @return {@link CourseLessonDto} chứa thông tin lesson, tổng thời lượng và danh sách video
   */
  private CourseLessonDto toLessonDto(Lesson lesson, List<CourseLessonVideoDto> videos) {
    // Step 1: cộng tổng thời lượng video để hiển thị thời lượng lesson trên giao diện.
    int totalDuration = videos.stream().map(CourseLessonVideoDto::durationMinutes)
        .filter(duration -> duration != null).reduce(0, Integer::sum);

    // Step 2: dựng DTO lesson với dữ liệu video đã được xử lý quyền xem ở bước trước.
    return new CourseLessonDto(
        lesson.getId(),
        lesson.getPosition(),
        lesson.getTitle(),
        "/assets/lesson-" + lesson.getId() + "-summary.pdf", totalDuration > 0 ?
        totalDuration : 10 + ((lesson.getPosition() == null ? 1 : lesson.getPosition()) * 4), videos);
  }

  /**
   * Sinh danh sách highlights mặc định khi khóa học chưa có nội dung highlights riêng.
   *
   * @param highlights chuỗi highlights thô, phân tách bằng `||`
   * @return {@link List} danh sách các highlight đã được chuẩn hóa
   */
  private List<String> defaultHighlights(String highlights) {
    // Step 1: nếu khóa học chưa có highlights thì trả về bộ highlight mặc định cho giao diện.
    if (highlights == null || highlights.isBlank()) {
      return List.of("Build a complete course project from start to finish",
          "Practice with structured lessons and real examples",
          "Review progress with guided resources and mentor support",
          "Improve code quality, performance, and delivery skills");
    }

    // Step 2: tách chuỗi highlights theo dấu phân cách và loại bỏ giá trị rỗng.
    return Arrays.stream(highlights.split("\\|\\|")).map(String::trim)
        .filter(value -> !value.isBlank()).toList();
  }

  /**
   * Ước lượng thời lượng khóa học theo số lượng lesson.
   *
   * @param lessons số lượng lesson hiện có của khóa học
   * @return {@link Integer} số giờ ước lượng, tối thiểu là 8 giờ
   */
  private Integer estimateDurationHours(int lessons) {
    // Step 1: nhân số lesson với hệ số hiển thị đơn giản cho màn hình public.
    // Step 2: đảm bảo khóa học luôn có thời lượng hiển thị tối thiểu là 8 giờ.
    return Math.max(8, lessons * 2);
  }

  /**
   * Chuẩn hóa điểm rating hiển thị của khóa học từ review hoặc fallback có sẵn.
   *
   * @param avgRating điểm trung bình lấy từ bảng review; có thể là {@code null}
   * @param fallbackRating điểm fallback đang lưu trên khóa học
   * @return {@link Integer} điểm rating đã được làm tròn và nằm trong luồng hiển thị an toàn
   */
  private Integer normalizeRating(Double avgRating, Integer fallbackRating) {
    // Step 1: ưu tiên điểm trung bình thực tế nếu khóa học đã có review.
    if (avgRating != null && avgRating > 0) {
      return (int) Math.round(avgRating);
    }

    // Step 2: fallback về rating tĩnh trên khóa học nếu chưa có review thực.
    return BkisNumberUtils.defaultInteger(fallbackRating, 5);
  }

  /**
   * Chuẩn hóa giá tiền khóa học về đúng scale hiển thị.
   *
   * @param price giá tiền thô lấy từ database; có thể là {@code null}
   * @return {@link BigDecimal} giá tiền đã được chuẩn hóa về 2 chữ số thập phân
   */
  private BigDecimal defaultPrice(BigDecimal price) {
    // Step 1: nếu dữ liệu giá rỗng thì trả về 0 để màn hình không lỗi.
    // Step 2: nếu có giá thì chuẩn hóa scale về 2 chữ số thập phân.
    return price == null ? BigDecimal.ZERO : price.setScale(2, RoundingMode.HALF_UP);
  }

  /**
   * Chuẩn hóa đường dẫn ảnh khóa học để template luôn nhận được URL hợp lệ.
   *
   * @param imageUrl đường dẫn ảnh thô lấy từ database; có thể là {@code null}
   * @return {@link String} URL ảnh hợp lệ hoặc ảnh mặc định của hệ thống
   */
  private String normalizeImage(String imageUrl) {
    // Step 1: fallback về ảnh mặc định nếu khóa học chưa có ảnh riêng.
    if (imageUrl == null || imageUrl.isBlank()) {
      return DEFAULT_COURSE_IMAGE;
    }

    // Step 2: giữ nguyên URL tuyệt đối hoặc đường dẫn tuyệt đối đã hợp lệ.
    if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://") || imageUrl.startsWith(
        "/")) {
      return imageUrl;
    }

    // Step 3: thêm dấu / đầu chuỗi để template nhận đúng đường dẫn tĩnh nội bộ.
    return "/" + imageUrl;
  }
}
