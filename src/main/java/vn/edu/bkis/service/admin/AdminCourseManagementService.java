package vn.edu.bkis.service.admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.bkis.dto.admin.course.AdminCourseDetailDto;
import vn.edu.bkis.dto.admin.course.AdminCourseDetailProjection;
import vn.edu.bkis.dto.admin.course.AdminCourseCreateFormDto;
import vn.edu.bkis.dto.admin.course.AdminCourseFilterDto;
import vn.edu.bkis.dto.admin.course.AdminCourseListItemDto;
import vn.edu.bkis.dto.admin.course.AdminCourseListPageDto;
import vn.edu.bkis.dto.admin.course.AdminCourseListProjection;
import vn.edu.bkis.dto.admin.course.AdminCourseModuleFormDto;
import vn.edu.bkis.dto.admin.course.AdminCourseModuleDto;
import vn.edu.bkis.dto.admin.course.AdminCourseSummaryDto;
import vn.edu.bkis.dto.admin.course.AdminCourseUpdateFormDto;
import vn.edu.bkis.dto.admin.course.AdminCourseVideoFormDto;
import vn.edu.bkis.dto.admin.course.AdminCourseVideoDto;
import vn.edu.bkis.dto.admin.AdminOptionDto;
import vn.edu.bkis.model.Course;
import vn.edu.bkis.model.CourseStatus;
import vn.edu.bkis.model.Lesson;
import vn.edu.bkis.model.LessonVideo;
import vn.edu.bkis.model.UserRole;
import vn.edu.bkis.repository.CourseRepository;
import vn.edu.bkis.repository.LessonRepository;
import vn.edu.bkis.repository.LessonVideoRepository;
import vn.edu.bkis.repository.ProgressRepository;
import vn.edu.bkis.repository.UserRepository;
import vn.edu.bkis.service.UploadService;

/**
 * Service for the admin course management pages.
 */
@Service
public class AdminCourseManagementService {
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;
    private static final DateTimeFormatter DATE_FORMATTER =
        DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.forLanguageTag("vi-VN"));

    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final LessonVideoRepository lessonVideoRepository;
    private final ProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final UploadService uploadService;

    public AdminCourseManagementService(CourseRepository courseRepository,
                                        LessonRepository lessonRepository,
                                        LessonVideoRepository lessonVideoRepository,
                                        ProgressRepository progressRepository,
                                        UserRepository userRepository,
                                        UploadService uploadService) {
        this.courseRepository = courseRepository;
        this.lessonRepository = lessonRepository;
        this.lessonVideoRepository = lessonVideoRepository;
        this.progressRepository = progressRepository;
        this.userRepository = userRepository;
        this.uploadService = uploadService;
    }

    /**
     * Build data for the admin course list.
     *
     * @param filter requested filters
     * @return list page DTO
     */
    @Transactional(readOnly = true)
    public AdminCourseListPageDto getCourseListPage(AdminCourseFilterDto filter) {
        Pageable pageable = PageRequest.of(resolvePage(filter.getPage()), resolveSize(filter.getSize()));
        Page<AdminCourseListProjection> coursePage = courseRepository.searchAdminCourses(
            normalize(filter.getKeyword()),
            filter.getYear(),
            normalizeStatus(filter.getStatus()),
            pageable
        );

        List<Integer> years = courseRepository.findAdminCourseYears();
        if (years.isEmpty()) {
            years = List.of(Year.now().getValue());
        }

        AdminCourseSummaryDto summary = new AdminCourseSummaryDto(
            courseRepository.countAdminCoursesByYear(Year.now().getValue()),
            courseRepository.countDraftCoursesForAdmin(),
            courseRepository.countPublishedStatusCoursesForAdmin(),
            courseRepository.countHiddenCoursesForAdmin(),
            courseRepository.count()
        );

        return new AdminCourseListPageDto(
            summary,
            coursePage.getContent().stream().map(this::toListItemDto).toList(),
            years,
            coursePage.getNumber(),
            coursePage.getSize(),
            Math.max(coursePage.getTotalPages(), 1),
            coursePage.getTotalElements()
        );
    }

    /**
     * Load one course for the admin detail page.
     *
     * @param courseId requested course id
     * @return detail DTO
     */
    @Transactional(readOnly = true)
    public AdminCourseDetailDto getCourseDetail(Long courseId) {
        AdminCourseDetailProjection projection = courseRepository.findAdminCourseDetailById(courseId)
            .orElseThrow(() -> new IllegalArgumentException("Course not found."));

        List<AdminCourseModuleDto> modules = getModules(courseId);
        long moduleCount = safeLong(projection.getModuleCount());
        long videoCount = safeLong(projection.getVideoCount());

        return new AdminCourseDetailDto(
            projection.getId(),
            projection.getTitle(),
            projection.getDescription(),
            projection.getHighlights(),
            projection.getTeacherId(),
            projection.getTeacherName(),
            projection.getPrice(),
            projection.getTag(),
            projection.getImageUrl(),
            projection.getRating(),
            resolveYear(projection.getUpdatedAt(), projection.getCreatedAt()),
            resolvePersistedStatus(projection.getCourseStatus(), projection.getActiveFlag()).name(),
            resolveStatusLabel(resolvePersistedStatus(projection.getCourseStatus(), projection.getActiveFlag())),
            Boolean.TRUE.equals(projection.getActiveFlag()),
            safeLong(projection.getEnrolledStudents()),
            projection.getRevenue() == null ? BigDecimal.ZERO : projection.getRevenue(),
            moduleCount,
            videoCount,
            safeLong(projection.getPaymentCount()),
            formatDate(projection.getCreatedAt()),
            formatDate(projection.getUpdatedAt()),
            calculateCompletionPercent(projection, moduleCount, videoCount),
            modules
        );
    }

    /**
     * Create update form from current detail data.
     *
     * @param detail course detail
     * @return update form
     */
    public AdminCourseUpdateFormDto toUpdateForm(AdminCourseDetailDto detail) {
        AdminCourseUpdateFormDto form = new AdminCourseUpdateFormDto();
        form.setId(detail.getId());
        form.setTitle(detail.getTitle());
        form.setDescription(detail.getDescription());
        form.setHighlights(detail.getHighlights());
        form.setTeacherId(detail.getTeacherId());
        form.setPrice(detail.getPrice());
        form.setTag(detail.getTag());
        form.setImageUrl(detail.getImageUrl());
        form.setStatus(detail.getStatus());
        form.setVisible(detail.isVisible());
        return form;
    }

    /**
     * Teacher options for admin course forms.
     *
     * @return teacher/instructor options
     */
    @Transactional(readOnly = true)
    public List<AdminOptionDto> getTeacherOptions() {
        return userRepository.findByRoleInOrderByFullNameAsc(List.of(UserRole.TEACHER, UserRole.INSTRUCTOR)).stream()
            .map(user -> new AdminOptionDto(user.getId(), user.getFullName()))
            .toList();
    }

    /**
     * Tạo khóa học ở trạng thái nháp để admin bổ sung giáo trình và tài nguyên ở trang chi tiết.
     *
     * @param form dữ liệu khóa học cơ bản từ popup tạo nháp
     * @return id khóa học vừa tạo
     */
    @Transactional
    public Long createDraftCourse(AdminCourseCreateFormDto form) {
        Course course = new Course();

        // Gán dữ liệu bắt buộc và dữ liệu mô tả cơ bản cho hồ sơ khóa học.
        course.setTitle(required(form.getTitle(), "Course title is required."));
        course.setTeacherId(required(form.getTeacherId(), "Teacher is required."));
        course.setDescription(form.getDescription());
        course.setHighlights(form.getHighlights());
        course.setTag(form.getTag());
        course.setImageUrl(form.getImageUrl());

        // Khóa nháp không được public cho đến khi admin chuyển sang PUBLISHED.
        course.setPrice(form.getPrice() == null ? BigDecimal.ZERO : form.getPrice());
        course.setTotalStudents(0);
        course.setRating(5);
        course.setCourseStatus(CourseStatus.DRAFT);
        course.setActiveFlag(false);
        course.setCreatedBy("admin");
        course.setUpdatedBy("admin");

        return courseRepository.save(course).getId();
    }

    /**
     * Update an existing course.
     *
     * @param form submitted form
     */
    @Transactional
    public void updateCourse(AdminCourseUpdateFormDto form) {
        if (form.getId() == null) {
            throw new IllegalArgumentException("Course id is required.");
        }

        Course course = courseRepository.findById(form.getId())
            .orElseThrow(() -> new IllegalArgumentException("Course not found."));

        course.setTitle(required(form.getTitle(), "Course title is required."));
        course.setDescription(form.getDescription());
        course.setHighlights(form.getHighlights());
        course.setTeacherId(required(form.getTeacherId(), "Teacher is required."));
        course.setPrice(form.getPrice() == null ? BigDecimal.ZERO : form.getPrice());
        course.setTag(form.getTag());
        course.setImageUrl(form.getImageUrl());
        CourseStatus status = resolveStatus(form.getStatus(), form.getVisible());
        course.setCourseStatus(status);
        course.setActiveFlag(resolveActiveFlag(status));
        course.setUpdatedBy("admin");

        courseRepository.save(course);
    }

    /**
     * Delete a course when possible, otherwise hide it.
     *
     * @param courseId requested course id
     * @return true if deleted, false if archived/hidden
     */
    @Transactional
    public boolean deleteOrArchiveCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new IllegalArgumentException("Course not found."));

        boolean hasBusinessData = courseRepository.countEnrollmentsByCourseId(courseId) > 0
            || courseRepository.countPaymentsByCourseId(courseId) > 0;
        if (hasBusinessData) {
            course.setActiveFlag(false);
            course.setCourseStatus(CourseStatus.HIDDEN);
            course.setUpdatedBy("admin");
            courseRepository.save(course);
            return false;
        }

        courseRepository.delete(course);
        return true;
    }

    /**
     * Create a module for a course.
     *
     * @param courseId owning course id
     * @param form submitted module form
     */
    @Transactional
    public void createModule(Long courseId, AdminCourseModuleFormDto form) {
        ensureCourseExists(courseId);
        Lesson lesson = new Lesson();
        lesson.setCourseId(courseId);
        applyModuleForm(lesson, form);
        lesson.setCreatedBy("admin");
        lesson.setUpdatedBy("admin");
        lessonRepository.save(lesson);
    }

    /**
     * Update a module in a course.
     *
     * @param courseId owning course id
     * @param moduleId module id
     * @param form submitted module form
     */
    @Transactional
    public void updateModule(Long courseId, Long moduleId, AdminCourseModuleFormDto form) {
        Lesson lesson = getLessonInCourse(courseId, moduleId);
        applyModuleForm(lesson, form);
        lesson.setUpdatedBy("admin");
        lessonRepository.save(lesson);
    }

    /**
     * Delete a module and its videos when no progress depends on them.
     *
     * @param courseId owning course id
     * @param moduleId module id
     */
    @Transactional
    public void deleteModule(Long courseId, Long moduleId) {
        Lesson lesson = getLessonInCourse(courseId, moduleId);
        List<Long> videoIds = lessonVideoRepository.findByLessonIdOrderByPositionAsc(moduleId).stream()
            .map(LessonVideo::getId)
            .toList();
        if (!videoIds.isEmpty() && progressRepository.countByLessonVideoIdIn(videoIds) > 0) {
            throw new IllegalArgumentException("Cannot delete this module because students already have progress in its videos.");
        }
        lessonVideoRepository.deleteByLessonId(moduleId);
        lessonRepository.delete(lesson);
    }

    /**
     * Create a video under a course module.
     *
     * @param courseId owning course id
     * @param moduleId module id
     * @param form submitted video form
     */
    @Transactional
    public void createVideo(Long courseId, Long moduleId, AdminCourseVideoFormDto form) {
        getLessonInCourse(courseId, moduleId);
        LessonVideo video = new LessonVideo();
        video.setLessonId(moduleId);
        applyVideoForm(video, form);
        video.setCreatedBy("admin");
        video.setUpdatedBy("admin");
        lessonVideoRepository.save(video);
    }

    /**
     * Update a lesson video.
     *
     * @param courseId owning course id
     * @param moduleId module id
     * @param videoId video id
     * @param form submitted video form
     */
    @Transactional
    public void updateVideo(Long courseId, Long moduleId, Long videoId, AdminCourseVideoFormDto form) {
        getLessonInCourse(courseId, moduleId);
        LessonVideo video = getVideoInLesson(moduleId, videoId);
        String oldVideoUrl = video.getVideoUrl();
        String newVideoUrl = required(form.getVideoUrl(), "Video URL is required.");

        // Neu doi sang video moi trong he thong upload noi bo thi xoa file cu truoc khi cap nhat DB.
        if (shouldDeleteOldVideo(oldVideoUrl, newVideoUrl)) {
            boolean deleted = uploadService.deleteUploadedFile(oldVideoUrl);
            if (!deleted) {
                throw new IllegalArgumentException("Cannot replace video because old file is not managed by upload service.");
            }
        }

        form.setVideoUrl(newVideoUrl);
        applyVideoForm(video, form);
        video.setUpdatedBy("admin");
        lessonVideoRepository.save(video);
    }

    /**
     * Delete a lesson video when no student progress depends on it.
     *
     * @param courseId owning course id
     * @param moduleId module id
     * @param videoId video id
     */
    @Transactional
    public void deleteVideo(Long courseId, Long moduleId, Long videoId) {
        getLessonInCourse(courseId, moduleId);
        LessonVideo video = getVideoInLesson(moduleId, videoId);
        if (progressRepository.countByLessonVideoId(videoId) > 0) {
            throw new IllegalArgumentException("Cannot delete this video because students already have progress for it.");
        }
        lessonVideoRepository.delete(video);
    }

    /**
     * Tải danh sách module và video của khóa học để render màn hình chi tiết quản trị.
     *
     * @param courseId id khóa học cần lấy giáo trình
     * @return {@link List} danh sách {@link AdminCourseModuleDto} đã kèm các video của từng module
     */
    private List<AdminCourseModuleDto> getModules(Long courseId) {
        // Step 1: lấy toàn bộ lesson của khóa học theo đúng thứ tự module.
        List<Lesson> lessons = lessonRepository.findByCourseIdOrderByPositionAsc(courseId);
        List<Long> lessonIds = lessons.stream().map(Lesson::getId).toList();
        // Step 2: tải video theo nhóm lesson để tránh query lặp lại cho từng module.
        Map<Long, List<LessonVideo>> videosByLesson = lessonIds.isEmpty()
            ? Map.of()
            : lessonVideoRepository.findByLessonIdInOrderByPositionAsc(lessonIds).stream()
                .collect(Collectors.groupingBy(LessonVideo::getLessonId));

        // Step 3: map lesson và video sang DTO cuối cùng để controller render ra màn hình admin.
        return lessons.stream()
            .map(lesson -> {
                List<AdminCourseVideoDto> videos = videosByLesson.getOrDefault(lesson.getId(), List.of()).stream()
                    .map(this::toVideoDto)
                    .toList();
                return new AdminCourseModuleDto(
                    lesson.getId(),
                    lesson.getTitle(),
                    lesson.getDescription(),
                    lesson.getPosition(),
                    videos.size(),
                    videos
                );
            })
            .toList();
    }

    /**
     * Chuyển entity video sang DTO hiển thị ở màn hình quản trị khóa học.
     *
     * @param video video nguồn lấy từ bảng lesson_videos
     * @return {@link AdminCourseVideoDto} chứa dữ liệu hiển thị và nhãn trạng thái preview
     */
    private AdminCourseVideoDto toVideoDto(LessonVideo video) {
        // Step 1: đọc cờ preview từ entity để xác định trạng thái hiển thị.
        // Step 2: dựng DTO với nhãn dễ đọc để admin biết video nào đang mở xem thử.
        return new AdminCourseVideoDto(
            video.getId(),
            video.getTitle(),
            video.getVideoUrl(),
            video.getDuration(),
            formatDuration(video.getDuration()),
            video.getPosition(),
            Boolean.TRUE.equals(video.getPreview()),
            Boolean.TRUE.equals(video.getPreview()) ? "Xem thử" : "Nội dung khóa học"
        );
    }

    private AdminCourseListItemDto toListItemDto(AdminCourseListProjection projection) {
        return new AdminCourseListItemDto(
            projection.getId(),
            projection.getTitle(),
            projection.getTag(),
            resolveYear(projection.getUpdatedAt(), projection.getCreatedAt()),
            projection.getTeacherName() == null ? "Chua phan cong" : projection.getTeacherName(),
            safeLong(projection.getEnrolledStudents()),
            projection.getRevenue() == null ? BigDecimal.ZERO : projection.getRevenue(),
            safeLong(projection.getModuleCount()),
            safeLong(projection.getVideoCount()),
            resolvePersistedStatus(projection.getCourseStatus(), projection.getActiveFlag()).name(),
            resolveStatusLabel(resolvePersistedStatus(projection.getCourseStatus(), projection.getActiveFlag())),
            Boolean.TRUE.equals(projection.getActiveFlag()),
            formatDate(coalesce(projection.getUpdatedAt(), projection.getCreatedAt()))
        );
    }

    private int calculateCompletionPercent(AdminCourseDetailProjection projection, long moduleCount, long videoCount) {
        int score = 0;
        if (!isBlank(projection.getTitle())) {
            score += 15;
        }
        if (!isBlank(projection.getDescription())) {
            score += 20;
        }
        if (!isBlank(projection.getImageUrl())) {
            score += 15;
        }
        if (!isBlank(projection.getTeacherId())) {
            score += 15;
        }
        if (projection.getPrice() != null && projection.getPrice().compareTo(BigDecimal.ZERO) > 0) {
            score += 15;
        }
        if (moduleCount > 0) {
            score += 10;
        }
        if (videoCount > 0) {
            score += 10;
        }
        return Math.min(score, 100);
    }

    private void ensureCourseExists(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new IllegalArgumentException("Course not found.");
        }
    }

    private Lesson getLessonInCourse(Long courseId, Long moduleId) {
        return lessonRepository.findByIdAndCourseId(moduleId, courseId)
            .orElseThrow(() -> new IllegalArgumentException("Module not found in this course."));
    }

    private LessonVideo getVideoInLesson(Long moduleId, Long videoId) {
        return lessonVideoRepository.findByIdAndLessonId(videoId, moduleId)
            .orElseThrow(() -> new IllegalArgumentException("Video not found in this module."));
    }

    private void applyModuleForm(Lesson lesson, AdminCourseModuleFormDto form) {
        lesson.setTitle(required(form.getTitle(), "Module title is required."));
        lesson.setDescription(form.getDescription());
        lesson.setPosition(resolvePosition(form.getPosition()));
    }

    /**
     * Đồng bộ dữ liệu form thêm hoặc sửa video vào entity trước khi lưu xuống database.
     *
     * @param video entity video sẽ được cập nhật dữ liệu
     * @param form dữ liệu gửi lên từ màn hình quản trị video
     * @return không trả dữ liệu; method cập nhật trực tiếp trên entity đầu vào
     */
    private void applyVideoForm(LessonVideo video, AdminCourseVideoFormDto form) {
        // Step 1: kiểm tra các field bắt buộc như tiêu đề và URL video trước khi gán vào entity.
        // Step 2: chuẩn hóa các field số như thời lượng, position và cờ preview từ form quản trị.
        video.setTitle(required(form.getTitle(), "Video title is required."));
        video.setVideoUrl(required(form.getVideoUrl(), "Video URL is required."));
        video.setDuration(form.getDuration() == null || form.getDuration() < 0 ? 0 : form.getDuration());
        video.setPosition(resolvePosition(form.getPosition()));
        video.setPreview(Boolean.TRUE.equals(form.getPreview()));
    }

    private int resolvePosition(Integer position) {
        return position == null || position < 1 ? 1 : position;
    }

    private String formatDuration(Integer seconds) {
        if (seconds == null || seconds <= 0) {
            return "--";
        }

        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int remainingSeconds = seconds % 60;
        if (hours > 0) {
            return String.format(Locale.ROOT, "%d:%02d:%02d", hours, minutes, remainingSeconds);
        }
        return String.format(Locale.ROOT, "%02d:%02d", minutes, remainingSeconds);
    }

    private Boolean resolveActiveFlag(CourseStatus status) {
        return status == CourseStatus.PUBLISHED;
    }

    private CourseStatus resolveStatus(String rawStatus, Boolean visible) {
        if (!isBlank(rawStatus)) {
            try {
                return CourseStatus.valueOf(rawStatus.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Invalid course status: " + rawStatus);
            }
        }
        return Boolean.TRUE.equals(visible) ? CourseStatus.PUBLISHED : CourseStatus.HIDDEN;
    }

    private CourseStatus resolvePersistedStatus(String rawStatus, Boolean activeFlag) {
        if (!isBlank(rawStatus)) {
            try {
                return CourseStatus.valueOf(rawStatus.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                return Boolean.TRUE.equals(activeFlag) ? CourseStatus.PUBLISHED : CourseStatus.HIDDEN;
            }
        }
        return Boolean.TRUE.equals(activeFlag) ? CourseStatus.PUBLISHED : CourseStatus.HIDDEN;
    }

    private String resolveStatusLabel(CourseStatus status) {
        return switch (status) {
            case DRAFT -> "Draft";
            case REVIEW -> "Review";
            case PUBLISHED -> "Published";
            case HIDDEN -> "Hidden";
            case ARCHIVED -> "Archived";
        };
    }

    private int resolvePage(Integer page) {
        return page == null || page < 0 ? 0 : page;
    }

    private int resolveSize(Integer size) {
        if (size == null || size <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String normalizeStatus(String status) {
        return status == null || status.isBlank() ? null : status.trim().toUpperCase(Locale.ROOT);
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    private Integer resolveYear(LocalDateTime updatedAt, LocalDateTime createdAt) {
        LocalDateTime date = coalesce(updatedAt, createdAt);
        return date == null ? null : date.getYear();
    }

    private LocalDateTime coalesce(LocalDateTime first, LocalDateTime second) {
        return first != null ? first : second;
    }

    private String formatDate(LocalDateTime value) {
        return value == null ? "--" : value.format(DATE_FORMATTER);
    }

    private String required(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    // Xac dinh co can xoa video cu khi URL moi khac URL hien tai hay khong.
    private boolean shouldDeleteOldVideo(String oldVideoUrl, String newVideoUrl) {
        if (isBlank(oldVideoUrl) || isBlank(newVideoUrl)) {
            return false;
        }
        return !oldVideoUrl.trim().equals(newVideoUrl.trim()) && uploadService.isManagedUploadUrl(oldVideoUrl.trim());
    }
}
