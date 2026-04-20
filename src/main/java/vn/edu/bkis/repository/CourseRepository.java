package vn.edu.bkis.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import vn.edu.bkis.dto.admin.AdminCourseDetailProjection;
import vn.edu.bkis.dto.admin.AdminCourseListProjection;
import vn.edu.bkis.model.Course;

/**
 * Repository for Course entity providing database access for course data.
 * Handles queries for active courses, filtering, and pagination.
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    /**
     * Retrieve active courses ordered alphabetically for admin select options.
     *
     * @return active course list
     */
    List<Course> findByActiveFlagTrueOrderByTitleAsc();

    /**
     * Retrieve top 6 active courses ordered by creation date (newest first).
     * @return list of up to 6 active courses
     */
    List<Course> findTop6ByActiveFlagTrueOrderByCreatedAtDesc();

    /**
     * Retrieve courses filtered by tag and active status.
     * @param tag the course tag/category to filter by
     * @return list of active courses with specified tag
     */
    List<Course> findTop6ByActiveFlagTrueAndTagOrderByCreatedAtDesc(String tag);

    /**
     * Retrieve related courses excluding a specific course.
     * @param id the ID of the course to exclude
     * @return list of up to 4 other active courses
     */
    List<Course> findTop4ByActiveFlagTrueAndIdNotOrderByCreatedAtDesc(Long id);

    /**
     * Retrieve top 4 active courses ordered by total students (most enrolled first).
     * @return list of up to 4 most popular active courses
     */
    List<Course> findTop4ByActiveFlagTrueOrderByTotalStudentsDesc();

    @Query(value = "SELECT COUNT(c.id) FROM courses c WHERE c.active_flag = TRUE",nativeQuery = true)
    Long countAllCourses();

    @Query(value = "SELECT COUNT(c.id) FROM courses c WHERE c.active_flag = TRUE " +
        " AND c.created_at BETWEEN DATE_FORMAT(CURDATE(), '%Y-%m-01') AND LAST_DAY(CURDATE())",nativeQuery = true)
    Long countAllCoursesCreateThisMonth();

    @Query(value = """
        SELECT
            c.id AS id,
            c.title AS title,
            c.tag AS tag,
            c.price AS price,
            c.active_flag AS activeFlag,
            COALESCE(c.course_status, CASE WHEN c.active_flag = TRUE THEN 'PUBLISHED' ELSE 'HIDDEN' END) AS courseStatus,
            c.created_at AS createdAt,
            c.updated_at AS updatedAt,
            teacher.full_name AS teacherName,
            COALESCE(enroll_stats.enrolled_students, 0) AS enrolledStudents,
            COALESCE(payment_stats.revenue, 0) AS revenue,
            COALESCE(module_stats.module_count, 0) AS moduleCount,
            COALESCE(video_stats.video_count, 0) AS videoCount
        FROM courses c
        LEFT JOIN users teacher ON teacher.id = c.teacher_id
        LEFT JOIN (
            SELECT course_id, COUNT(*) AS enrolled_students
            FROM enrollments
            GROUP BY course_id
        ) enroll_stats ON enroll_stats.course_id = c.id
        LEFT JOIN (
            SELECT course_id, SUM(CASE WHEN status = 'COMPLETED' THEN amount ELSE 0 END) AS revenue
            FROM payments
            GROUP BY course_id
        ) payment_stats ON payment_stats.course_id = c.id
        LEFT JOIN (
            SELECT course_id, COUNT(*) AS module_count
            FROM lessons
            GROUP BY course_id
        ) module_stats ON module_stats.course_id = c.id
        LEFT JOIN (
            SELECT l.course_id AS course_id, COUNT(lv.id) AS video_count
            FROM lessons l
            JOIN lesson_videos lv ON lv.lesson_id = l.id
            GROUP BY l.course_id
        ) video_stats ON video_stats.course_id = c.id
        WHERE (:keyword IS NULL OR :keyword = ''
            OR LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(COALESCE(c.tag, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(COALESCE(teacher.full_name, '')) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:year IS NULL OR YEAR(COALESCE(c.updated_at, c.created_at)) = :year)
          AND (:status IS NULL OR :status = ''
            OR COALESCE(c.course_status, CASE WHEN c.active_flag = TRUE THEN 'PUBLISHED' ELSE 'HIDDEN' END) = :status)
        ORDER BY COALESCE(c.updated_at, c.created_at) DESC, c.id DESC
        """,
        countQuery = """
        SELECT COUNT(*)
        FROM courses c
        LEFT JOIN users teacher ON teacher.id = c.teacher_id
        WHERE (:keyword IS NULL OR :keyword = ''
            OR LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(COALESCE(c.tag, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(COALESCE(teacher.full_name, '')) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:year IS NULL OR YEAR(COALESCE(c.updated_at, c.created_at)) = :year)
          AND (:status IS NULL OR :status = ''
            OR COALESCE(c.course_status, CASE WHEN c.active_flag = TRUE THEN 'PUBLISHED' ELSE 'HIDDEN' END) = :status)
        """,
        nativeQuery = true)
    Page<AdminCourseListProjection> searchAdminCourses(@Param("keyword") String keyword,
                                                       @Param("year") Integer year,
                                                       @Param("status") String status,
                                                       Pageable pageable);

    @Query(value = """
        SELECT DISTINCT YEAR(COALESCE(c.updated_at, c.created_at))
        FROM courses c
        WHERE COALESCE(c.updated_at, c.created_at) IS NOT NULL
        ORDER BY YEAR(COALESCE(c.updated_at, c.created_at)) DESC
        """, nativeQuery = true)
    List<Integer> findAdminCourseYears();

    @Query(value = """
        SELECT COUNT(*)
        FROM courses c
        WHERE YEAR(COALESCE(c.updated_at, c.created_at)) = :year
        """, nativeQuery = true)
    long countAdminCoursesByYear(@Param("year") int year);

    @Query(value = "SELECT COUNT(*) FROM courses c WHERE c.active_flag = TRUE", nativeQuery = true)
    long countPublishedCoursesForAdmin();

    @Query(value = """
        SELECT COUNT(*)
        FROM courses c
        WHERE COALESCE(c.course_status, CASE WHEN c.active_flag = TRUE THEN 'PUBLISHED' ELSE 'HIDDEN' END) = 'DRAFT'
        """, nativeQuery = true)
    long countDraftCoursesForAdmin();

    @Query(value = """
        SELECT COUNT(*)
        FROM courses c
        WHERE COALESCE(c.course_status, CASE WHEN c.active_flag = TRUE THEN 'PUBLISHED' ELSE 'HIDDEN' END) = 'PUBLISHED'
        """, nativeQuery = true)
    long countPublishedStatusCoursesForAdmin();

    @Query(value = """
        SELECT COUNT(*)
        FROM courses c
        WHERE COALESCE(c.course_status, CASE WHEN c.active_flag = TRUE THEN 'PUBLISHED' ELSE 'HIDDEN' END) = 'HIDDEN'
        """, nativeQuery = true)
    long countHiddenCoursesForAdmin();

    @Query(value = """
        SELECT
            c.id AS id,
            c.title AS title,
            c.description AS description,
            c.highlights AS highlights,
            c.teacher_id AS teacherId,
            teacher.full_name AS teacherName,
            c.price AS price,
            c.total_students AS totalStudents,
            c.active_flag AS activeFlag,
            COALESCE(c.course_status, CASE WHEN c.active_flag = TRUE THEN 'PUBLISHED' ELSE 'HIDDEN' END) AS courseStatus,
            c.tag AS tag,
            c.image_url AS imageUrl,
            c.rating AS rating,
            c.created_at AS createdAt,
            c.updated_at AS updatedAt,
            COALESCE(enroll_stats.enrolled_students, 0) AS enrolledStudents,
            COALESCE(payment_stats.revenue, 0) AS revenue,
            COALESCE(module_stats.module_count, 0) AS moduleCount,
            COALESCE(video_stats.video_count, 0) AS videoCount,
            COALESCE(payment_stats.payment_count, 0) AS paymentCount
        FROM courses c
        LEFT JOIN users teacher ON teacher.id = c.teacher_id
        LEFT JOIN (
            SELECT course_id, COUNT(*) AS enrolled_students
            FROM enrollments
            GROUP BY course_id
        ) enroll_stats ON enroll_stats.course_id = c.id
        LEFT JOIN (
            SELECT course_id,
                   SUM(CASE WHEN status = 'COMPLETED' THEN amount ELSE 0 END) AS revenue,
                   COUNT(*) AS payment_count
            FROM payments
            GROUP BY course_id
        ) payment_stats ON payment_stats.course_id = c.id
        LEFT JOIN (
            SELECT course_id, COUNT(*) AS module_count
            FROM lessons
            GROUP BY course_id
        ) module_stats ON module_stats.course_id = c.id
        LEFT JOIN (
            SELECT l.course_id AS course_id, COUNT(lv.id) AS video_count
            FROM lessons l
            JOIN lesson_videos lv ON lv.lesson_id = l.id
            GROUP BY l.course_id
        ) video_stats ON video_stats.course_id = c.id
        WHERE c.id = :courseId
        """, nativeQuery = true)
    Optional<AdminCourseDetailProjection> findAdminCourseDetailById(@Param("courseId") Long courseId);

    @Query(value = "SELECT COUNT(*) FROM enrollments en WHERE en.course_id = :courseId", nativeQuery = true)
    long countEnrollmentsByCourseId(@Param("courseId") Long courseId);

    @Query(value = "SELECT COUNT(*) FROM payments p WHERE p.course_id = :courseId", nativeQuery = true)
    long countPaymentsByCourseId(@Param("courseId") Long courseId);
}
