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
   * Khoi tao service chi tiet khoa hoc voi day du repository phu thuoc.
   *
   * @param courseRepository repository truy xuat du lieu khoa hoc
   * @param userRepository repository truy xuat du lieu giang vien va nguoi dung
   * @param courseReviewRepository repository truy xuat danh gia khoa hoc
   * @param lessonRepository repository truy xuat danh sach lesson cua khoa hoc
   * @param lessonVideoRepository repository truy xuat video cua tung lesson
   * @param enrollmentRepository repository kiem tra trang thai dang ky khoa hoc
   * @return khong tra du lieu; constructor dung de gan dependency cho service
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
   * Lay du lieu chi tiet khoa hoc cho man hinh public khi chua co ngu canh nguoi dung dang nhap.
   *
   * @param courseId id khoa hoc can hien thi chi tiet
   * @return {@link CourseDetailPageDto} chua du lieu khoa hoc de render man hinh chi tiet
   * @throws IllegalArgumentException nem ra khi khong tim thay khoa hoc hoac khoa hoc dang bi an
   */
  public CourseDetailPageDto getCourseDetail(Long courseId) {
    return getCourseDetail(courseId, null);
  }

  /**
   * Lay du lieu chi tiet khoa hoc kem quyen xem video cua nguoi dung hien tai.
   *
   * @param courseId id khoa hoc can hien thi
   * @param currentUser nguoi dung hien tai; co the la {@code null} neu khach chua dang nhap
   * @return {@link CourseDetailPageDto} chua du lieu khoa hoc, giao trinh, danh gia va trang thai dang ky
   * @throws IllegalArgumentException nem ra khi khoa hoc khong ton tai hoac khong con active
   */
  public CourseDetailPageDto getCourseDetail(Long courseId, User currentUser) {
    // Step 1: lay khoa hoc active tu database, neu khong co thi dung som de tranh render sai.
    Course course =
        courseRepository.findById(courseId).filter(c -> Boolean.TRUE.equals(c.getActiveFlag()))
            .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));

    // Step 2: tai du lieu lien quan gom giang vien, lesson, trang thai enrollment va danh sach video.
    User teacher = userRepository.findById(course.getTeacherId()).orElse(null);
    List<Lesson> lessons = lessonRepository.findByCourseIdOrderByPositionAsc(course.getId());
    boolean enrolled = isEnrolled(currentUser, course.getId());
    Map<Long, List<CourseLessonVideoDto>> lessonVideos = loadLessonVideos(lessons, enrolled);
    long totalReviews = courseReviewRepository.countByCourseId(course.getId());
    Double avgRating = courseReviewRepository.findAverageRatingByCourseId(course.getId());

    // Step 3: tong hop du lieu da tai va tra ve DTO cuoi cung cho man hinh chi tiet khoa hoc.
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
   * Kiem tra nguoi dung hien tai da co enrollment ACTIVE cho khoa hoc hay chua.
   *
   * @param currentUser nguoi dung dang thao tac; co the la {@code null}
   * @param courseId id khoa hoc can kiem tra quyen truy cap
   * @return {@code true} neu nguoi dung da dang ky khoa hoc va enrollment dang active, nguoc lai tra ve {@code false}
   */
  private boolean isEnrolled(User currentUser, Long courseId) {
    // Step 1: chan som truong hop khach chua dang nhap hoac user khong co id hop le.
    if (currentUser == null || currentUser.getId() == null) {
      return false;
    }

    // Step 2: tra cuu truc tiep tren bang enrollments de xac dinh quyen hoc khoa hoc.
    return enrollmentRepository.existsByStudentIdAndCourseIdAndStatus(
        currentUser.getId(), courseId, EnrollmentStatus.ACTIVE);
  }

  /**
   * Xac dinh video ngoai he thong de frontend mo embed truc tiep.
   * @param videoUrl URL video dang luu trong database
   * @return true neu la URL ngoai he thong, nguoc lai false
   */
  private boolean isExternalVideoUrl(String videoUrl) {
    return videoUrl != null && (videoUrl.startsWith("http://") || videoUrl.startsWith("https://"))
            && !videoUrl.contains("/uploads/");
  }

  /**
   * Tai danh sach video theo lesson va tinh quyen xem tung video dua tren trang thai dang ky.
   *
   * @param lessons danh sach lesson cua khoa hoc hien tai
   * @param enrolled co cho biet nguoi dung hien tai da so huu khoa hoc hay chua
   * @return {@link Map} anh xa lessonId sang danh sach {@link CourseLessonVideoDto} da sap xep theo position
   */
  private Map<Long, List<CourseLessonVideoDto>> loadLessonVideos(List<Lesson> lessons,
      boolean enrolled) {

    // Step 1: lay toan bo id lesson de truy van video trong bang lesson_videos theo dung thu tu hien thi.
    List<Long> lessonIds = lessons.stream().map(Lesson::getId).toList();
    List<LessonVideo> videos = lessonVideoRepository.findByLessonIdInOrderByPositionAsc(lessonIds);
    if (CollectionUtils.isEmpty(videos)) {
      return Map.of();
    }

    // Step 2: map tung video sang DTO va chi tra URL that cho video preview hoac video cua khoa da mua.
    List<CourseLessonVideoDto> videoDtos = videos.stream().map(video -> {
      boolean preview = Boolean.TRUE.equals(video.getPreview());
      boolean accessible = enrolled || preview;
      return new CourseLessonVideoDto(
          video.getId(), video.getLessonId(), video.getTitle(),
          accessible && isExternalVideoUrl(video.getVideoUrl()) ? video.getVideoUrl() : "",
          video.getDuration(), video.getPosition(), accessible, preview
      );
    }).toList();

    // Step 3: gom nhom ket qua theo lessonId de service cha render curriculum nhanh va ro hon.
    return videoDtos.stream().collect(Collectors.groupingBy(CourseLessonVideoDto::lessonId));
  }

  /**
   * Tai danh sach khoa hoc lien quan de hien thi o cuoi trang chi tiet khoa hoc.
   *
   * @param course khoa hoc hien tai can loai tru khoi danh sach lien quan
   * @return {@link List} toi da 4 {@link HomeCourseDto} active gan nhat
   */
  private List<HomeCourseDto> getRelatedCourses(Course course) {
    // Step 1: lay danh sach khoa hoc active khac khoa hien tai tu database.
    List<Course> related =
        courseRepository.findTop4ByActiveFlagTrueAndIdNotOrderByCreatedAtDesc(course.getId());

    // Step 2: map du lieu khoa hoc sang DTO dung lai cho khoi related courses ngoai trang public.
    return related.stream().map(c -> new HomeCourseDto(c.getId(), c.getTitle(), c.getDescription(),
        userRepository.findById(c.getTeacherId()).map(User::getUsername).orElse("BKIS Instructor"),
        defaultPrice(c.getPrice()), BkisNumberUtils.defaultInteger(c.getTotalStudents(), 0),
        estimateDurationHours(6), BkisNumberUtils.defaultInteger(c.getRating(), 5),
        Math.max(50, BkisNumberUtils.defaultInteger(c.getTotalStudents(), 0)),
        normalizeImage(c.getImageUrl()), c.getTag())).toList();
  }

  /**
   * Chuyen mot lesson cung danh sach video cua lesson do sang DTO phuc vu man hinh chi tiet khoa hoc.
   *
   * @param lesson lesson nguon lay tu bang lessons
   * @param videos danh sach video da duoc tinh quyen xem cho lesson tuong ung
   * @return {@link CourseLessonDto} chua thong tin lesson, tong thoi luong va danh sach video
   */
  private CourseLessonDto toLessonDto(Lesson lesson, List<CourseLessonVideoDto> videos) {
    // Step 1: cong tong thoi luong video de hien thi thoi luong lesson tren giao dien.
    int totalDuration = videos.stream().map(CourseLessonVideoDto::durationMinutes)
        .filter(duration -> duration != null).reduce(0, Integer::sum);

    // Step 2: dung DTO lesson voi du lieu video da duoc xu ly quyen xem o buoc truoc.
    return new CourseLessonDto(
        lesson.getId(),
        lesson.getPosition(),
        lesson.getTitle(),
        "/assets/lesson-" + lesson.getId() + "-summary.pdf", totalDuration > 0 ?
        totalDuration : 10 + ((lesson.getPosition() == null ? 1 : lesson.getPosition()) * 4), videos);
  }

  /**
   * Sinh danh sach highlights mac dinh khi khoa hoc chua co noi dung highlights rieng.
   *
   * @param highlights chuoi highlights tho, phan tach bang `||`
   * @return {@link List} danh sach cac highlight da duoc chuan hoa
   */
  private List<String> defaultHighlights(String highlights) {
    // Step 1: neu khoa hoc chua co highlights thi tra ve bo highlight mac dinh cho giao dien.
    if (highlights == null || highlights.isBlank()) {
      return List.of("Build a complete course project from start to finish",
          "Practice with structured lessons and real examples",
          "Review progress with guided resources and mentor support",
          "Improve code quality, performance, and delivery skills");
    }

    // Step 2: tach chuoi highlights theo dau phan cach va loai bo gia tri rong.
    return Arrays.stream(highlights.split("\\|\\|")).map(String::trim)
        .filter(value -> !value.isBlank()).toList();
  }

  /**
   * Uoc luong thoi luong khoa hoc theo so luong lesson.
   *
   * @param lessons so luong lesson hien co cua khoa hoc
   * @return {@link Integer} so gio uoc luong, toi thieu la 8 gio
   */
  private Integer estimateDurationHours(int lessons) {
    // Step 1: nhan so lesson voi he so hien thi don gian cho man hinh public.
    // Step 2: dam bao khoa hoc luon co thoi luong hien thi toi thieu la 8 gio.
    return Math.max(8, lessons * 2);
  }

  /**
   * Chuan hoa diem rating hien thi cua khoa hoc tu review hoac fallback co san.
   *
   * @param avgRating diem trung binh lay tu bang review; co the la {@code null}
   * @param fallbackRating diem fallback dang luu tren khoa hoc
   * @return {@link Integer} diem rating da duoc lam tron va nam trong luong hien thi an toan
   */
  private Integer normalizeRating(Double avgRating, Integer fallbackRating) {
    // Step 1: uu tien diem trung binh thuc te neu khoa hoc da co review.
    if (avgRating != null && avgRating > 0) {
      return (int) Math.round(avgRating);
    }

    // Step 2: fallback ve rating tinh tren khoa hoc neu chua co review thuc.
    return BkisNumberUtils.defaultInteger(fallbackRating, 5);
  }

  /**
   * Chuan hoa gia tien khoa hoc ve dung scale hien thi.
   *
   * @param price gia tien tho lay tu database; co the la {@code null}
   * @return {@link BigDecimal} gia tien da duoc chuan hoa ve 2 chu so thap phan
   */
  private BigDecimal defaultPrice(BigDecimal price) {
    // Step 1: neu du lieu gia rong thi tra ve 0 de man hinh khong loi.
    // Step 2: neu co gia thi chuan hoa scale ve 2 chu so thap phan.
    return price == null ? BigDecimal.ZERO : price.setScale(2, RoundingMode.HALF_UP);
  }

  /**
   * Chuan hoa duong dan anh khoa hoc de template luon nhan duoc URL hop le.
   *
   * @param imageUrl duong dan anh tho lay tu database; co the la {@code null}
   * @return {@link String} URL anh hop le hoac anh mac dinh cua he thong
   */
  private String normalizeImage(String imageUrl) {
    // Step 1: fallback ve anh mac dinh neu khoa hoc chua co anh rieng.
    if (imageUrl == null || imageUrl.isBlank()) {
      return DEFAULT_COURSE_IMAGE;
    }

    // Step 2: giu nguyen URL tuyet doi hoac duong dan tuyet doi da hop le.
    if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://") || imageUrl.startsWith(
        "/")) {
      return imageUrl;
    }

    // Step 3: them dau / dau chuoi de template nhan dung duong dan tinh noi bo.
    return "/" + imageUrl;
  }
}
